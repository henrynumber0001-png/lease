package com.nocompanyname.lease.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.model.entity.AttrKey;
import com.nocompanyname.lease.model.entity.AttrValue;
import com.nocompanyname.lease.web.admin.service.AttrKeyService;
import com.nocompanyname.lease.web.admin.service.AttrValueService;
import com.nocompanyname.lease.web.admin.vo.attr.AttrKeyVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "房间属性管理")
@RestController
@RequestMapping("/admin/attr")
public class AttrController {
    @Autowired
    private AttrKeyService attrKeyService;
    @Autowired
    private AttrValueService attrValueService;

    @Operation(summary = "新增或更新属性名称")
    @PostMapping("key/saveOrUpdate")
    public Result saveOrUpdateAttrKey(@RequestBody AttrKey attrKey) {
        attrKeyService.saveOrUpdate(attrKey);
        return Result.ok();
    }

    @Operation(summary = "新增或更新属性值")
    @PostMapping("value/saveOrUpdate")
    public Result saveOrUpdateAttrValue(@RequestBody AttrValue attrValue) {
        attrValueService.saveOrUpdate(attrValue);
        return Result.ok();
    }


    @Operation(summary = "查询全部属性名称和属性值列表")
    @GetMapping("list")
    public Result<List<AttrKeyVo>> listAttrInfo() {
        List<AttrKeyVo> attrKeyVoList = attrKeyService.listAttrInfo(); //最终返回值（AttrKeyVo对象的集合）被带到controller层并返回给前端
        return Result.ok(attrKeyVoList);
    }

    @Operation(summary = "根据attr_key的id删除属性名称")
    @DeleteMapping("key/deleteById")
    public Result removeAttrKeyById(@RequestParam Long attrKeyId) {
        attrKeyService.removeById(attrKeyId); //根据id 删除 attr_key表中对应的行数据

        LambdaQueryWrapper<AttrValue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttrValue::getAttrKeyId, attrKeyId);
        attrValueService.remove(queryWrapper); //删除的是 attr_value 表中 attr_key_id = attrKeyId 的所有行
        /*
        Wrapper = 条件构造器
        QueryWrapper = 查询条件构造器
        作用就是在单表操作时，将sql语句中的where条件部分进行封装，提供了很多方法来设置查询条件。
         例如：eq(属性, 值) = where 属性 = 值
         还可以使用 and、or、like、in 等方法来构建更复杂的查询条件。
         通过使用 QueryWrapper，可以避免手写 SQL 语句，提高代码的可读性和维护性，同时也能防止 SQL 注入攻击。

         但同时，也要注意，QueryWrapper只是封装了查询条件的构造器，并不是代替数据库操纵语言。
         也就是最后的 增删改，
         还是要你自己手动写（把条件构造器传入）
         */

        return Result.ok();
    }

    @Operation(summary = "根据id删除属性值")
    @DeleteMapping("value/deleteById")
    public Result removeAttrValueById(@RequestParam Long id) {
        attrValueService.removeById(id);
        return Result.ok();
    }

}
