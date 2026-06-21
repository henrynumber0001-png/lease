package com.nocompanyname.lease.web.app.service;

import com.nocompanyname.lease.web.app.vo.user.LoginVo;
import com.nocompanyname.lease.web.app.vo.user.UserInfoVo;

public interface LoginService {

    void getCode(String phone);

    String login(LoginVo loginVo);

    UserInfoVo getInfo();

}
