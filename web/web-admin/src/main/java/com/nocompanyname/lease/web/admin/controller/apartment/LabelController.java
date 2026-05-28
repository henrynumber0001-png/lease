package com.nocompanyname.lease.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.model.entity.LabelInfo;
import com.nocompanyname.lease.model.enums.ItemType;
import com.nocompanyname.lease.web.admin.service.LabelInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "标签管理")
@RestController
@RequestMapping("/admin/label")
public class LabelController {
    @Autowired
    LabelInfoService labelInfoService;


    @Operation(summary = "（根据类型）查询标签列表")
    @GetMapping("list")
    public Result<List<LabelInfo>> labelList(@RequestParam(required = false) ItemType type) { //前端传入一个type的值1
        LambdaQueryWrapper<LabelInfo> query = new LambdaQueryWrapper<>();
        query.eq(type != null, LabelInfo::getType, type); //当 type = null, 也就是条件查询为空，那么query = {}
        /*
        1）LabelInfo::getType -> 获取成员变量&字段 type;
        2）根据ItemTypeConverter，将type的值1 匹配找到 ItemType类型 的枚举常量APARTMENT，并自动转换成 ItemType.APARTMENT*/

        List<LabelInfo> labelInfos = labelInfoService.list(query); //因此这一条内容 等价于 labelInfoService.list()
        /*到达Mapper层，Mybatis 根据 ItemType的 @EnumValue注解，将ItemType.APARTMENT的code成员变量传入数据库(label_info)中作为type字段的值
        当返回查询值的时候，数据库返回字段type的值1，Mybatis根据 @EnumValue注解，匹配ItemType的成员变量code，将1转换成ItemType.APARTMENT对象（枚举常量），返回给controller层
        Java层永远保存的是“枚举对象”，不是数字 */

        return Result.ok(labelInfos);
        /* @JsonValue注解 发生在 Controller层返回之后：
        Controller（返回Java对象）-> Spring MVC -> HttpMessageConverter（关键）-> Jackson -> @jsonValue / getter / 注解生效 -> JSON字符串（返回给前端）
        因此，在Controller层返回 Result对象 之后，又经历了一次转换（根据 @jsonValue注解），将 type 从 ItemType.APARTMENT 对象转换成1，最终返回给前端的JSON字符串中type字段的值是1，而不是"APARTMENT"
         */
    }

    @Operation(summary = "新增或修改标签信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdateLabel(@RequestBody LabelInfo labelInfo) { //在这一步，是@JsonValue参与JSON的反序列化，扫描注解所在成员变量对应的枚举常量，将前端传入的type字段的值1，转换成ItemType.APARTMENT对象，封装到LabelInfo对象中
        labelInfoService.saveOrUpdate(labelInfo);
        return Result.ok();
    }

    @Operation(summary = "根据id删除标签信息")
    @DeleteMapping("deleteById")
    public Result deleteLabelById(@RequestParam Long id) {
        labelInfoService.removeById(id);
        return Result.ok();
    }
}
