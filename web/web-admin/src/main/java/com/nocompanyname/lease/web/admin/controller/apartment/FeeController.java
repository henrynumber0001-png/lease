package com.nocompanyname.lease.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.model.entity.FeeKey;
import com.nocompanyname.lease.model.entity.FeeValue;
import com.nocompanyname.lease.web.admin.service.FeeKeyService;
import com.nocompanyname.lease.web.admin.service.FeeValueService;
import com.nocompanyname.lease.web.admin.vo.fee.FeeKeyVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "房间杂费管理")
@RestController
@RequestMapping("/admin/fee")
public class FeeController {
    @Autowired
    FeeKeyService feeKeyService;
    @Autowired
    FeeValueService feeValueService;

    @Operation(summary = "保存或更新杂费名称")
    @PostMapping("key/saveOrUpdate")
    public Result saveOrUpdateFeeKey(@RequestBody FeeKey feeKey) {
        feeKeyService.saveOrUpdate(feeKey);
        return Result.ok();
    }

    @Operation(summary = "保存或更新杂费值")
    @PostMapping("value/saveOrUpdate")
    public Result saveOrUpdateFeeValue(@RequestBody FeeValue feeValue) {
        feeValueService.saveOrUpdate(feeValue);
        return Result.ok();
    }


    @Operation(summary = "查询全部杂费名称和杂费值列表")
    @GetMapping("list")
    public Result<List<FeeKeyVo>> feeInfoList() {
        List<FeeKeyVo> feeKeyVoList = feeKeyService.listFeeInfo();
        //业务需求是：查询全部杂费信息，就包含fee_key表和fee_value表拼接在一起的多条行数据，那么就必然要用包含FeeKeyVo的List来存放多行数据
        //FeeKeyVo 就是拼接了 fee_key表和fee_value表的全部字段 的一行数据
        return Result.ok(feeKeyVoList);
    }

    @Operation(summary = "根据id删除杂费名称")
    @DeleteMapping("key/deleteById")
    public Result deleteFeeKeyById(@RequestParam Long feeKeyId) {
        feeKeyService.removeById(feeKeyId);
        feeValueService.removeByFeeKeyId(feeKeyId);
        return Result.ok();
    }

    @Operation(summary = "根据id删除杂费值")
    @DeleteMapping("value/deleteById")
    public Result deleteFeeValueById(@RequestParam Long id) {
        feeValueService.removeById(id);
        return Result.ok();
    }
}
