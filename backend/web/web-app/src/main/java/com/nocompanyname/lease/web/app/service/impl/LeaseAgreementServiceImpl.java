package com.nocompanyname.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.*;
import com.nocompanyname.lease.model.enums.ItemType;
import com.nocompanyname.lease.model.enums.LeaseSourceType;
import com.nocompanyname.lease.model.enums.LeaseStatus;
import com.nocompanyname.lease.web.app.context.LoginUserHolder;
import com.nocompanyname.lease.web.app.mapper.LeaseAgreementMapper;
import com.nocompanyname.lease.web.app.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.app.vo.agreement.AgreementDetailVo;
import com.nocompanyname.lease.web.app.vo.agreement.AgreementItemVo;
import com.nocompanyname.lease.web.app.vo.agreement.AgreementRenewVo;
import com.nocompanyname.lease.web.app.vo.graph.GraphVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {

    private final RoomInfoService roomInfoService;
    private final GraphInfoService graphInfoService;
    private final ApartmentInfoService apartmentInfoService;
    private final LeaseTermService leaseTermService;
    private final PaymentTypeService paymentTypeService;


    public LeaseAgreementServiceImpl(RoomInfoService roomInfoService, GraphInfoService graphInfoService,
                                     ApartmentInfoService apartmentInfoService, LeaseTermService leaseTermService,
                                     PaymentTypeService paymentTypeService) {
        this.roomInfoService = roomInfoService;
        this.graphInfoService = graphInfoService;
        this.apartmentInfoService = apartmentInfoService;
        this.leaseTermService = leaseTermService;
        this.paymentTypeService = paymentTypeService;
    }

    @Override
    public List<AgreementItemVo> listItem() {
        String phone = LoginUserHolder.getUserPhone();

        if (!StringUtils.hasText(phone)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }

        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getPhone, phone);
        List<LeaseAgreement> leaseAgreements = this.list(queryWrapper);

        //LeaseAgreement + RoomInfo + GraphInfo
        List<AgreementItemVo> agreementItemVos = new ArrayList<>();
        if (!leaseAgreements.isEmpty()) {
            List<Long> roomIds = leaseAgreements.stream().map(LeaseAgreement::getRoomId).collect(Collectors.toList());
            List<RoomInfo> roomInfos = roomInfoService.listByIds(roomIds);

            //roomNumber(roomId)
            Map<Long, String> mapRoomNumber = roomInfos.stream().collect(Collectors.toMap(RoomInfo::getId, RoomInfo::getRoomNumber));

            //apartmentName(apartmentId)
            List<Long> apartmentIds = roomInfos.stream().map(RoomInfo::getApartmentId).collect(Collectors.toList());
            List<ApartmentInfo> apartmentInfos = apartmentInfoService.listByIds(apartmentIds);

            Map<Long, String> mapApartmentName = apartmentInfos.stream().collect(Collectors.toMap(ApartmentInfo::getId, ApartmentInfo::getName));

            //List<GraphVo> roomGraphVoList(roomId)
            Map<Long, List<GraphVo>> mapGraphVo = new HashMap<>();
            roomIds.forEach(roomId -> {
                LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
                graphInfoQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM)
                        .eq(GraphInfo::getItemId, roomId);
                List<GraphInfo> graphInfos = graphInfoService.list(graphInfoQueryWrapper);
                graphInfos.forEach(graphInfo -> {
                    GraphVo graphVo = new GraphVo();
                    BeanUtils.copyProperties(graphInfo, graphVo);

                    if (!mapGraphVo.containsKey(roomId)) {
                        mapGraphVo.put(roomId, new ArrayList<>());
                    }
                    mapGraphVo.get(roomId).add(graphVo); //其实这一步就是 List<GraphVo> graphVos
                });
                //这个forEach循环之后，得到的是一个完整的map键值对，键是roomId，值是List<GraphVo> graphVos
            });

            leaseAgreements.forEach(leaseAgreement -> {
                AgreementItemVo agreementItemVo = new AgreementItemVo();
                BeanUtils.copyProperties(leaseAgreement, agreementItemVo);

                agreementItemVo.setRoomNumber(mapRoomNumber.get(leaseAgreement.getRoomId()));
                agreementItemVo.setApartmentName(mapApartmentName.get(leaseAgreement.getApartmentId()));
                agreementItemVo.setRoomGraphVoList(mapGraphVo.get(leaseAgreement.getRoomId()));

                agreementItemVos.add(agreementItemVo);
            });
        }
        return agreementItemVos;
    }

    @Override
    public AgreementDetailVo getDetailById(Long id) {

        //leaseAgreement
        if (id == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        String phone = LoginUserHolder.getUserPhone();
        if(!StringUtils.hasText(phone)){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }

        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getId, id)
                .eq(LeaseAgreement::getPhone, phone);
        LeaseAgreement leaseAgreement = this.getOne(queryWrapper);
        if (leaseAgreement == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }
        AgreementDetailVo agreementDetailVo = new AgreementDetailVo();
        BeanUtils.copyProperties(leaseAgreement, agreementDetailVo);

        //String apartmentName
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(leaseAgreement.getApartmentId());
        //每次查询指令结束，都要记得做一个非空判断
        if (apartmentInfo == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }
        String apartmentName = apartmentInfo.getName();
        agreementDetailVo.setApartmentName(apartmentName);

        //List<GraphVo> apartmentGraphVoList
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT)
                .eq(GraphInfo::getItemId, apartmentInfo.getId());
        List<GraphInfo> apartmentGraphInfos = graphInfoService.list(graphInfoQueryWrapper); //不用做非空判断，因为空集合.forEach不会执行
        List<GraphVo> apartmentGraphVoList = new ArrayList<>();

        apartmentGraphInfos.forEach(graphInfo -> {
            GraphVo graphVo = new GraphVo();
            BeanUtils.copyProperties(graphInfo, graphVo);
            apartmentGraphVoList.add(graphVo);
        });
        agreementDetailVo.setApartmentGraphVoList(apartmentGraphVoList);


        //String roomNumber
        Long roomId = leaseAgreement.getRoomId();
        RoomInfo roomInfo = roomInfoService.getById(roomId);
        if (roomInfo == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }
        String roomNumber = roomInfo.getRoomNumber();
        agreementDetailVo.setRoomNumber(roomNumber);

        //List<GraphVo> roomGraphVoList
        LambdaQueryWrapper<GraphInfo> roomGraphInfoQueryWrapper = new LambdaQueryWrapper<>();
        roomGraphInfoQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM)
                .eq(GraphInfo::getItemId, leaseAgreement.getRoomId());
        List<GraphInfo> roomGraphInfos = graphInfoService.list(roomGraphInfoQueryWrapper);
        if (roomGraphInfos == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
            //roomGraphInfos.isEmpty()这个不需要判断，因为空集合不会执行forEach
            //但是null还是要判断的，否则会报空指针异常
        }

        List<GraphVo> roomGraphVoList = new ArrayList<>();
        roomGraphInfos.forEach(graphInfo -> {
            GraphVo graphVo = new GraphVo();
            BeanUtils.copyProperties(graphInfo, graphVo);
            roomGraphVoList.add(graphVo);
        });
        agreementDetailVo.setRoomGraphVoList(roomGraphVoList);

        //leaseTeam
        LambdaQueryWrapper<LeaseTerm> leaseTermQueryWrapper = new LambdaQueryWrapper<>();
        leaseTermQueryWrapper.eq(LeaseTerm::getId, leaseAgreement.getLeaseTermId());

        LeaseTerm leaseTerm = leaseTermService.getOne(leaseTermQueryWrapper);
        if (leaseTerm == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }
        agreementDetailVo.setLeaseTermMonthCount(leaseTerm.getMonthCount());
        agreementDetailVo.setLeaseTermUnit(leaseTerm.getUnit());

        //paymentType
        LambdaQueryWrapper<PaymentType> paymentTypeQueryWrapper = new LambdaQueryWrapper<>();
        paymentTypeQueryWrapper.eq(PaymentType::getId, leaseAgreement.getPaymentTypeId());

        PaymentType paymentType = paymentTypeService.getOne(paymentTypeQueryWrapper);
        if (paymentType == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }
        agreementDetailVo.setPaymentTypeName(paymentType.getName());

        return agreementDetailVo;
    }

    @Override
    public void updateStatusById(Long id, LeaseStatus leaseStatus) {
        if (id == null || leaseStatus == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        String phone = LoginUserHolder.getUserPhone();
        if(!StringUtils.hasText(phone)){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }

        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getId, id)
                .eq(LeaseAgreement::getPhone, phone);

        LeaseAgreement leaseAgreement = this.getOne(queryWrapper);
        if (leaseAgreement == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        LeaseStatus currentLeaseStatus = leaseAgreement.getStatus();


        if (currentLeaseStatus == LeaseStatus.SIGNING && leaseStatus == LeaseStatus.SIGNED) {
            LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(LeaseAgreement::getId, id)
                    //只按 id 更新状态，没有加当前用户手机号/用户身份条件。任何已登录用户只要知道租约 id，就可能更新别人的租约状态。
                    //这里只能用 手机号做叠加筛选条件，是因为 LeaseAgreement中 没有UserId 字段。
                    .eq(LeaseAgreement::getPhone, LoginUserHolder.getUserPhone())
                    .eq(LeaseAgreement::getStatus, currentLeaseStatus)
                    .set(LeaseAgreement::getStatus, leaseStatus);
            boolean update = this.update(updateWrapper);
            if (!update) {
                throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
            }
        } else if (currentLeaseStatus == LeaseStatus.SIGNED && leaseStatus == LeaseStatus.WITHDRAWING) {
            LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(LeaseAgreement::getId, id)
                    //只按 id 更新状态，没有加当前用户手机号/用户身份条件。任何已登录用户只要知道租约 id，就可能更新别人的租约状态。
                    //这里只能用 手机号做叠加筛选条件，是因为 LeaseAgreement中 没有UserId 字段。
                    .eq(LeaseAgreement::getPhone, LoginUserHolder.getUserPhone())
                    .eq(LeaseAgreement::getStatus, currentLeaseStatus)
                    .set(LeaseAgreement::getStatus, leaseStatus);
            boolean update = this.update(updateWrapper);
            if (!update) {
                throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
            }
        } else {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
    }

    @Override
    public void renew(AgreementRenewVo renewVo) {

        if (renewVo == null || renewVo.getOldAgreementId() == null || renewVo.getLeaseEndDate() == null
                || renewVo.getLeaseStartDate() == null || renewVo.getPaymentTypeId() == null
                || renewVo.getLeaseTermId() == null){
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        if(!renewVo.getLeaseStartDate().before(renewVo.getLeaseEndDate())){ //结束日期 =或者< 都不行
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        String phone = LoginUserHolder.getUserPhone();
        if (!StringUtils.hasText(phone)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }

        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getId, renewVo.getOldAgreementId())
                .eq(LeaseAgreement::getPhone, phone);

       LeaseAgreement oldleaseAgreement = this.getOne(queryWrapper);
       if(oldleaseAgreement == null){
           throw new LeaseException(ResultCodeEnum.DATA_ERROR);
       }

       //查看原租约状态，如果不是2，不能续约
       if(oldleaseAgreement.getStatus() != LeaseStatus.SIGNED
               || !renewVo.getLeaseStartDate().after(oldleaseAgreement.getLeaseEndDate())){
           throw new LeaseException(ResultCodeEnum.DATA_ERROR);
       }

       LambdaQueryWrapper<LeaseAgreement> leaseAgreementQueryWrapper = new LambdaQueryWrapper<>();
       leaseAgreementQueryWrapper.eq(LeaseAgreement::getPhone, phone)
               .eq(LeaseAgreement::getRoomId, oldleaseAgreement.getRoomId())
               .eq(LeaseAgreement::getSourceType,LeaseSourceType.RENEW)
               .eq(LeaseAgreement::getStatus, LeaseStatus.RENEWING);

       Long count = this.count(leaseAgreementQueryWrapper);
       if(count > 0){
           throw new LeaseException(ResultCodeEnum.DATA_ERROR);
       }

       //继续验证用户输入的租期ID和支付方式ID是否存在
        if(leaseTermService.getById(renewVo.getLeaseTermId()) == null){
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }
        if(paymentTypeService.getById(renewVo.getPaymentTypeId()) == null){
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }

        //一切检查无误，开始将前端输入和旧租约附带信息合并，带入到新租约中
        LeaseAgreement newLeaseAgreement = new LeaseAgreement();
        newLeaseAgreement.setLeaseTermId(renewVo.getLeaseTermId());
        newLeaseAgreement.setPaymentTypeId(renewVo.getPaymentTypeId());
        newLeaseAgreement.setLeaseStartDate(renewVo.getLeaseStartDate());
        newLeaseAgreement.setLeaseEndDate(renewVo.getLeaseEndDate());
        newLeaseAgreement.setAdditionalInfo(renewVo.getAdditionalInfo());

        newLeaseAgreement.setPhone(phone);
        newLeaseAgreement.setIdentificationNumber(oldleaseAgreement.getIdentificationNumber());
        newLeaseAgreement.setName(oldleaseAgreement.getName());
        newLeaseAgreement.setApartmentId(oldleaseAgreement.getApartmentId());
        newLeaseAgreement.setRoomId(oldleaseAgreement.getRoomId());
        newLeaseAgreement.setRent(oldleaseAgreement.getRent());
        newLeaseAgreement.setDeposit(oldleaseAgreement.getDeposit());
        newLeaseAgreement.setStatus(LeaseStatus.RENEWING);
        newLeaseAgreement.setSourceType(LeaseSourceType.RENEW);

        boolean result = this.save(newLeaseAgreement);
        if(!result){
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

    }


}




