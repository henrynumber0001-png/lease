package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.*;
import com.nocompanyname.lease.model.enums.LeaseStatus;
import com.nocompanyname.lease.web.admin.mapper.LeaseAgreementMapper;
import com.nocompanyname.lease.web.admin.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.nocompanyname.lease.web.admin.vo.agreement.AgreementVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;

    @Autowired
    private RoomInfoService roomInfoService;

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private PaymentTypeService paymentTypeService;

    @Autowired
    private LeaseTermService leaseTermService;

    @Override
    public IPage<AgreementVo> getPage(long current, long size, AgreementQueryVo queryVo) {


        //首先创建一个空的分页容器
        IPage<AgreementVo> page = new Page<>(current, size);
        return leaseAgreementMapper.getPage(page, queryVo);
    }

    @Override
    public AgreementVo getByLeaseAgreementId(Long id) {

        if(id == null){
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }


        AgreementVo agreementVo = new AgreementVo();

        LeaseAgreement leaseAgreement = this.getById(id);
        if(leaseAgreement == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR); //统一把错误的返回值交给全局异常类处理，确保返回到Controller层的都是OK（正确的数值）
        }

        BeanUtils.copyProperties(leaseAgreement, agreementVo);

        //RoomInfo roomInfo
        RoomInfo roomInfo = roomInfoService.getById(leaseAgreement.getRoomId()); //业务逻辑都要在Service层实现

        //ApartmentInfo apartmentInfo
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId()); //Service -> Mapper -> SQL -> 数据库 apartment_info

        //PaymentType paymentType
        PaymentType paymentType = paymentTypeService.getById(leaseAgreement.getPaymentTypeId());

        //LeaseTerm leaseTerm
        LeaseTerm leaseTerm = leaseTermService.getById(leaseAgreement.getLeaseTermId());

        agreementVo.setRoomInfo(roomInfo);
        agreementVo.setApartmentInfo(apartmentInfo);
        agreementVo.setPaymentType(paymentType);
        agreementVo.setLeaseTerm(leaseTerm);

        return agreementVo;
    }

    @Override
    public void updateStatusById(Long id, LeaseStatus status) {
        if(id == null || status == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(LeaseAgreement::getStatus, status).eq(LeaseAgreement::getId, id);
        boolean update = this.update(updateWrapper);

        if(!update) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

    }
}




