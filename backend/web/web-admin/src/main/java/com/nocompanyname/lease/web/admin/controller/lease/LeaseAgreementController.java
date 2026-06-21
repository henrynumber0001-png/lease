package com.nocompanyname.lease.web.admin.controller.lease;


import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.model.entity.LeaseAgreement;
import com.nocompanyname.lease.model.enums.LeaseStatus;
import com.nocompanyname.lease.web.admin.service.LeaseAgreementService;
import com.nocompanyname.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.nocompanyname.lease.web.admin.vo.agreement.AgreementVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Tag(name = "租约管理")
@RestController
@RequestMapping("/admin/agreement")
public class LeaseAgreementController {

    @Autowired
    LeaseAgreementService leaseAgreementService;

    @Operation(summary = "保存或修改租约信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody LeaseAgreement leaseAgreement) {
        leaseAgreementService.saveOrUpdate(leaseAgreement);
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询租约列表")
    @GetMapping("page")
    public Result<IPage<AgreementVo>> page(@RequestParam long current, @RequestParam long size, AgreementQueryVo queryVo) {
        IPage<AgreementVo> page = leaseAgreementService.getPage(current,size,queryVo);
        return Result.ok(page);
    }

    @Operation(summary = "根据id查询租约信息")
    @GetMapping(name = "getById")
    public Result<AgreementVo> getById(@RequestParam Long id) {
        AgreementVo agreementVo = leaseAgreementService.getByLeaseAgreementId(id);
        return Result.ok(agreementVo);
    }

    @Operation(summary = "根据id删除租约信息")
    @DeleteMapping("removeById")
    public Result removeById(@RequestParam Long id) {
        leaseAgreementService.removeById(id);
        return Result.ok();
    }
    /*
    这里解释一下，为什么根据id删除租约，就只删它实体类本身就行了。
    根本原因是 lease_agreement 是附属表，关联关系都内置了（相当于中间表已成为内置字段了）。
    所以要搞清楚 依附 和 被依附 关系。

    租约表本身通常只是引用其他表：
    lease_agreement.room_id
    lease_agreement.apartment_id
    lease_agreement.payment_type_id
    lease_agreement.lease_term_id
    这些字段是“引用别人”，不是“别人专门依附于这条租约”。

    room_info
    apartment_info
    payment_type
    lease_term
    因为这些数据还可能被其他业务继续使用。比如房间还在，公寓还在，付款方式和租期也是基础配置。

    但 RoomController 里：
    roomInfoService.removeByRoomId(id);
    删除的是房间。
    房间有很多专属于这个房间的关系记录：
    room_label
    room_facility
    room_payment_type
    room_lease_term
    room_attr_value
    graph_info(item_type=ROOM, item_id=roomId)
    这些表里的记录不是独立业务实体，而是“这个房间和标签/配套/租期/图片之间的关联”。房间删了，这些关系记录继续留着就没有意义了，会变成垃圾数据。
     */

    @Operation(summary = "根据id更新租约状态")
    @PostMapping("updateStatusById")
    public Result updateStatusById(@RequestParam Long id, @RequestParam LeaseStatus status) {
        leaseAgreementService.updateStatusById(id,status);
        return Result.ok();
    }

}

