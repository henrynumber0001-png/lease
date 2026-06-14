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
        if (id == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        RoomInfo roomInfo = this.getById(id);
        if (roomInfo == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        RoomDetailVo roomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, roomDetailVo);

        if (roomInfo.getApartmentId() == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }

        ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());
        if (apartmentInfo == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }

        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);

        LambdaQueryWrapper<ApartmentLabel> apartmentLabelQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelQueryWrapper.eq(ApartmentLabel::getApartmentId, apartmentInfo.getId());
        List<ApartmentLabel> apartmentLabels = apartmentLabelService.list(apartmentLabelQueryWrapper);
        List<LabelInfo> labelInfoList = new ArrayList<>();
        if (!apartmentLabels.isEmpty()) {
            List<Long> labelIds = apartmentLabels.stream()
                    .map(ApartmentLabel::getLabelId)
                    .collect(Collectors.toList());
            labelInfoList = labelInfoService.listByIds(labelIds);
        }
        apartmentItemVo.setLabelInfoList(labelInfoList);

        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemId, apartmentInfo.getId())
                .eq(GraphInfo::getItemType, ItemType.APARTMENT);

        //MyBatis-Plus 的 list(...) 正常情况下查询不到数据会返回空集合 []，不会返回 null。
        List<GraphInfo> graphInfos = graphInfoService.list(graphInfoQueryWrapper);

        List<GraphVo> graphVoList = new ArrayList<>();

        //如果graphInfos.isEmpty()，那么forEach不会执行，也不会报错
        //因此就不需要额外再写一个if (!graphInfos.isEmpty())判断了
        graphInfos.forEach(graphInfo -> {
            GraphVo graphVo = new GraphVo();
            graphVo.setName(graphInfo.getName());
            graphVo.setUrl(graphInfo.getUrl());
            graphVoList.add(graphVo);
        });
        apartmentItemVo.setGraphVoList(graphVoList);

        /*
        方法一：用到了比较器
        LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoQueryWrapper.eq(RoomInfo::getApartmentId, apartmentInfo.getId());

        if (!roomInfos.isEmpty()) {
                List<BigDecimal> sortedRents = roomInfos.stream()
                        .sorted(Comparator.comparing(RoomInfo::getRent))
                        .map(RoomInfo::getRent)
                        .collect(Collectors.toList());

                BigDecimal minRent = sortedRents.get(0);
                apartmentItemVo.setMinRent(minRent);
        }
         */

        //方法二：使用mapper.xml 通过Min(rent)聚合函数操作
        BigDecimal minRent = roomInfoMapper.selectMinRentByApartmentId(roomInfo.getApartmentId());
        apartmentItemVo.setMinRent(minRent);
        //即使 minRent == null，也是合法的，允许属性为null
        //只要满足条件的记录中至少有一个非 NULL 的 rent，MIN(rent) 就会返回最小的非空值。
        //Min(rent)会自动忽略 null


        roomDetailVo.setApartmentItemVo(apartmentItemVo);

        LambdaQueryWrapper<GraphInfo> graphInfoRoomQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoRoomQueryWrapper.eq(GraphInfo::getItemId, id)
                .eq(GraphInfo::getItemType, ItemType.ROOM);
        List<GraphInfo> graphInfosByRoomId = graphInfoService.list(graphInfoRoomQueryWrapper);

        List<GraphVo> graphVoListByRoomId = new ArrayList<>();
        if (!graphInfosByRoomId.isEmpty()) {
            graphInfosByRoomId.forEach(graphInfo -> {
                GraphVo graphVo = new GraphVo();
                graphVo.setName(graphInfo.getName());
                graphVo.setUrl(graphInfo.getUrl());
                graphVoListByRoomId.add(graphVo);
            });
        }
        roomDetailVo.setGraphVoList(graphVoListByRoomId);

        LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
        roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, id);
        List<RoomAttrValue> roomAttrValues = roomAttrValueService.list(roomAttrValueQueryWrapper);

        List<AttrValueVo> attrValueVoList = new ArrayList<>();
        if (!roomAttrValues.isEmpty()) {
            List<Long> attrValueIds = roomAttrValues.stream()
                    .map(RoomAttrValue::getAttrValueId)
                    .collect(Collectors.toList());
            List<AttrValue> attrValues = attrValueService.listByIds(attrValueIds);

            if (!attrValues.isEmpty()) {
                List<Long> attrKeyIds = attrValues.stream()
                        .map(AttrValue::getAttrKeyId)
                        .distinct()
                        .collect(Collectors.toList());

                List<AttrKey> attrKeys = attrKeyService.listByIds(attrKeyIds);
                Map<Long, String> attrKeyMap = attrKeys.stream().collect(Collectors.toMap(AttrKey::getId, AttrKey::getName));

                attrValues.forEach(attrValue -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(attrValue, attrValueVo);
                    attrValueVo.setAttrKeyName(attrKeyMap.get(attrValue.getAttrKeyId()));
                    attrValueVoList.add(attrValueVo);
                });
            }
        }
        roomDetailVo.setAttrValueVoList(attrValueVoList);

        LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, id);
        List<RoomFacility> roomFacilities = roomFacilityService.list(roomFacilityQueryWrapper);

        List<FacilityInfo> facilityInfoList = new ArrayList<>();
        if (!roomFacilities.isEmpty()) {
            List<Long> facilityIds = roomFacilities.stream()
                    .map(RoomFacility::getFacilityId)
                    .collect(Collectors.toList());
            facilityInfoList = facilityInfoService.listByIds(facilityIds);
        }
        roomDetailVo.setFacilityInfoList(facilityInfoList);

        LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelQueryWrapper.eq(RoomLabel::getRoomId, id);
        List<RoomLabel> roomLabelList = roomLabelService.list(roomLabelQueryWrapper);

        List<LabelInfo> labelInfoListByRoomId = new ArrayList<>();
        if (!roomLabelList.isEmpty()) {
            List<Long> labelIds = roomLabelList.stream()
                    .map(RoomLabel::getLabelId)
                    .collect(Collectors.toList());
            labelInfoListByRoomId = labelInfoService.listByIds(labelIds);
        }
        roomDetailVo.setLabelInfoList(labelInfoListByRoomId);

        LambdaQueryWrapper<RoomPaymentType> rptQueryWrapper = new LambdaQueryWrapper<>();
        rptQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        List<RoomPaymentType> roomPaymentTypes = roomPaymentTypeService.list(rptQueryWrapper);

        List<PaymentType> paymentTypeList = new ArrayList<>();
        if (!roomPaymentTypes.isEmpty()) {
            List<Long> paymentTypeIds = roomPaymentTypes.stream()
                    .map(RoomPaymentType::getPaymentTypeId)
                    .collect(Collectors.toList());
            paymentTypeList = paymentTypeService.listByIds(paymentTypeIds);
        }
        roomDetailVo.setPaymentTypeList(paymentTypeList);

        //List<FeeValueVo> feeValueVoList;
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, apartmentInfo.getId());
        List<ApartmentFeeValue> apartmentFeeValues = apartmentFeeValueService.list(apartmentFeeValueQueryWrapper);

        List<FeeValueVo> feeValueVoList = new ArrayList<>();
        if (!apartmentFeeValues.isEmpty()) {
            List<Long> feeValueIds = apartmentFeeValues.stream()
                    .map(ApartmentFeeValue::getFeeValueId)
                    .collect(Collectors.toList());
            List<FeeValue> feeValues = feeValueService.listByIds(feeValueIds);

            if (!feeValues.isEmpty()) {
                List<Long> feeKeyIds = feeValues.stream()
                        .map(FeeValue::getFeeKeyId)
                        .distinct()
                        .collect(Collectors.toList());

                List<FeeKey> feeKeys = feeKeyService.listByIds(feeKeyIds);
                Map<Long, String> feeKeyMap = feeKeys.stream().collect(Collectors.toMap(FeeKey::getId, FeeKey::getName));

                feeValues.forEach(feeValue -> {
                    FeeValueVo feeValueVo = new FeeValueVo();
                    BeanUtils.copyProperties(feeValue, feeValueVo);
                    feeValueVo.setFeeKeyName(feeKeyMap.get(feeValue.getFeeKeyId()));
                    feeValueVoList.add(feeValueVo);
                });
            }
        }
        roomDetailVo.setFeeValueVoList(feeValueVoList);

        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        List<RoomLeaseTerm> roomLeaseTerms = roomLeaseTermService.list(roomLeaseTermQueryWrapper);

        List<LeaseTerm> leaseTermList = new ArrayList<>();
        if (!roomLeaseTerms.isEmpty()) {
            List<Long> leaseTermIds = roomLeaseTerms.stream()
                    .map(RoomLeaseTerm::getLeaseTermId)
                    .collect(Collectors.toList());
            leaseTermList = leaseTermService.listByIds(leaseTermIds);
        }
        roomDetailVo.setLeaseTermList(leaseTermList);

        return roomDetailVo;
    }

    @Override
    public IPage<RoomItemVo> pageItemByApartmentId(long current, long size, Long apartmentId) {
        Page<RoomItemVo> page = new Page<>(current, size);

        if(apartmentId == null){
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        return roomInfoMapper.pageItemByApartmentId(page, apartmentId);
    }


}


