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
    //这个地方一定要注意，也是回答了你一直以来的疑惑
    /*
    因为 ApartmentDetailVo 继承自 ApartmentInfo, 所以它自然拥有 ApartmentInfo 的全部成员变量（据为己有，是属于自己的！）
    所以虽然你只在 ApartmentDetailVo 类的成员变量中写了4个，但实际上还要包含ApartmentInfo（连通它继承的BaseEntity）中的 17个！一共21个！
    因为你最终Result<>中返回的是 ApartmentDetailVo，不是ApartmentInfo，所以如果你不把 从ApartmentInfo那里继承而来的 成员变量做get & set赋值/ copyProperties()方法复杂，
    你最后将无法得到完整字段信息的Result<ApartmentDetailVo>，因为从ApartmentInfo那里继承的17个属性的值是null，因为你没get & set / copyProperties()
     */

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
