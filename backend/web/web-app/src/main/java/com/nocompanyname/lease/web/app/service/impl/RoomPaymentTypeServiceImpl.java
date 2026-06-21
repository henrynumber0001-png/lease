package com.nocompanyname.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.PaymentType;
import com.nocompanyname.lease.model.entity.RoomPaymentType;
import com.nocompanyname.lease.web.app.service.PaymentTypeService;
import com.nocompanyname.lease.web.app.service.RoomPaymentTypeService;
import com.nocompanyname.lease.web.app.mapper.RoomPaymentTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class RoomPaymentTypeServiceImpl extends ServiceImpl<RoomPaymentTypeMapper, RoomPaymentType>
    implements RoomPaymentTypeService{

    @Autowired
    private PaymentTypeService paymentTypeService;

    @Override
    public List<PaymentType> listByRoomId(Long id) {

        if(id == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        LambdaQueryWrapper<RoomPaymentType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomPaymentType::getRoomId, id);
        List<RoomPaymentType> roomPaymentTypeList = this.list(queryWrapper);

        List<PaymentType> paymentTypes = new ArrayList<>();
        if(!roomPaymentTypeList.isEmpty()) {
            List<Long> paymentTypeIds = roomPaymentTypeList.stream().map(RoomPaymentType::getPaymentTypeId).collect(Collectors.toList());
            List<PaymentType> paymentTypeByIds = paymentTypeService.listByIds(paymentTypeIds);
            paymentTypes = paymentTypeByIds;
        }
        return paymentTypes;
    }
}




