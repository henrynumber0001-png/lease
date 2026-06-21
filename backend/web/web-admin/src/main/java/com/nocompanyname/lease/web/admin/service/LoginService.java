package com.nocompanyname.lease.web.admin.service;


import com.nocompanyname.lease.web.admin.vo.login.CaptchaVo;
import com.nocompanyname.lease.web.admin.vo.login.LoginVo;
import com.nocompanyname.lease.web.admin.vo.system.user.SystemUserInfoVo;

public interface LoginService {

    CaptchaVo getCaptcha();

    String login(LoginVo loginVo);

    SystemUserInfoVo getInfo();
}
