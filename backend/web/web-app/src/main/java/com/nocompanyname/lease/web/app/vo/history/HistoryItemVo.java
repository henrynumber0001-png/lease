package com.nocompanyname.lease.web.app.vo.history;


import com.baomidou.mybatisplus.annotation.TableField;
import com.nocompanyname.lease.model.entity.BrowsingHistory;
import com.nocompanyname.lease.web.app.vo.graph.GraphVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "浏览历史基本信息")
public class HistoryItemVo extends BrowsingHistory {

    @Schema(description = "房间图片列表")
    private List<GraphVo> roomGraphVoList;

    @Schema(description = "房间当前是否可访问")
    private Boolean available;

    @Schema(description = "当前不可访问原因")
    private String unavailableReason;


}
