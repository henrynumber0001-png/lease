package com.nocompanyname.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.*;
import com.nocompanyname.lease.model.enums.ItemType;
import com.nocompanyname.lease.web.app.mapper.RoomInfoMapper;
import com.nocompanyname.lease.web.app.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.app.vo.apartment.ApartmentItemVo;
import com.nocompanyname.lease.web.app.vo.attr.AttrValueVo;
import com.nocompanyname.lease.web.app.vo.fee.FeeValueVo;
import com.nocompanyname.lease.web.app.vo.graph.GraphVo;
import com.nocompanyname.lease.web.app.vo.room.RoomDetailVo;
import com.nocompanyname.lease.web.app.vo.room.RoomItemVo;
import com.nocompanyname.lease.web.app.vo.room.RoomQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private ApartmentLabelService apartmentLabelService;

    @Autowired
    private LabelInfoService labelInfoService;

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private RoomAttrValueService roomAttrValueService;

    @Autowired
    private AttrValueService attrValueService;

    @Autowired
    private AttrKeyService attrKeyService;

    @Autowired
    private RoomFacilityService roomFacilityService;

    @Autowired
    private FacilityInfoService facilityInfoService;

    @Autowired
    private RoomLabelService roomLabelService;

    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;

    @Autowired
    private PaymentTypeService paymentTypeService;

    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;

    @Autowired
    private FeeValueService feeValueService;

    @Autowired
    private FeeKeyService feeKeyService;
    @Autowired
    private RoomLeaseTermService roomLeaseTermService;
    @Autowired
    private LeaseTermService leaseTermService;


    @Override
    public IPage<RoomItemVo> getPageItem(long current, long size, RoomQueryVo queryVo) {
        // Page must be passed to the mapper so the MyBatis-Plus interceptor can add pagination.
        Page<RoomItemVo> page = new Page<>(current, size);
        if (queryVo == null) {
            queryVo = new RoomQueryVo();
        }
        return roomInfoMapper.getPageItem(page, queryVo);
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {

        if(id == null){
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        RoomDetailVo roomDetailVo = new RoomDetailVo();

        //ApartmentItemVo apartmentItemVo;
        RoomInfo roomInfo = this.getById(id);
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());

        if(apartmentInfo == null){
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);


        //List<LabelInfo> labelInfoList;
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelQueryWrapper.eq(ApartmentLabel::getApartmentId,apartmentInfo.getId());

        List<ApartmentLabel> apartmentLabels = apartmentLabelService.list(apartmentLabelQueryWrapper);

        List<LabelInfo> labelInfoList = new ArrayList<>();
        if(!apartmentLabels.isEmpty()){
            List<Long> labelIds = apartmentLabels.stream().map(ApartmentLabel::getLabelId).collect(Collectors.toList());
            labelInfoList = labelInfoService.listByIds(labelIds);
        }
        apartmentItemVo.setLabelInfoList(labelInfoList);

        //List<GraphVo> graphVoList;
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemId, apartmentInfo.getId())
                .eq(GraphInfo::getItemType, ItemType.APARTMENT);

        List<GraphInfo> graphInfos = graphInfoService.list(graphInfoQueryWrapper);

        List<GraphVo> graphVoList = new ArrayList<>();
        if(!graphInfos.isEmpty()){
            graphInfos.forEach(graphInfo -> {
                GraphVo graphVo = new GraphVo();
                graphVo.setName(graphInfo.getName());
                graphVo.setUrl(graphInfo.getUrl());
                graphVoList.add(graphVo);
            });
        }
        apartmentItemVo.setGraphVoList(graphVoList);

        //minRent (先查询出这个公寓里所有的房间list，找到里面Rent最小的）
        LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoQueryWrapper.eq(RoomInfo::getApartmentId, apartmentInfo.getId());
        List<RoomInfo> roomInfos = this.list(roomInfoQueryWrapper);

        if(!roomInfos.isEmpty()){
            List<BigDecimal> sortedRents = roomInfos.stream()
                    .sorted(Comparator.comparing(RoomInfo::getRent))
                    .map(RoomInfo::getRent)
                    .collect(Collectors.toList());
            BigDecimal minRent = sortedRents.get(0);
            apartmentItemVo.setMinRent(minRent);
        }
        roomDetailVo.setApartmentItemVo(apartmentItemVo);
        /*
        comparing() 是 compare() 的一种“模板”,
        Comparator.comparing(RoomInfo::getRent
        等价于 (o1,o2) -> o1.getRent().compareTo(o2.getRent())
        它是Comparator接口的静态方法，用于模板化一个方法体是compareTo()的compare()方法
        而BigDecimal类型恰巧不能用传统的compare()方法的o1-o2进行比较，
        并且BigDecimal类本身已经实现了Comparable接口，所以可以直接使用专门的compareTo()方法。

        Java的两种比较方式：
        ① Comparable：对象自己知道怎么比；
        ② Comparator：外部指定比较规则（o1-o2/o2-o1/compareTo()）;
         */


        //List<GraphVo> graphVoList;
        LambdaQueryWrapper<GraphInfo> graphInfoRoomQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoRoomQueryWrapper.eq(GraphInfo::getItemId, id)
                .eq(GraphInfo::getItemType, ItemType.ROOM);
        List<GraphInfo> graphInfosByRoomId = graphInfoService.list(graphInfoRoomQueryWrapper);

        List<GraphVo> graphVoListByRoomId = new ArrayList<>();
        if(!graphInfosByRoomId.isEmpty()){
            graphInfosByRoomId.forEach(graphInfo -> {
                GraphVo graphVo = new GraphVo();
                graphVo.setName(graphInfo.getName());
                graphVo.setUrl(graphInfo.getUrl());
                graphVoListByRoomId.add(graphVo);
            });
            roomDetailVo.setGraphVoList(graphVoListByRoomId);
        }

        //List<AttrValueVo> attrValueVoList;
        LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
        roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, id);

        List<RoomAttrValue> roomAttrValues = roomAttrValueService.list(roomAttrValueQueryWrapper);

        if(!roomAttrValues.isEmpty()){
            List<Long> attrValueIds = roomAttrValues.stream()
                    .map(RoomAttrValue::getAttrValueId)
                    .collect(Collectors.toList());

            List<AttrValue> attrValues = attrValueService.listByIds(attrValueIds);

            List<Long> attrKeyIds = attrValues.stream().map(AttrValue::getAttrKeyId).collect(Collectors.toList());
            List<AttrKey> attrKeys = attrKeyService.listByIds(attrKeyIds);
            Map<Long, String> attrKeyMap = attrKeys.stream().collect(Collectors.toMap(AttrKey::getId, AttrKey::getName));


            List<AttrValueVo> attrValueVoList = new ArrayList<>();
            attrValues.forEach(attrValue -> {
                AttrValueVo attrValueVo = new AttrValueVo();
                BeanUtils.copyProperties(attrValue, attrValueVo);
                attrValueVo.setAttrKeyName(attrKeyMap.get(attrValue.getAttrKeyId()));
                attrValueVoList.add(attrValueVo);
                    }
            );
            roomDetailVo.setAttrValueVoList(attrValueVoList);
        }

        //List<FacilityInfo> facilityInfoList;
        LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, id);

        List<RoomFacility> roomFacilities = roomFacilityService.list(roomFacilityQueryWrapper);

        List<FacilityInfo> facilityInfoList = new ArrayList<>();
        if(!roomFacilities.isEmpty()){
            List<Long> facilityIds = roomFacilities.stream().map(RoomFacility::getFacilityId).collect(Collectors.toList());
            facilityInfoList = facilityInfoService.listByIds(facilityIds);
        }
        roomDetailVo.setFacilityInfoList(facilityInfoList);

        //List<LabelInfo> labelInfoList;
        LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelQueryWrapper.eq(RoomLabel::getRoomId, id);
        List<RoomLabel> roomLabelList = roomLabelService.list(roomLabelQueryWrapper);

        List<LabelInfo> labelInfoListByRoomId = new ArrayList<>();
        if(!roomLabelList.isEmpty()){
            labelInfoListByRoomId = labelInfoService
                    .listByIds(roomLabelList
                            .stream()
                            .map(RoomLabel::getLabelId)
                            .collect(Collectors.toList()));
        }
        roomDetailVo.setLabelInfoList(labelInfoListByRoomId);

        //List<PaymentType> paymentTypeList
        LambdaQueryWrapper<RoomPaymentType> rptQueryWrapper = new LambdaQueryWrapper<>();
        rptQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        List<RoomPaymentType> roomPaymentTypes = roomPaymentTypeService.list(rptQueryWrapper);

        List<PaymentType> paymentTypeList = new ArrayList<>();
        if(!roomPaymentTypes.isEmpty()){
            List<Long> paymentTypeIds = roomPaymentTypes.stream().map(RoomPaymentType::getPaymentTypeId).collect(Collectors.toList());
            paymentTypeList = paymentTypeService.listByIds(paymentTypeIds);
        }
        roomDetailVo.setPaymentTypeList(paymentTypeList);

        //List<FeeValueVo> feeValueVoList;
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, apartmentInfo.getId());
        List<ApartmentFeeValue> apartmentFeeValues = apartmentFeeValueService.list(apartmentFeeValueQueryWrapper);

        List<FeeValueVo> feeValueVoList = new ArrayList<>();
        if(!apartmentFeeValues.isEmpty()){
            List<Long> feeValueIds = apartmentFeeValues.stream().map(ApartmentFeeValue::getFeeValueId).collect(Collectors.toList());
            List<FeeValue> feeValues = feeValueService.listByIds(feeValueIds);

            List<Long> feeKeyIds = feeValues.stream().map(FeeValue::getFeeKeyId).collect(Collectors.toList());
            List<FeeKey> feeKeys = feeKeyService.listByIds(feeKeyIds);
            Map<Long, String> feeKeyMap = feeKeys.stream().collect(Collectors.toMap(FeeKey::getId, FeeKey::getName));

            feeValues.forEach(feeValue -> {
                FeeValueVo feeValueVo = new FeeValueVo();
                BeanUtils.copyProperties(feeValue, feeValueVo);
                feeValueVo.setFeeKeyName(feeKeyMap.get(feeValue.getFeeKeyId()));
                feeValueVoList.add(feeValueVo);
            });
            roomDetailVo.setFeeValueVoList(feeValueVoList);

            //List<LeaseTerm> leaseTermList;
            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
            roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);

            List<RoomLeaseTerm> roomLeaseTerms = roomLeaseTermService.list(roomLeaseTermQueryWrapper);

            List<LeaseTerm> leaseTermList = new ArrayList<>();
            if(!roomLeaseTerms.isEmpty()){
                List<Long> leaseTermIds = roomLeaseTerms.stream().map(RoomLeaseTerm::getLeaseTermId).collect(Collectors.toList());
                leaseTermList = leaseTermService.listByIds(leaseTermIds);
            }

            roomDetailVo.setLeaseTermList(leaseTermList);

        }
        return roomDetailVo;
    }
}


