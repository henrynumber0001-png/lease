package com.nocompanyname.lease.web.admin.vo.apartment;


import com.nocompanyname.lease.model.entity.ApartmentInfo;
import com.nocompanyname.lease.model.entity.FacilityInfo;
import com.nocompanyname.lease.model.entity.LabelInfo;
import com.nocompanyname.lease.web.admin.vo.graph.GraphVo;
import com.nocompanyname.lease.web.admin.vo.fee.FeeValueVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "公寓信息")
@Data
public class ApartmentDetailVo extends ApartmentInfo {

    @Schema(description = "图片列表")
    private List<GraphVo> graphVoList;

    @Schema(description = "标签列表")
    /*
    这个里面的说法就是：apartment_info 是通过 apartment_label 关联 label_info 的，而非直接关联的
    所以你要通过查询 apartment_label 中的 apartment_id 去获取 label_id，进而去获取 label_info 中的 type 和 name 字段
     */
    private List<LabelInfo> labelInfoList;

    @Schema(description = "配套列表")
    private List<FacilityInfo> facilityInfoList;

    @Schema(description = "杂费列表")
    private List<FeeValueVo> feeValueVoList;

}
