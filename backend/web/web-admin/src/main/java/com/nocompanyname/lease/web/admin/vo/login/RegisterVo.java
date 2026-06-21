package com.nocompanyname.lease.web.admin.vo.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "注册请求参数")
public class RegisterVo {
    @Schema(description="用户名")
    private String username;

    @Schema(description="密码")
    private String password;

    @Schema(description="二次密码输入")
    private String passwordAgain;

    @Schema(description="验证码key")
    private String captchaKey;

    @Schema(description="验证码code")
    private String captchaCode;
}
