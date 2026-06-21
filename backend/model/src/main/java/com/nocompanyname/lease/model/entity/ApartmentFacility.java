package com.nocompanyname.lease.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "公寓&配套关系")
@TableName(value = "apartment_facility")
@Data
//@Builder

/*
@Data 不带 空参构造，
@Builder 带 全参构造。
因此，如果使用 @Data，不使用@Builder，那么JVM会默认创建一个空参构造
如果使用了 @Data + @Builder，那么Lombok 可能会生成一个全参构造方法，从而JVM不会再生成 空参构造
 */

/*
builder() 更多用于这些场景：
1）DTO / VO / 请求响应对象
2）字段比较多，而且需要一次性组装
3）对象希望创建后尽量不变
4）测试数据构造
5）复杂对象构造

使用建议：
1）关联表 Entity：用 new + set 更合适
2）复杂 VO / DTO：可以考虑 builder
 */
public class ApartmentFacility extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "公寓id")
    @TableField(value = "apartment_id")
    private Long apartmentId;

    @Schema(description = "设施id")
    @TableField(value = "facility_id")
    private Long facilityId;


}