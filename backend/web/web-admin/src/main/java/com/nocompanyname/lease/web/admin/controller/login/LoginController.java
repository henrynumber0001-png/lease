package com.nocompanyname.lease.web.admin.controller.login;


import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.web.admin.service.LoginService;
import com.nocompanyname.lease.web.admin.vo.login.CaptchaVo;
import com.nocompanyname.lease.web.admin.vo.login.LoginVo;
import com.nocompanyname.lease.web.admin.vo.login.RegisterVo;
import com.nocompanyname.lease.web.admin.vo.system.user.SystemUserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台管理系统登录管理")
@RestController
@RequestMapping("/admin")
public class LoginController {

    @Autowired
    LoginService loginService;

    @Operation(summary = "获取图形验证码")
    @GetMapping("login/captcha")
    public Result<CaptchaVo> getCaptcha() {
        CaptchaVo captchaVo = loginService.getCaptcha();
        return Result.ok(captchaVo);
    }

    @Operation(summary = "注册")
    @PostMapping("register")
    public Result<String> register(@RequestBody RegisterVo registerVo) {
        String token = loginService.register(registerVo);
        return Result.ok(token);
    }

    @Operation(summary = "登录")
    @PostMapping("login")
    public Result<String> login(@RequestBody LoginVo loginVo) {
        String token = loginService.login(loginVo);
        return Result.ok(token);
    }

    @Operation(summary = "获取登陆用户个人信息")
    @GetMapping("info")
    public Result<SystemUserInfoVo> info() {
        SystemUserInfoVo userInfoVo = loginService.getInfo();
        return Result.ok(userInfoVo);
    }
}

/*
准确来说，login() 和 info() 是两个独立的 HTTP 请求，但前端通常将它们连续执行：
const loginResult = await login(loginForm);
saveToken(loginResult.data);

const userInfo = await getInfo();
displayUserInfo(userInfo.data);

业务逻辑：
用户负责输入登录信息
→ login() 返回 Token
→ 浏览器保存 Token
→ 浏览器携带 Token 请求 info()
→ 服务器返回用户资料
 */

/*
问：Java是如何知道 查询出来的 userInfoVo 应该返回给哪一个前端浏览器的？
答：通过 HttpServletResponse

业务逻辑：
getInfo() 查询到 userInfoVo
→ Controller 返回 Result.ok(userInfoVo)
→ Spring MVC 写入当前请求的 HttpServletResponse
→ HttpServletResponse 返回给发出这个请求的浏览器

Controller层 并不知道要返回给哪个 HttpServletResponse。
userInfoVo 本身也不知道要返回给哪个 HttpServletResponse, 它只是一个普通 Java 对象。
但是 Spring MVC 的 调用链 知道。

每一个前端请求进入服务器时，Tomcat 会创建一组对象：
HttpServletRequest request (浏览器发给服务器的请求，包含请求路径/方法/请求头(JWT)/体)
HttpServletResponse response (userInfoVo，服务器准备返回给浏览器的响应)
然后把它们交给 Spring MVC 当前这一次请求处理流程。

Controller 的返回值 → 被 Spring MVC 捕获 → 使用当前请求绑定的 response 写回给前端浏览器


 */