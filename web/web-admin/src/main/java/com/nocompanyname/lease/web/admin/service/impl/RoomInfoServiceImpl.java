package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.*;
import com.nocompanyname.lease.model.enums.ItemType;
import com.nocompanyname.lease.model.enums.ReleaseStatus;
import com.nocompanyname.lease.web.admin.mapper.RoomInfoMapper;
import com.nocompanyname.lease.web.admin.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.admin.vo.attr.AttrValueVo;
import com.nocompanyname.lease.web.admin.vo.graph.GraphVo;
import com.nocompanyname.lease.web.admin.vo.room.RoomDetailVo;
import com.nocompanyname.lease.web.admin.vo.room.RoomItemVo;
import com.nocompanyname.lease.web.admin.vo.room.RoomQueryVo;
import com.nocompanyname.lease.web.admin.vo.room.RoomSubmitVo;
import io.minio.Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private RoomAttrValueService roomAttrValueService;

    @Autowired
    private RoomFacilityService roomFacilityService;

    @Autowired
    private RoomLabelService roomLabelService;

    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;

    @Autowired
    private RoomLeaseTermService roomLeaseTermService;

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private AttrValueService attrValueService;

    @Autowired
    private AttrKeyService attrKeyService;

    @Autowired
    private FacilityInfoService facilityInfoService;

    @Autowired
    private LabelInfoService labelInfoService;

    @Autowired
    private PaymentTypeService paymentTypeService;

    @Autowired
    private LeaseTermService leaseTermService;

    @Transactional
    @Override
    public void saveOrUpdateByRoom(RoomSubmitVo roomSubmitVo) {

        //首先判断是否是 新增还是更新
        boolean update = roomSubmitVo.getId() != null;

        //无论 新增还是更新，都要将RoomSubmitVo中 继承的 RoomInfo这个部分的属性先更新进去
        this.saveOrUpdate(roomSubmitVo);
        //本身就在RoomInfoService的实现类中，因此不用调用RoomInfoService了
        //为什么这一步要这样操作？
        // 因为这是一个多表信息的更新or新增操作，相当于是前端将要执行更新or新增的表信息，全部都放到Vo类中了
        // 这其中就包括了room_info表
        //所以你要理解，对于更新or新增操作，传入的Vo类，是为了让你往它包含的类中放数据用的
        //针对主表room_info，会保存roomSubmitVo中被包含的全部字段

        Long roomId = roomSubmitVo.getId();

        if (update) { //说明是更新，而不是新增

            //删除已有图片信息
            LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoQueryWrapper
                    .eq(GraphInfo::getItemType, ItemType.ROOM)
                    .eq(GraphInfo::getItemId, roomId);
            graphInfoService.remove(graphInfoQueryWrapper);

            //删除属性关系 RoomAttrValue
            LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
            roomAttrValueQueryWrapper
                    .eq(RoomAttrValue::getRoomId, roomId);
            roomAttrValueService.remove(roomAttrValueQueryWrapper); //这里删除的是roomAttrValue的成员变量的值，当然也包括roomAttrValue自己的id值

            //删除房间配套关系 RoomFacility
            LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
            roomFacilityQueryWrapper
                    .eq(RoomFacility::getRoomId, roomId);
            roomFacilityService.remove(roomFacilityQueryWrapper);


            //删除房间标签关系 RoomLabel
            LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
            roomLabelQueryWrapper
                    .eq(RoomLabel::getRoomId, roomId);
            roomLabelService.remove(roomLabelQueryWrapper);

            //删除房间支付方式关系 RoomPaymentType
            LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
            roomPaymentTypeQueryWrapper
                    .eq(RoomPaymentType::getRoomId, roomId);
            roomPaymentTypeService.remove(roomPaymentTypeQueryWrapper);

            //删除房间租期关系表 RoomLeaseTerm
            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
            roomLeaseTermQueryWrapper
                    .eq(RoomLeaseTerm::getRoomId, roomId);
            roomLeaseTermService.remove(roomLeaseTermQueryWrapper);

        }

        //删除之后，是新增/更新的后半部分（都是相同的逻辑，就是插入新的数据）

        //首先是图片 GraphInfo
        //前端传入的roomSubmitVo对象中，包含一个属性：graphVoList，get出来，用List接收
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();

        //因为最终是要往GraphInfo中放数据，所以还要创建一个对应的GraphInfo的List
        List<GraphInfo> graphInfos = new ArrayList<>();

        //遍历graphVoList，把每一个GraphVo转换成GraphInfo，最终放到graph_info表的字段中
        graphVoList.forEach(graphVo -> {
            GraphInfo graphInfo = new GraphInfo();
            graphInfo.setName(graphVo.getName());
            graphInfo.setUrl(graphVo.getUrl());
            graphInfo.setItemId(roomId);
            graphInfo.setItemType(ItemType.ROOM);
            graphInfos.add(graphInfo);
        });
        if (!graphInfos.isEmpty()) {
            graphInfoService.saveBatch(graphInfos);
        }

        //其次是属性关系 RoomAttrValue
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();

        List<RoomAttrValue> roomAttrValues = new ArrayList<>();
        attrValueIds.forEach(attrValueId -> {
            RoomAttrValue roomAttrValue = new RoomAttrValue();
            roomAttrValue.setRoomId(roomId);
            roomAttrValue.setAttrValueId(attrValueId);
            roomAttrValues.add(roomAttrValue); //不需要set roomAttrValue的id属性，因为在前面已经都删了（或者是新增），这个id就是null，到数据库中自增到
        });
        if (!roomAttrValues.isEmpty()) {
            roomAttrValueService.saveBatch(roomAttrValues);
        }

        //插入房间配套关系 RoomFacility
        List<Long> roomFacilityIds = roomSubmitVo.getFacilityIds();

        List<RoomFacility> roomFacilities = new ArrayList<>();
        roomFacilityIds.forEach(roomFacilityId -> {
            RoomFacility roomFacility = new RoomFacility();
            roomFacility.setRoomId(roomId);
            roomFacility.setFacilityId(roomFacilityId);
            roomFacilities.add(roomFacility);
        });
        if (!roomFacilities.isEmpty()) {
            roomFacilityService.saveBatch(roomFacilities);
        }

        //插入房间标签关系 RoomLabel
        List<Long> labelIds = roomSubmitVo.getLabelIds();
        List<RoomLabel> roomLabels = new ArrayList<>();
        labelIds.forEach(labelId -> {
            RoomLabel roomLabel = new RoomLabel();
            roomLabel.setRoomId(roomId);
            roomLabel.setLabelId(labelId);
            roomLabels.add(roomLabel);
        });
        if (!roomLabels.isEmpty()) {
            roomLabelService.saveBatch(roomLabels);
        }

        //插入房间支付方式关系 RoomPaymentType
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        List<RoomPaymentType> roomPaymentTypes = new ArrayList<>();
        paymentTypeIds.forEach(paymentTypeId -> {
            RoomPaymentType roomPaymentType = new RoomPaymentType();
            roomPaymentType.setRoomId(roomId);
            roomPaymentType.setPaymentTypeId(paymentTypeId);
            roomPaymentTypes.add(roomPaymentType);
        });
        if (!roomPaymentTypes.isEmpty()) {
            roomPaymentTypeService.saveBatch(roomPaymentTypes);
        }

        //插入房间租期关系 RoomLeaseTerm
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        List<RoomLeaseTerm> roomLeaseTerms = new ArrayList<>();
        leaseTermIds.forEach(leaseTermId -> {
            RoomLeaseTerm roomLeaseTerm = new RoomLeaseTerm();
            roomLeaseTerm.setRoomId(roomId);
            roomLeaseTerm.setLeaseTermId(leaseTermId);
            roomLeaseTerms.add(roomLeaseTerm);
        });
        if (!roomLeaseTerms.isEmpty()) {
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }

    }

    @Override
    public IPage<RoomItemVo> getBypageItem(long current, long size, RoomQueryVo queryVo) {

        //先创建一个 空的分页容器
        Page<RoomItemVo> page = new Page<>(current, size);
        return roomInfoMapper.getBypageItem(page, queryVo);

        /*
            1.RoomInfoServiceImpl 创建一个空的分页对象 page
            2.把这个 page 传给 RoomInfoMapper.getBypageItem(...)
            3.MyBatis-Plus 执行 SQL 后，把查询结果、总条数、页码、每页数量等信息填充到这个 page 里
            4.Mapper 返回这个已经填充好的分页对象
         */


    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        if (id == null) { //不加条件判断，getById(id)可能会报空指针异常
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR); //防止NonpointerException
        }

        RoomInfo roomInfo = this.getById(id); //这里是id != null
        if (roomInfo == null) {
            //如果id 有值，但是数据库里没有这个房间（即，匹配不到），不会报空指针异常，而是返回 结果：空/NULL（即什么也没查到，但这不是语法错误）
            //如果不写这个判断，后面任何拿着这个roomInfo再去调用其他方法时，都会报空指针异常
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        RoomDetailVo roomDetailVo = new RoomDetailVo();

        BeanUtils.copyProperties(roomInfo, roomDetailVo);

        //ApartmentInfo apartmentInfo
        //根据roomInfo中的apartment_id，查询获取apartmentInfo
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());
        roomDetailVo.setApartmentInfo(apartmentInfo);

        //List<GraphVo> graphVoList
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper
                .eq(GraphInfo::getItemType, ItemType.ROOM)
                .eq(GraphInfo::getItemId, id);
        List<GraphInfo> graphInfos = graphInfoService.list(graphInfoQueryWrapper);
        //这里的graphInfos是有可能是空列表的（不满足任查询条件）
        /*
        但不需要加if非空判断：
        因为空列表执行 forEach，只是循环 0 次。
        空列表不是“里面有一个null对象”，而是“里面没有任何对象”。
        因此循环体一次都不会执行。
        所以graphInfo.getName()根本不会被调用，不会报空指针异常。

        注意区分：graphInfos = [] 和 graphInfos = null
        前者是 正常的查询操作，无匹配结果，则返回空集合，
        后者是 异常情况，可能是底层代码/graphInfoService 注入失败等特殊问题。

        还有一种异常情况：graphInfos = [null]，这种如果调用 graphInfo.getName()，是会报空指针异常的。
        但是这两种异常情况，是极少数情况，正常的业务逻辑不必考虑（roomId过滤掉null就可以了）
         */

        List<GraphVo> graphVoList = new ArrayList<>();
        graphInfos.forEach(graphInfo -> {
            GraphVo graphVo = new GraphVo();
            graphVo.setName(graphInfo.getName());
            graphVo.setUrl(graphInfo.getUrl());
            graphVoList.add(graphVo);
        });

        roomDetailVo.setGraphVoList(graphVoList);


        //List<AttrValueVo> attrValueVoList
        LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
        roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, id);
        List<RoomAttrValue> roomAttrValueList = roomAttrValueService.list(roomAttrValueQueryWrapper);
        List<Long> attrValueIds = roomAttrValueList
                .stream()
                .map(RoomAttrValue::getAttrValueId)
                .collect(Collectors.toList());
        /*
        当List<RoomAttrValue> roomAttrValueList 是一个空列表时，
        List<Long> attrValueIds 也会是一个空列表，不会报空指针异常。

        因为空列表[].stream() 是合法的，只是流里没有任何元素。
        .map(RoomAttrValue::getAttrValueId) 不会被执行任何一次。
        .collect(Collectors.toList()); 会收集出来一个空列表 attrValueIds。
         */

        //要加入到setAttrValueVoList方法的参数
        List<AttrValueVo> attrValueVoList = new ArrayList<>();
        if (!attrValueIds.isEmpty()) {
            List<AttrValue> attrValues = attrValueService.listByIds(attrValueIds);

            //从整体到零件，用map转换
            //从id到整体，用listByIds
            List<Long> attrKeyIds = attrValues.stream().map(AttrValue::getAttrKeyId).collect(Collectors.toList());
            List<AttrKey> attrKeys = attrKeyService.listByIds(attrKeyIds);
            Map<Long, String> attrKeyMap = attrKeys.stream().collect(Collectors.toMap(AttrKey::getId, AttrKey::getName));


            attrValues.forEach(attrValue -> {

                AttrValueVo attrValueVo = new AttrValueVo();

                BeanUtils.copyProperties(attrValue, attrValueVo);
                attrValueVo.setAttrKeyName(attrKeyMap.get(attrValue.getAttrKeyId()));
                //不要用对象直接调用 attrKeyId 属性，因为@Data注解下，属性都是private的，要用get方法
                /*
                你是无法在attrValues中 遍历attrKeyMap集合的
                那么是如何获取attrKeyMap中的每一个name的值呢？
                因为map集合的值 = get(key)，而attrKey的id = attrValue的attrKeyId属性
                我们不能控制attrKeyMap，但是我们能控制每一个attrValue
                所以就是通过attrValue.getAttrKeyId()来实现对key的value的遍历的
                 */
                attrValueVoList.add(attrValueVo);
            });
        }
        roomDetailVo.setAttrValueVoList(attrValueVoList); //允许返回空集合

        //List<FacilityInfo> facilityInfoList
        LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, id);
        //已经通过设置id的过滤条件，确保不会传入null，导致空指针异常
        //但还是会出现 根据roomId查询不到数据（roomId是9999），但不会报空指针异常，而是返回空列表

        List<RoomFacility> roomFacilities = roomFacilityService.list(roomFacilityQueryWrapper);

        List<Long> facilityIds = roomFacilities.stream()
                .map(RoomFacility::getFacilityId)
                .collect(Collectors.toList());
        //这一步也是有可能返回空列表，即有roomId，但没有facilityId
        /*
        如果roomFacilities 是空集合，调用 stream() 是允许的，只是这个流里没有任何元素，不会真正执行任何一次。
        收集出来的就是一个空列表facilityIds。
         */

        List<FacilityInfo> facilityInfoList = new ArrayList<>();
        if (!facilityIds.isEmpty()) {
            facilityInfoList = facilityInfoService.listByIds(facilityIds); //如果facilityIds集合不为空，查询所有满足条件的facility_info的行数据到facilityInfoList，再set进roomDetailVo的facilityInfoList属性
        }
        roomDetailVo.setFacilityInfoList(facilityInfoList); //这样保证，无论如何都会返回查询结果给前端，即使是一个空集合

        //List<LabelInfo> labelInfoList
        LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelQueryWrapper.eq(RoomLabel::getRoomId, id);
        List<RoomLabel> roomLabels = roomLabelService.list(roomLabelQueryWrapper);
        List<Long> labelIds = roomLabels.stream()
                .map(RoomLabel::getLabelId)
                .collect(Collectors.toList());
        if (!labelIds.isEmpty()) {
            List<LabelInfo> labelInfoList = labelInfoService.listByIds(labelIds);
            roomDetailVo.setLabelInfoList(labelInfoList);
        }

        //List<PaymentType> paymentTypeList
        LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
        roomPaymentTypeQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        List<RoomPaymentType> roomPaymentTypeList = roomPaymentTypeService.list(roomPaymentTypeQueryWrapper);
        List<Long> paymentTypeIds = roomPaymentTypeList.stream()
                .map(RoomPaymentType::getPaymentTypeId)
                .collect(Collectors.toList());

        List<PaymentType> paymentTypeList = new ArrayList<>();
        if (!paymentTypeIds.isEmpty()) {
            paymentTypeList = paymentTypeService.listByIds(paymentTypeIds);
        }
        roomDetailVo.setPaymentTypeList(paymentTypeList); //允许返回空集合

        //List<LeaseTerm> leaseTermList
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermLambdaQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        List<RoomLeaseTerm> roomLeaseTermsList = roomLeaseTermService.list(roomLeaseTermLambdaQueryWrapper);
        List<Long> leaseTermIds = roomLeaseTermsList.stream()
                .map(RoomLeaseTerm::getLeaseTermId)
                .collect(Collectors.toList());

        List<LeaseTerm> leaseTermList = new ArrayList<>();
        if (!leaseTermIds.isEmpty()) {
            leaseTermList = leaseTermService.listByIds(leaseTermIds);
        }
        roomDetailVo.setLeaseTermList(leaseTermList);

        return roomDetailVo;
    }

    @Override
    public void removeByRoomId(Long id) {
        if (id == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

//        LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();
//        roomInfoQueryWrapper.eq(RoomInfo::getId, id);
//        long count = this.count(roomInfoQueryWrapper);
//
//        if(count > 0){
//            throw new LeaseException(ResultCodeEnum.DELETE_ERROR);
//        }
        //删除公寓的时候，才需要顺便考虑把房间信息也一并删除
        //你根据roomId查room_info，那肯定是一比一啊

        //首先删除room_info
        this.removeById(id);

        //再删除已有图片信息
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper
                .eq(GraphInfo::getItemType, ItemType.ROOM)
                .eq(GraphInfo::getItemId, id);
        graphInfoService.remove(graphInfoQueryWrapper);

        //删除属性关系 RoomAttrValue
        LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
        roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(roomAttrValueQueryWrapper);

        //删除属性关系 RoomAttrValue
        LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(roomFacilityQueryWrapper);

        //删除RoomLabel
        LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelQueryWrapper.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(roomLabelQueryWrapper);

        //删除RoomPaymentType
        LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
        roomPaymentTypeQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(roomPaymentTypeQueryWrapper);

        //删除RoomLeaseTerm
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(roomLeaseTermQueryWrapper);


    }

    @Override
    public void updateReleaseStatusById(Long id, ReleaseStatus status) {

        if (id == null || status == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        //执行失败的情况，统一用全局异常处理，这样不仅能知道false的原因，而且还能保证返回到Controller层的一定是true的
        //返回值类型不用Result,不用boolean

        LambdaUpdateWrapper<RoomInfo> roomInfoUpdateWrapper = new LambdaUpdateWrapper<>();
        roomInfoUpdateWrapper
                .set(RoomInfo::getIsRelease, status)
                .eq(RoomInfo::getId, id);
        /*
        如果找不到要更新的roomId，会返回false（SQL执行了，语法没问题，只是没找到）
        如果传入了错误的枚举值，比如3（ReleaseStatus.values()中不存在的值），会抛出异常 IllegalArgumentException

        set和eq的顺序不影响执行
         */

        boolean update = this.update(roomInfoUpdateWrapper);
        if (!update) {
            //没找到roomId，所以更新失败
            //至于枚举值的错误，在Spring进入到Controller方法前，就会在枚举转换器中抛出IllegalArgumentException，所以不用写在Service层的业务异常里,而是在全局异常处理类中设置处理方法
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

    }


    //根据公寓id查询房间列表
    @Override
    public List<RoomInfo> listBasicByApartmentId(Long id) {
        LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoQueryWrapper
                .eq(RoomInfo::getApartmentId, id)
                .eq(RoomInfo::getIsRelease, ReleaseStatus.RELEASED);
        return this.list(roomInfoQueryWrapper);

    }
}







