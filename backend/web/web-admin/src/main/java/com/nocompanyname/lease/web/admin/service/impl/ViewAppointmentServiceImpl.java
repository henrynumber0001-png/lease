package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.ViewAppointment;
import com.nocompanyname.lease.model.enums.AppointmentStatus;
import com.nocompanyname.lease.web.admin.mapper.ViewAppointmentMapper;
import com.nocompanyname.lease.web.admin.service.ViewAppointmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.admin.vo.appointment.AppointmentQueryVo;
import com.nocompanyname.lease.web.admin.vo.appointment.AppointmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liubo
 * @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment>
        implements ViewAppointmentService {

    @Autowired
    private ViewAppointmentMapper viewAppointmentMapper;

    @Override
    public void updateStatusById(Long id, AppointmentStatus status) {
        if(id == null || status == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        LambdaUpdateWrapper<ViewAppointment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(ViewAppointment::getId,id)
                .set(ViewAppointment::getAppointmentStatus,status);

        boolean update = this.update(updateWrapper);

        if(!update) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

    }

    @Override
    public IPage<AppointmentVo> getPage(long current, long size, AppointmentQueryVo queryVo) {

        //首先创建一个分页器
        IPage<AppointmentVo> page = new Page<>(current,size);
        return viewAppointmentMapper.getPage(page,queryVo);
        /*
        这里有一个小注意点：
        如果你写 page = viewAppointmentMapper.getPage(page,queryVo);
        就把Mapper层的返回值写死了，它只能进入page这个变量里

        为了方便解耦，这里直接 写 return Mapper方法的调用
        因为标注了 返回值类型 = IPage<AppointmentVo>
        所以，就默认viewAppointmentMapper.getPage(page,queryVo)的返回类型 就是 IPage<AppointmentVo>

        从而实现了解耦。
         */


    }
}




