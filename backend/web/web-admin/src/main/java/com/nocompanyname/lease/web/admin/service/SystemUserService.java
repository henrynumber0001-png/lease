package com.nocompanyname.lease.web.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nocompanyname.lease.model.entity.SystemUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nocompanyname.lease.model.enums.BaseStatus;
import com.nocompanyname.lease.web.admin.vo.system.user.SystemUserItemVo;
import com.nocompanyname.lease.web.admin.vo.system.user.SystemUserQueryVo;

/**
* @author liubo
* @description 针对表【system_user(员工信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface SystemUserService extends IService<SystemUser> {

    IPage<SystemUserItemVo> getPage(long current, long size, SystemUserQueryVo queryVo);

    SystemUserItemVo getByUserId(Long id);

    void saveOrUpdateByUser(SystemUser systemUser);

    void removeByUserId(Long id);

    void updateStatusByUserId(Long id, BaseStatus status);


    boolean isUsernameAvailabe(String username);
}
