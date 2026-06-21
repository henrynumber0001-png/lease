package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.redisConstant.RedisConstant;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.common.utils.JwtUtil;
import com.nocompanyname.lease.model.entity.SystemUser;
import com.nocompanyname.lease.model.enums.BaseStatus;
import com.nocompanyname.lease.model.enums.SystemUserType;
import com.nocompanyname.lease.web.admin.custom.context.LoginUserHolder;
import com.nocompanyname.lease.web.admin.mapper.SystemUserMapper;
import com.nocompanyname.lease.web.admin.service.LoginService;
import com.nocompanyname.lease.web.admin.service.SystemUserService;
import com.nocompanyname.lease.web.admin.vo.login.CaptchaVo;
import com.nocompanyname.lease.web.admin.vo.login.LoginVo;
import com.nocompanyname.lease.web.admin.vo.login.RegisterVo;
import com.nocompanyname.lease.web.admin.vo.system.user.SystemUserInfoVo;
import com.wf.captcha.SpecCaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @Autowired
    private JwtUtil jwtUtil;

    private final StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SystemUserService systemUserService;

    //构造方法注入StringRedisTemplate
    public LoginServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4); //创建一个PNG格式的图形验证码
        String captchaCode = specCaptcha.text().toLowerCase(); //获取本次生成的验证码文本内容（正确答案）

        String key = UUID.randomUUID().toString(); //返回给前端
        String redisKey = RedisConstant.ADMIN_LOGIN_PREFIX + key; //生成Redis的Key

        String image = specCaptcha.toBase64(); //toBase64()将图形验证码从PNG格式转换成字符串类型

        stringRedisTemplate.opsForValue().set(
                redisKey,
                captchaCode,
                RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC,
                TimeUnit.SECONDS
        );
        /*
        因为CaptchaVo的成员变量 是字符串类型，所以要创建一个操作 String类型数据的 Redis工具对象：stringRedisTemplate
        opsForValue() = 操作 String类型
        set() = 设置 String类型的 key-value
        key = Redis的Key， 是一个 api标识 + UUID
        captchaCode = 验证码答案
         */

        return new CaptchaVo(image, key);
        /*
        将验证码图片 和 验证码答案对应的唯一标识（验证码的key），返回给前端。
        用户看到图片，输入验证码之后，
        浏览器将 验证码 和 Key 一起发送到后端。
        这样后端才能知道 用户输入的到底是哪一条 验证码，才能进行匹配和验证。

        例如：
        {
          "username":"admin",
          "password":"123456",
          "captchaCode":"a8k3",
          "captchaKey":"admin:login:UUID"
        }
         */
    }

    /*
    captchaKey的获取与装配逻辑：
    服务器生成 captchaKey
    → 第一次：查询请求，生成captchaKey 并返回给浏览器
    → 浏览器临时保存
    → 第二次：发送登录请求，携带回来给服务器
    → 服务器用它查询 Redis
     */
    @Override
    public String login(LoginVo loginVo) {

        //第一步：非空校验

        //首先是必要字段空了，报 参数异常
        if(loginVo == null
                || !StringUtils.hasText(loginVo.getUsername())
                || !StringUtils.hasText(loginVo.getPassword())) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }


        //验证码空了，报 验证码为空 异常
        if(!StringUtils.hasText(loginVo.getCaptchaCode())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }


        if(!StringUtils.hasText(loginVo.getCaptchaKey())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_KEY_NOT_FOUND);
        }
        /*
        虽然captchaKey 不是由用户输入的，但还是要为可能的 captchaKey 为空的场景做判断
        因为当 captchaKey == null 时，后面 get(loginVo.getCaptchaKey()) 会报 系统异常
         */

        /*
        那么如果 loginVo.getCaptchaKey() 非空，但同时也是被恶意篡改的 错误captchaKey 呢？
        其实这一点，无法通过常规的业务涉及去阻断，我们不能控制和匹配浏览器中的 Key与getCaptcha() 生成的key，因为同一时间有大量用户在使用验证码服务
        但我们至少可以通过条件判断确保，错误的key永远不可能返回验证码答案（不会匹配成功）
        这至少在业务上防止了恶意攻击的发生

        黑客就算拿到captchaKey，也无法进入到redis（后端的服务器），那么就无法获得captchaCode
        所以只要保证redis安全，黑客得到captchaKey问题也不大
         */


        //第二步：从redis中读取captchaKey对应的captchaCode，校验验证码

        //redisKey 和 返回前端的key 分离
        //前端不需要知道 Redis 的实现细节
        //以后修改 Redis 结构更容易
        String redisKey = RedisConstant.ADMIN_LOGIN_PREFIX + loginVo.getCaptchaKey();
        String captchaCode = stringRedisTemplate.opsForValue().get(redisKey);
        //这里只要能获取到 captchaCode的值，那么就是redisKey在redis里的value，但是你不能确保 这个captchaCode 一定能匹配上 用户输入的 code
        //比如连续发送两次 验证码，redis中会更新第二次的值，但你发送的是第一次的值
        //但因为有if条件判断，如果不匹配，这正是代码设计目的的体现

        if(!StringUtils.hasText(captchaCode)){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }
        //如果非空，那就说明根据 captchaKey 可以找到 captchaCode，接下来就是去验证这个返回的 captchaCode 与 loginVo 自带的是否一致

        //第三步：验证 captchaCode
        //redis中正确答案与输入的 captchaCode 进行比较
        if(!captchaCode.equalsIgnoreCase(loginVo.getCaptchaCode())){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }

        //第四步：验证成功后立刻删除captchaKey 和 对应的 code，无法再做二次验证，保证安全
        stringRedisTemplate.delete(redisKey);

        //第五步：验证码验证成功之后，接下来是验证 用户信息 是否存在，存在才能发给token，允许进入
        //先查用户名有没有（能根据用户名查到systemUser，至少说明这个 username 是存在的，至于是不是他的，还需要再继续查验密码）
        LambdaQueryWrapper<SystemUser> systemUserQueryWrapper = new LambdaQueryWrapper<>();
        systemUserQueryWrapper.eq(SystemUser::getUsername, loginVo.getUsername())
                .select(SystemUser::getId, SystemUser::getUsername, SystemUser::getPassword,SystemUser::getStatus);

        SystemUser systemUser = systemUserMapper.selectOne(systemUserQueryWrapper);
        // 根据查询条件，只给select的字段: id,user_name,password,status 所对应的成员变量赋值，其余成员变量的值=null

        if(systemUser == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }

        //接下来验证密码
        boolean result = bCryptPasswordEncoder.matches(loginVo.getPassword(), systemUser.getPassword());

        if(!result){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        }

        //第六步：验证账户权限
        //当 用户名 和 密码都通过验证之后，要看看这个账号有没有被封
        if(systemUser.getStatus() == BaseStatus.DISABLE){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
        }

        //到这一步都没问题，就安全通过所有验证：非空验证、验证码校验、用户名和密码校验、账户权限校验
        //接下来就是生成token（一个能够证明“你已经通过验证”的身份证），发给前端，通常是JWT(JSON Web Token）
        String token = jwtUtil.createToken(systemUser.getId(), systemUser.getUsername());

        return token; //返回的是一个JTW字符串
    }

    @Override
    public SystemUserInfoVo getInfo() {

        Long userId = LoginUserHolder.getUserId();
        //通过获得的userId，可以知道保存它的线程是哪一条
        /*
        Service层并不知道要处理哪一个token，但拦截器从token中解析出userId，并放入到一个线程中。
        这样，Service层只需要获取这个userId，就可以去查询用户信息了。
         */

        if(userId == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }
        /*
        一种防御性的检查。
        即便数据库已经将 userId = 1001 删除了，但是 userId 判断依然不为null，因为它是从线程中获取的 userId 信息。

        这一步的作用是：
        拦截器路径配置错误，导致接口没有被拦截；
        后续其他代码直接调用 getInfo()；
        ThreadLocal 存储逻辑被修改；
        单元测试直接调用 Service，没有经过 HTTP 拦截器；
        异步线程中调用 getInfo()，普通 ThreadLocal 不会自动传递。

        可以理解为一个兜底的冗余设计
         */


        SystemUser systemUser = systemUserMapper.selectById(userId);

        if(systemUser == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);//账号不存在
        }
        /*
        这一步的非null判断，还是非常有必要的。
        因为 JWT 中存在 userId，只代表：这个 Token 签发时记录了该 userId，并且 Token 的签名有效。
        它不能保证该用户当前仍然存在于数据库中。
        例如：
        用户 ID 为 1001，登录成功并获得 Token。
        管理员删除了用户 1001。
        用户继续携带尚未过期的 Token 请求 /admin/info。
        JWT 仍然能正常解析出 1001。
        但数据库查询结果是 null。

        所以这两个验证的目标不同：
        jwtUtil.getUserId(token)
        验证 Token 是否可信，并取得它声明的用户 ID

        systemUserMapper.selectById(userId)
        验证该用户在当前数据库中是否仍然存在
         */


        if(systemUser.getStatus() == BaseStatus.DISABLE){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
        }

        SystemUserInfoVo systemUserInfoVo = new SystemUserInfoVo();
        systemUserInfoVo.setName(systemUser.getName());
        systemUserInfoVo.setAvatarUrl(systemUser.getAvatarUrl());

        return systemUserInfoVo; //返回用户信息 给浏览器显示
    }

    @Override
    public String register(RegisterVo registerVo) {
        if(registerVo == null || !StringUtils.hasText(registerVo.getUsername()) ||
        !StringUtils.hasText(registerVo.getPassword())){
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        if(!StringUtils.hasText(registerVo.getCaptchaCode())){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }

        if(!StringUtils.hasText(registerVo.getCaptchaKey())){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_KEY_NOT_FOUND);
        }

        String redisKey = RedisConstant.ADMIN_LOGIN_PREFIX + registerVo.getCaptchaKey();
        String captchaCode = stringRedisTemplate.opsForValue().get(redisKey);

        if(!StringUtils.hasText(captchaCode)){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }

        if(!captchaCode.equalsIgnoreCase(registerVo.getCaptchaCode())){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }

        stringRedisTemplate.delete(redisKey);

        if(!registerVo.getPassword().equals(registerVo.getPasswordAgain())){
            throw new LeaseException(ResultCodeEnum.ADMIN_PASSWORD_NOT_MATCH);
        }

        SystemUser systemUser = new SystemUser();
        systemUser.setUsername(registerVo.getUsername());
        if(!systemUserService.isUsernameAvailabe(registerVo.getUsername())){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_EXIST_ERROR);
        }

        systemUser.setPassword(bCryptPasswordEncoder.encode(registerVo.getPassword()));
        systemUser.setStatus(BaseStatus.ENABLE);
        systemUser.setType(SystemUserType.ADMIN);
        systemUserService.save(systemUser);

        String token = jwtUtil.createToken(systemUser.getId(), systemUser.getUsername());

        return token;

    }
}

/*
首次登录的业务逻辑：
┌──────────────────────────────┐
│ 用户打开登录页面                │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ 浏览器请求验证码                │
│ GET /admin/login/captcha     │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ 服务器生成验证码                │
│                              │
│ Redis 保存：                  │
│ admin:login:UUID → a8k3      │
│                              │
│ 返回浏览器：                   │
│ captchaKey + 验证码图片        │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ 浏览器显示验证码图片            │
│ 并临时保存 captchaKey          │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ 用户输入:                     │
│ 用户名,密码,验证码              │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ 浏览器提交登录请求              │
│ POST /admin/login            │
│                              │
│ username                     │
│ password                     │
│ captchaCode                  │
│ captchaKey                   │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ login() 校验                  │
│                              │
│ 1. 校验验证码                  │
│ 2. 校验用户名和密码             │
│ 3. 检查账号状态                │
└──────────────┬───────────────┘
               │ 校验成功
               ▼
┌──────────────────────────────┐
│ 服务器生成并返回 JWT            │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ 浏览器保存 JWT                 │
│ 例如保存到内存或存储空间         │
└──────────────┬───────────────┘
               │ 自动发起请求
               ▼
┌──────────────────────────────┐
│ GET /admin/info              │
│ Authorization: Bearer JWT    │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ LoginInterceptor             │
│                              │
│ 1. 读取 JWT                  │
│ 2. 验证 JWT                  │
│ 3. 解析 userId               │
│ 4. 写入 LoginUserHolder      │
└──────────────┬───────────────┘
               │ return true
               ▼
┌──────────────────────────────┐
│ Controller.info()            │
│       ↓                      │
│ LoginService.getInfo()       │
│       ↓                      │
│ 从 LoginUserHolder 取 userId  │
│       ↓                      │
│ 查询 system_user              │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ 返回用户信息                   │
│                              │
│ name、avatarUrl 等            │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ 浏览器显示后台首页              │
│ 用户姓名、头像等                │
└──────────────────────────────┘
 */

/*
再次登录的业务逻辑：
┌────────────────────────────┐
│ 用户输入网址打开网站          │
└─────────────┬──────────────┘
              │
              ▼
┌────────────────────────────┐
│ 浏览器前端代码启动            │
└─────────────┬──────────────┘
              │
              ▼
┌────────────────────────────┐
│ 浏览器检查是否保存 Token      │
└─────────────┬──────────────┘
              │
       有 Token│没有 Token
              │
              ├──────────────► 跳转登录页
              ▼
┌────────────────────────────┐
│ 携带 Token 请求 /admin/info │
└─────────────┬──────────────┘
              │
              ▼
┌────────────────────────────┐
│ 后端拦截器验证 Token         │
└─────────────┬──────────────┘
              │
       有效   │无效/过期
              │
              ├──────────────► 返回未登录/Token过期
              ▼
┌────────────────────────────┐
│ 返回当前用户信息              │
└─────────────┬──────────────┘
              ▼
┌────────────────────────────┐
│ 前端进入后台首页              │
└────────────────────────────┘

 */

/*
前端保存token的位置：
1. localStorage
localStorage.setItem('token', token);

2. sessionStorage
sessionStorage.setItem('token', token);

3. cookie
document.cookie = 'token=' + token + '; path=/';
 */
