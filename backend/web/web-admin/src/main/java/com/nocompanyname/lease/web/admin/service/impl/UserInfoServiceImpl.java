package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.UserInfo;
import com.nocompanyname.lease.model.enums.BaseStatus;
import com.nocompanyname.lease.web.admin.service.UserInfoService;
import com.nocompanyname.lease.web.admin.mapper.UserInfoMapper;
import com.nocompanyname.lease.web.admin.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements UserInfoService{

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public IPage<UserInfo> getPageUserInfo(long current, long size, UserInfoQueryVo queryVo) {

        Page<UserInfo> page = new Page<>(current,size);
        //注意：你创建的是Page实现类的对象，而不是使用更抽象的IPage接口作为 变量类型。
        /*
        因为创建出来的真实对象就是 Page。
        Page 是 MyBatis-Plus 提供的具体分页实现类，
        所以在 Service 层“创建分页容器”这个动作上，用具体类型更直观。
        而 IPage<UserInfo> 更适合用在方法返回值上，
        这样外部只需要知道返回的是“分页结果接口”，不关心具体实现类。

        可以理解为：
        局部变量：用 Page，因为你确实是在创建 Page。
        方法返回值：用 IPage，因为对外暴露接口更抽象。
         */


        /*
        针对单表查询 + 简单查询条件，不用Mapper + XML文件
        直接在Service层实现了即可，维护和小号性能都更少。
         */

        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();

        if(queryVo != null) { //如果 所有的查询条件都是null，那么就不执行分页查询了。这fair enough。
            queryWrapper.like(queryVo.getPhone() != null,UserInfo::getPhone,queryVo.getPhone())
                    .eq(queryVo.getStatus() != null,UserInfo::getStatus,queryVo.getStatus());
        }
        return this.page(page,queryWrapper); //page()是Service层预定义的方法（快捷方式）

//        if(queryVo.getPhone() == null || queryVo.getStatus() == null) {
//            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
//        }

        /*
        上面的写法是错误的，你不能用if(queryVo.getPhone() == null || queryVo.getStatus() == null)
        因为 查询条件queryVo 存在的意义，就是 ”可传可不传”，查询条件中的 成员变量 没有传任何值，并不会造成异常，这是业务逻辑允许的。

        而if条件表示：只要你有任何一个条件没传，都会报错。
        这是不对的。
         */

    }

    @Override
    public void updateStatusById(Long id, BaseStatus status) {
        if(id == null || status == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        LambdaUpdateWrapper<UserInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserInfo::getId, id).set(UserInfo::getStatus, status);
        boolean update = this.update(updateWrapper);

        if(!update) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
    }
}




