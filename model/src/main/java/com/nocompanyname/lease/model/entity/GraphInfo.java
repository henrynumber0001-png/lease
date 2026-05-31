package com.nocompanyname.lease.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.nocompanyname.lease.model.enums.ItemType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Schema(description = "图片信息表")
@TableName(value = "graph_info")
@Data
public class GraphInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "图片名称")
    @TableField(value = "name")
    private String name;

    @Schema(description = "图片所属枚举常量类型（公寓or房间）")
    @TableField(value = "item_type")
    private ItemType itemType;

    @Schema(description = "图片所属对象 apartment/room 的 id主键")
    @TableField(value = "item_id")
    private Long itemId;

    @Schema(description = "图片地址")
    @TableField(value = "url")
    private String url;

}