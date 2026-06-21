package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.SystemPost;
import com.nocompanyname.lease.model.entity.SystemUser;
import com.nocompanyname.lease.model.enums.BaseStatus;
import com.nocompanyname.lease.web.admin.mapper.SystemUserMapper;
import com.nocompanyname.lease.web.admin.service.SystemPostService;
import com.nocompanyname.lease.web.admin.service.SystemUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.admin.vo.system.user.SystemUserItemVo;
import com.nocompanyname.lease.web.admin.vo.system.user.SystemUserQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author liubo
 * @description 针对表【system_user(员工信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser>
        implements SystemUserService {

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Autowired
    private SystemPostService systemPostService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public IPage<SystemUserItemVo> getPage(long current, long size, SystemUserQueryVo queryVo) {

        //先创建空的分页容器
        Page<SystemUserItemVo> page = new Page<>(current,size);

        //只要是涉及到复合类（携带多表信息的Vo类），那么就要用Mapper.xml去写SQL查询，不然没办法在SystemUserService中返回SystemUserItemVo
        return systemUserMapper.getPage(page,queryVo);

    }

    @Override
    public SystemUserItemVo getByUserId(Long id) {


        if(id == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        SystemUser systemUser = this.getById(id);
        if(systemUser == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        SystemUserItemVo systemUserItemVo = new SystemUserItemVo();
        BeanUtils.copyProperties(systemUser,systemUserItemVo);

        SystemPost systemPost = systemPostService.getById(systemUser.getPostId());
        systemUserItemVo.setPostName(systemPost.getName());

        return systemUserItemVo;
    }

    @Override
    public void saveOrUpdateByUser(SystemUser systemUser) {

        if(systemUser.getId() == null) {//说明是 新增
            //新增的User信息是必须包含密码的
            if(StringUtils.hasText(systemUser.getPassword())) {//如果密码不是null 且 非空字符串（且一定有一个非空白字符）
                //有密码，就用BCrypt转换
                systemUser.setPassword(passwordEncoder.encode(systemUser.getPassword()));
                this.save(systemUser);

            }else{
                throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
            }
        }else { //说明是 更新
            //更新时，密码不是必填项，因为getById查询，不返回密码，所以不要求password必填
            //那么就要做一个非空判断（空了不报错，但要设置一下password的值为null，表示不更新密码属性; 非空就要转换一下密码）
            //大前提：还是要先判断一下传入的systemUser信息的id，在不在表里（虽然它不是null，但也有可能不匹配任何一个已有的id）
            SystemUser systemUserById = this.getById(systemUser.getId());
            if(systemUserById == null) {
                throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
            }

            //接下来继续判断：不要求一定有密码，但当传了密码的时候，一定要给它用BCrypt转换
            if(StringUtils.hasText(systemUser.getPassword())) {
                systemUser.setPassword(passwordEncoder.encode(systemUser.getPassword()));
            }else{ //也就是没传密码，这种情况不报错
                //MyBatis-Plus 默认情况下，updateById 不会更新 值=null 的成员变量。
                    /*
                    所以 password = null 的意思不是“把数据库密码改成 null”，
                    而是这次更新跳过 password 字段，保留数据库里原来的密码。
                     */
                systemUser.setPassword(null);
            }
            //记住，Service层的更新，没有update()，只有updateById(T entity)
            this.updateById(systemUser);
        }
        /*
        注册时用 encode()，登录时用 matches()。
        encode() 每次都会生成新 salt，所以结果不同；
        matches() 会读取数据库 hash 里的旧 salt，再验证用户输入是否正确。
         */
    }

    @Override
    public void removeByUserId(Long id) {
        if(id == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        boolean remove = this.removeById(id);
        if(!remove) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
    }

    @Override
    public void updateStatusByUserId(Long id, BaseStatus status) {
        if(id == null || status == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        LambdaUpdateWrapper<SystemUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(SystemUser::getId,id)
                .set(SystemUser::getStatus,status);
        boolean update = this.update(updateWrapper);
        if(!update) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
    }

    @Override
    public boolean isUsernameAvailabe(String username) {

        if(!StringUtils.hasText(username)) { //String的 空/非空 判断 要用StringUtils.hasText()
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        /*
        然后注意：这个用户名的查重验证，和密码的更新是一样的逻辑
        即，不强制。
        用户名相同，在前端通过代码添加提示“用户名已存在”即可
        也就是说，这个不是一种异常，不用抛异常。
         */

        LambdaQueryWrapper<SystemUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemUser::getUsername,username);
        long count = this.count(queryWrapper);
        return count == 0;
        /*
        count == 0
        等价于
        if(count == 0){
            return true;
        } else {
            return false;
        }

        总结：count == 0 是一种简写了。
        当返回false给前端，前端会自己通过写JS代码提示用户：该用户名重复
         */

    }
}




