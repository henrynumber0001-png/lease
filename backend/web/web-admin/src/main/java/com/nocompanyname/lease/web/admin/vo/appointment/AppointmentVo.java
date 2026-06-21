package com.nocompanyname.lease.web.admin.vo.appointment;

import com.nocompanyname.lease.model.entity.ApartmentInfo;
import com.nocompanyname.lease.model.entity.ViewAppointment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "预约看房信息")
public class AppointmentVo extends ViewAppointment {

    //没写的属性不是代表没有，只是因为继承自ViewAppointment，所以省略了
    //所以，你在ResultMap中该写还是要写啊！！！

    @Schema(description = "预约公寓信息")
    private ApartmentInfo apartmentInfo;

}
