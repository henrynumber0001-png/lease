package com.nocompanyname.lease.web.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nocompanyname.lease.model.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nocompanyname.lease.model.enums.BaseStatus;
import com.nocompanyname.lease.web.admin.vo.user.UserInfoQueryVo;

/**
* @author liubo
* @description 针对表【user_info(用户信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface UserInfoService extends IService<UserInfo> {

    IPage<UserInfo> getPageUserInfo(long current, long size, UserInfoQueryVo queryVo);

    void updateStatusById(Long id, BaseStatus status);
}
