package com.nocompanyname.lease.web.app.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.jwt.JWTUtil;
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
import com.nocompanyname.lease.web.app.vo.user.LoginVo;
import com.nocompanyname.lease.web.app.vo.user.UserInfoVo;
import org.apache.ibatis.mapping.ResultSetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SmsServiceImpl smsServiceImpl;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void getCode(String phone) {

        //创建一个随机4位数的验证码
        String code = RandomUtil.randomNumbers(4);

        String key = phone;
        String redisKey = RedisConstant.APP_LOGIN_PREFIX + key;

        stringRedisTemplate.opsForValue().
                set(redisKey, code, RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);

        //给阿里云客户端发送验证码
        smsServiceImpl.sendSms(phone, code);
    }

    @Override
    public String login(LoginVo loginVo) {

        //非空校验
        if(loginVo == null || !StringUtils.hasText(loginVo.getPhone())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }

        if(!StringUtils.hasText(loginVo.getCode())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }

        //第二步：从redis中读取key对应的code,看是否能匹配到code

        String redisKey = RedisConstant.APP_LOGIN_PREFIX + loginVo.getPhone();
        String verificationCode = stringRedisTemplate.opsForValue().get(redisKey);
        //这一步如果能返回非空的字符串，那么至少说明redis里有这个手机号(redisKey)，否则一定返回null

        if(!StringUtils.hasText(verificationCode)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }

        //删除redisKey，防止被二次利用
        stringRedisTemplate.delete(redisKey);

        //比对code
        if(!verificationCode.equals(loginVo.getCode())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        //查验用户是否存在
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getPhone, loginVo.getPhone())
                .select(UserInfo::getId, UserInfo::getPhone, UserInfo::getStatus);

        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);

        if(userInfo == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_USER_NOT_EXIST);
        }

        if(userInfo.getStatus() == BaseStatus.DISABLE) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }

        String token = jwtUtil.createToken(userInfo.getId(), userInfo.getPhone());

        return token;

    }

    @Override
    public UserInfoVo getInfo() {
         Long userId = LoginUserHolder.getUserId();

         if(userId == null) {
             throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
         }

        UserInfo userInfo = userInfoMapper.selectById(userId);
         if(userInfo == null) {
             throw new LeaseException(ResultCodeEnum.APP_LOGIN_USER_NOT_EXIST);
         }

         if(userInfo.getStatus() == BaseStatus.DISABLE) {
             throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
         }

         UserInfoVo userInfoVo = new UserInfoVo();
         userInfoVo.setNickname(userInfo.getNickname());
         userInfoVo.setAvatarUrl(userInfo.getAvatarUrl());

         return userInfoVo;
    }
}
