package com.nocompanyname.lease.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.nocompanyname.lease.model.enums.ItemType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Schema(description = "标签信息表")
@TableName(value = "label_info")
@Data
public class LabelInfo extends BaseEntity {

    /*
    ‼️重要：
    LabelInfo extends BaseEntity
    你要怎么理解 “继承” ？？
    在父类中的 属性，但是没有子类中写出来，就不是LabelInfo的属性了吗？？

    一定要特别注意：
    Long id 成员变量，是LabelInfo的，也就是LabelInfo 和 BaseEntity 都有这个 成员变量！！
     */

    private static final long serialVersionUID = 1L;

    @Schema(description = "类型")
    @TableField(value = "type")
    private ItemType type;

    @Schema(description = "标签名称")
    @TableField(value = "name")
    private String name;

}