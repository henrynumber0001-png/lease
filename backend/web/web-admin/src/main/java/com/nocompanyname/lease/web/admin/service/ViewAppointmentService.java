package com.nocompanyname.lease.web.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nocompanyname.lease.model.entity.ViewAppointment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nocompanyname.lease.model.enums.AppointmentStatus;
import com.nocompanyname.lease.web.admin.vo.appointment.AppointmentQueryVo;
import com.nocompanyname.lease.web.admin.vo.appointment.AppointmentVo;

/**
* @author liubo
* @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface ViewAppointmentService extends IService<ViewAppointment> {

    void updateStatusById(Long id, AppointmentStatus status);

    IPage<AppointmentVo> getPage(long current, long size, AppointmentQueryVo queryVo);
}
