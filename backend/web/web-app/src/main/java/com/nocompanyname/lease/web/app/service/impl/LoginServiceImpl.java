package com.nocompanyname.lease.web.app.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.redisConstant.RedisConstant;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.common.utils.JwtUtil;
import com.nocompanyname.lease.model.entity.UserInfo;
import com.nocompanyname.lease.model.enums.BaseStatus;
import com.nocompanyname.lease.web.app.context.LoginUserHolder;
import com.nocompanyname.lease.web.app.mapper.UserInfoMapper;
import com.nocompanyname.lease.web.app.service.LoginService;
import com.nocompanyname.lease.web.app.service.SmsService;
import com.nocompanyname.lease.web.app.vo.user.LoginVo;
import com.nocompanyname.lease.web.app.vo.user.UserInfoVo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class LoginServiceImpl implements LoginService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$"); //通过正则表达式校验手机号码格式是否合规
    private static final String RESEND_KEY_SEGMENT = "resend:"; //重新发送
    private static final String ATTEMPT_KEY_SEGMENT = "attempt:"; //验证码输错次数
    private static final int MAX_CODE_ATTEMPTS = 5; //最大输错次数


    //用 构造方法 注入 需要的 Java类对象
    //工程级项目更推荐使用 构造器 + final关键字
    //构造器会强制注入参数，确保 生成的对象的值不是null，不会出现空指针异常
    //final关键字 保证成员变量的值不会被修改
    //单元测试很方便 LoginServiceImpl service = new LoginServiceImpl(mockSmsService);
    private final StringRedisTemplate stringRedisTemplate;
    private final SmsService smsService;
    private final UserInfoMapper userInfoMapper;
    private final JwtUtil jwtUtil;

    public LoginServiceImpl(
            StringRedisTemplate stringRedisTemplate,
            SmsService smsService,
            UserInfoMapper userInfoMapper,
            JwtUtil jwtUtil
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.smsService = smsService;
        this.userInfoMapper = userInfoMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void getCode(String phone) {
        String normalizedPhone = normalizePhone(phone); //去掉前后空格，获得手机号

        //先要看这个请求是否是 重复发送，如果重复发送，要判断是否在指定时间外
        //这个 resendKey 相当于一个 验证码发送锁，用来限制用户在短时间内重复发送短信（与用户侧的校验无关）
        String resendKey = RedisConstant.APP_LOGIN_PREFIX + RESEND_KEY_SEGMENT + normalizedPhone;

        /*
                           点击获取验证码
                                ↓
                        手机号对应的发送锁存在吗？
                                ↓
                            ┌───┴───┐
                            ↓       ↓
                         不存在    已存在
                            ↓       ↓
                         创建锁    拒绝发送
                         60秒过期   "请求过于频繁"
                            ↓
                         发送短信
         */
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(
                resendKey,
                "1", //redis key的value，这里的 "1" 没有业务含义，只是为了匹配 resendKey，形成redis中的键值对
                RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC, //60秒内 resendKey 存在则不能发送
                TimeUnit.SECONDS
        );

        //如果 acquired 是 false，说明锁已经存在，那么久不用创建锁了，而是进一步提醒：验证码发送过于频繁
        if (!Boolean.TRUE.equals(acquired)) {
            throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
        }

        String code = RandomUtil.randomNumbers(4); //生成4位数字验证码
        String redisKey = buildRedisKey(normalizedPhone); //生成redisKey

        try {
            //将验证码键值对存入redis
            stringRedisTemplate.opsForValue().set(
                    redisKey,
                    code,
                    RedisConstant.APP_LOGIN_CODE_TTL_SEC,
                    TimeUnit.SECONDS
            );

            //向阿里云客户端发送短信参数
            smsService.sendSms(normalizedPhone, code);

            //如果短信发送失败，就删除Redis中的redisKey和resendKey
        } catch (RuntimeException e) {
            stringRedisTemplate.delete(redisKey); //redisKey和对应的value一起删除
            stringRedisTemplate.delete(resendKey); //resendKey和对应的value一起删除
            throw new LeaseException(ResultCodeEnum.SERVICE_ERROR, e);
            /*
            这一步是为了防止：
            短信没发出去，但 Redis 里已经存了验证码，用户又不能重新获取
             */
        }
    }

    @Override
    public String login(LoginVo loginVo) {
        if (loginVo == null || !StringUtils.hasText(loginVo.getPhone())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }

        if (!StringUtils.hasText(loginVo.getCode())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }

        //清理手机号和用户输入的验证码的前后空格
        String phone = normalizePhone(loginVo.getPhone());
        String redisKey = buildRedisKey(phone);
        String attemptKey = buildAttemptKey(phone); //此时，这个attemptKey还没有value

        String redisCode = stringRedisTemplate.opsForValue().get(redisKey);
        //这个redisCode 只要非空，redis里存储的就一定是redisKey的value，只不过它还需要和用户输入的验证码进行比较

        if(!StringUtils.hasText(redisCode)){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }

        if(!redisCode.equals(loginVo.getCode())){

            //当输入验证码和redis验证码不匹配时，进入计数模式
            //第一次输错后，将attemptKey的value设置为1
            //Long increment(String key) 表示 将 Redis 中 key 对应的数值加 1，并返回加 1 后的新值
            //如果redis中原本不存在 attemptKey，那么redis会自动将key的值从0开始计算 +1
            Long count = stringRedisTemplate.opsForValue().increment(attemptKey);

            if(count != null && count == 1){
                //只有第一次输错的时候，设置过期时间，这样可以让错误次数和验证码大致在同一个时间范围内失效
                //如果60秒内，还没验证成功 并且 输错次数不足5次，attemptKey到时间自动删除（和redisKey同时删除）
                stringRedisTemplate.expire( //给 Redis 中的 key 设置过期时间，到时间后 Redis 自动删除这个 key。
                        attemptKey,
                        RedisConstant.APP_LOGIN_CODE_TTL_SEC,
                        TimeUnit.SECONDS);
            }

            //如果60秒内，还没验证成功 并且 输错次数达到5次，主动删除attemptKey和redisKey
            if(count != null && count >= MAX_CODE_ATTEMPTS){
                stringRedisTemplate.delete(attemptKey);
                stringRedisTemplate.delete(redisKey);
            }

            //每次校验不匹配，都会throw这个异常
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        //这一步，是针对 验证码验证成功之后，也要删除Key和Value
        //删除attemptKey，如果验证码一次成功，那么删除null（redis里没有attemptKey），不会报错
        //如果验证码不是一次成功，而是2/3/4次成功（且在60秒内），那么redis里是有 attemptKey=value 记录的，因此即便成功了也要删除
        stringRedisTemplate.delete(attemptKey);
        stringRedisTemplate.delete(redisKey);

        //通过验证之后，接下来检查用户账户是否存在
        //根据手机号查询id,username 和 status（这里不是说其他的成员变量没有了，而是不给他们赋值，因为用不到它们）
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getPhone, phone)
                .select(UserInfo::getId, UserInfo::getPhone, UserInfo::getStatus);

        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);

        if(userInfo == null){
            //如果账户不存在，用注册账户代替抛出异常
            UserInfo registerUser = new UserInfo();
            registerUser.setPhone(phone);
            registerUser.setStatus(BaseStatus.ENABLE);
            registerUser.setNickname("新用户" + phone.substring(7));
            userInfoMapper.insert(registerUser);
            userInfo = registerUser;
        }

        if(userInfo.getStatus() == BaseStatus.DISABLE){
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }

        String token = jwtUtil.createToken(userInfo.getId(), userInfo.getPhone());
        return token;
    }

    @Override
    public UserInfoVo getInfo() {
        //这个userId 是 AppLoginInterceptor拦截器 经过拦截过滤，放入到 线程ThreadLocal中的
        Long userId = LoginUserHolder.getUserId();

        if (userId == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }

        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_USER_NOT_EXIST);
        }

        if (userInfo.getStatus() == BaseStatus.DISABLE) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }

        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setNickname(userInfo.getNickname());
        userInfoVo.setAvatarUrl(userInfo.getAvatarUrl());

        return userInfoVo;
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }

        String normalizedPhone = phone.trim(); //去掉空字符串
        if (!PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        return normalizedPhone;
    }

    private String buildRedisKey(String phone) {
        return RedisConstant.APP_LOGIN_PREFIX + phone;
    }

    private String buildAttemptKey(String phone) {
        return RedisConstant.APP_LOGIN_PREFIX + ATTEMPT_KEY_SEGMENT + phone;
    }
}
