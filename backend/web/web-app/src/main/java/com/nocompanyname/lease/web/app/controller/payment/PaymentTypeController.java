package com.nocompanyname.lease.web.app.controller.payment;


import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.model.entity.PaymentType;
import com.nocompanyname.lease.model.entity.RoomPaymentType;
import com.nocompanyname.lease.web.app.service.PaymentTypeService;
import com.nocompanyname.lease.web.app.service.RoomPaymentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "支付方式接口")
@RestController
@RequestMapping("/app/payment")
public class PaymentTypeController {

    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;

    @Autowired
    private PaymentTypeService paymentTypeService;

    @Operation(summary = "根据房间id获取可选支付方式列表")
    @GetMapping("listByRoomId")
    public Result<List<PaymentType>> list(@RequestParam Long id) {
        List<PaymentType> paymentTypes = roomPaymentTypeService.listByRoomId(id);
        return Result.ok(paymentTypes);
    }

    @Operation(summary = "获取全部支付方式列表")
    @GetMapping("list")
    public Result<List<PaymentType>> list() {
        List<PaymentType> paymentTypes = paymentTypeService.list();
        return Result.ok(paymentTypes);
    }
}
