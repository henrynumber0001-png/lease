package com.nocompanyname.lease.web.admin.vo.room;

import com.nocompanyname.lease.model.entity.ApartmentInfo;
import com.nocompanyname.lease.model.entity.RoomInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;


@Data
@Schema(description = "房间信息")
public class RoomItemVo extends RoomInfo {

    @Schema(description = "租约结束日期")
    private Date leaseEndDate;

    @Schema(description = "是已入住的状态")
    private Boolean isCheckIn;

    @Schema(description = "所属公寓信息")
    private ApartmentInfo apartmentInfo;

    /*
    注意看：
    这个Vo类，是用于向前端展示查询结果信息的。
    包含room_info, lease_agreement, apartment_info三张表中的字段。

    那么，你想想也知道，就不可能用Union查询，一定是JOIN查询。
    一定是横向拼接多个不同表中的字段到一起（主要使用LEFT JOIN 拼接可选字段到主表）

    你一定要理解Vo类是这个横向拼接的概念，才能知道代码怎么写。

    比如:
    la.id IS NOT NULL AS is_check_in
    虽说有lease_agreement表，就一定会有la.id字段，
    但因为我们使用的是LEFT JOIN，所以la.id字段是可能为NULL的，
    因为一定会有房间是不存在满足筛选条件的租约的。

    如果你不能想象到这是一个横向拼接的中间表，你就无法理解为什么 la.id 的结果 是 Java的属性 isCheckIn

    所以，满足 数据库返回1，Java操作true
    不满足 数据库返回0，Java操作false
     */

}
