package com.nocompanyname.lease.web.app.controller.agreement;

import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.model.entity.LeaseAgreement;
import com.nocompanyname.lease.model.enums.LeaseStatus;
import com.nocompanyname.lease.web.app.service.LeaseAgreementService;
import com.nocompanyname.lease.web.app.vo.agreement.AgreementDetailVo;
import com.nocompanyname.lease.web.app.vo.agreement.AgreementItemVo;
import com.nocompanyname.lease.web.app.vo.agreement.AgreementRenewVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/agreement")
@Tag(name = "租约信息")
public class LeaseAgreementController {

    @Autowired
    private LeaseAgreementService leaseAgreementService;

    @Operation(summary = "（根据手机号）获取个人租约基本信息列表")
    @GetMapping("listItem")

    //因为租约信息里没有设计userId，因此获取个人租约信息列表只能通过phone
    public Result<List<AgreementItemVo>> listItem() {
        List<AgreementItemVo> list = leaseAgreementService.listItem();
        return Result.ok(list);
    }

    @Operation(summary = "根据租约id获取租约详细信息")
    @GetMapping("getDetailById")
    public Result<AgreementDetailVo> getDetailById(@RequestParam Long id) {
        AgreementDetailVo agreementDetailVo = leaseAgreementService.getDetailById(id);
        return Result.ok(agreementDetailVo);
    }

    @Operation(summary = "根据租约id更新租约状态", description = "用于确认租约和提前退租")
    @PostMapping("updateStatusById")
    public Result updateStatusById(@RequestParam Long id, @RequestParam LeaseStatus leaseStatus) {
        leaseAgreementService.updateStatusById(id, leaseStatus);
        return Result.ok();
    }

    @Operation(summary = "更新租约")
    @PostMapping("renew")
    public Result renew(@RequestBody AgreementRenewVo renewVo) {
        leaseAgreementService.renew(renewVo);
        return Result.ok();
    }

}
