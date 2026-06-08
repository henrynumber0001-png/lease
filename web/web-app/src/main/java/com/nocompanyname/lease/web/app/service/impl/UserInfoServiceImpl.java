package com.nocompanyname.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.model.entity.UserInfo;
import com.nocompanyname.lease.web.app.service.UserInfoService;
import com.nocompanyname.lease.web.app.mapper.UserInfoMapper;
import com.wf.captcha.SpecCaptcha;
import org.springframework.stereotype.Service;

/**
* @author liubo
* @description 针对表【user_info(用户信息表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements UserInfoService{

    @Override
    public void getCode(String phone) {


    }
}




