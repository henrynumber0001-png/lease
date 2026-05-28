package com.nocompanyname.lease.web.admin.vo.apartment;


import com.nocompanyname.lease.model.entity.ApartmentInfo;
import com.nocompanyname.lease.web.admin.vo.graph.GraphVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Schema(description = "公寓信息")
@Data
public class ApartmentSubmitVo extends ApartmentInfo {

    /*
    id、name、provinceId、cityId...保存到 apartment_info 表

    facilityIds保存到 apartment_facility 表

    labelIds保存到 apartment_label 表

    feeValueIds保存到 apartment_fee_value 表

    graphVoList保存到 graph_info 表
     */

    @Schema(description="公寓配套id")
    private List<Long> facilityIds;

    @Schema(description="公寓标签id")
    private List<Long> labelIds;

    @Schema(description="公寓杂费值id")
    private List<Long> feeValueIds;

    @Schema(description="公寓图片id")
    private List<GraphVo> graphVoList;

    /*
    关于VO类，准确的说，是前端提交数据时使用的 Java 接收对象。
    你可以把它理解成 数据库多表查询时候 出现的 虚拟表（相似原理）

    前端在发送请求的时候，会把关联的多张表的数据一起发送。
    但是这些数据的呈现形式，无法用完整的数据库表直观显示，
    只能做一个 中间件（Java对象），
    中间件 继承 的父类(ApartmentInfo)就是 主表(apartment_info)
    中间件(VO类)的成员变量，可以是 关联表，例如List<FeeValue>，
    也可以是关联表中的字段单独拆出来，成为一个字段，例如List<Long> labelIds。
    就是ApartmentLabel(apartment_label)中的 labelId 成员变量

    之所以可以这么做，是因为多个 成员变量所在的关联表中，有共同的外键(apartmentId)
     */

    /*
    VO类的作用：
    接收前端提交的复合数据
    不直接对应数据库表
    用于一次性保存多张表的数据（但不一定是完整的多张表的结构，也可以是提取公因式的结构，例如本类中的成员变量，都是从不同的关联表中提取出来的）
     */

}
