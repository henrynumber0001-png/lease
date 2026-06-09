package com.nocompanyname.lease.web.app.controller.login;


import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.web.app.service.LoginService;
import com.nocompanyname.lease.web.app.service.UserInfoService;
import com.nocompanyname.lease.web.app.vo.user.LoginVo;
import com.nocompanyname.lease.web.app.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录管理")
@RestController
@RequestMapping("/app/")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("login/getCode")
    @Operation(summary = "请求短信验证码")
    public Result getCode(@RequestParam String phone) {
        loginService.getCode(phone);
        return Result.ok();
    }

    @PostMapping("login")
    @Operation(summary = "登录")
    public Result<String> login(@RequestBody LoginVo loginVo) {
        String token = loginService.login(loginVo);
        return Result.ok(token);
    }

    @GetMapping("info")
    @Operation(summary = "获取登录用户信息")
    public Result<UserInfoVo> info() {
        UserInfoVo userInfoVo = loginService.getInfo();
        return Result.ok(userInfoVo);
    }
}

