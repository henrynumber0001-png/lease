package com.nocompanyname.lease.web.admin.vo.attr;

import com.nocompanyname.lease.model.entity.AttrKey;
import com.nocompanyname.lease.model.entity.AttrValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Data
public class AttrKeyVo extends AttrKey {

    @Schema(description = "属性value列表")
    private List<AttrValue> attrValueList;

    /* AttrKeyVo 继承了 AttrKey，因此自动拥有成员变量 id 和 name。
       然后AttrKeyVo类中 添加了成员变量 List<AttrValue>类型的 attrValueList，
       即，很多个 AttrValue对象。

       那么首先第一步，就需要拼一张 中间表，把 attr_key 和 attr_value 拼起来
       再执行 Controller层 的查询指令（从Mapper层到SQL）
     */
}
