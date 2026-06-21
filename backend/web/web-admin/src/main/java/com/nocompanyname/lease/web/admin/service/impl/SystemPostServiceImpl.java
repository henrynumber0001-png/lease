package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.SystemPost;
import com.nocompanyname.lease.model.enums.BaseStatus;
import com.nocompanyname.lease.web.admin.service.SystemPostService;
import com.nocompanyname.lease.web.admin.mapper.SystemPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author liubo
* @description 针对表【system_post(岗位信息表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class SystemPostServiceImpl extends ServiceImpl<SystemPostMapper, SystemPost>
    implements SystemPostService{



    @Override
    public IPage<SystemPost> getPage(long current, long size) {

        //首先创建分页容器
        Page<SystemPost> page = new Page<>(current,size);
        return this.page(page);

        /*
        无查询条件、简单条件查询、单表查询，可以直接用MyBatis-Plus的预定义方法:
        Service层 page(page)/ Mapper层：selectPage(page,null)
        提高性能，减少操作

        但是只要涉及到多表，或者关联条件，比如Vo类，那么就要用Mapper.xml，
        因为 this.page()无法返回Vo类，只能返回实体类自己
         */
    }

    @Override
    public void saveOrUpdatePost(SystemPost systemPost) {
        if(systemPost.getId() == null) {
            this.save(systemPost);
        } else {
            SystemPost post = this.getById(systemPost.getId());
            if(post == null) {
                throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
            }
            this.updateById(systemPost);
        }
    }

    @Override
    public void removeByPostId(Long id) {
        if(id == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        boolean remove = this.removeById(id);
        if (!remove) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }


    }

    @Override
    public SystemPost getByPostId(Long id) {
        if(id == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        SystemPost systemPost = this.getById(id);
        if(systemPost == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        return systemPost;
    }

    @Override
    public void updateStatusByPostId(Long id, BaseStatus status) {
        if(id == null || status == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        LambdaUpdateWrapper<SystemPost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SystemPost::getId,id)
                .set(SystemPost::getStatus,status);
        boolean update = this.update(updateWrapper);
        if(!update) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
    }

}




