package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.model.entity.*;
import com.nocompanyname.lease.model.enums.ItemType;
import com.nocompanyname.lease.web.admin.mapper.ApartmentInfoMapper;
import com.nocompanyname.lease.web.admin.mapper.LabelInfoMapper;
import com.nocompanyname.lease.web.admin.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.nocompanyname.lease.web.admin.vo.fee.FeeValueVo;
import com.nocompanyname.lease.web.admin.vo.graph.GraphVo;
import kotlin.jvm.internal.Lambda;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Autowired
    private ApartmentFacilityService apartmentFacilityService;

    @Autowired
    private ApartmentLabelService apartmentLabelService;

    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private LabelInfoService labelInfoService;

    @Autowired
    private ApartmentLabelService aparmentLabelService;

    @Autowired
    private FacilityInfoService facilityInfoService;

    @Autowired
    private FeeValueService feeValueService;

    @Autowired
    private FeeKeyService feeKeyService;


    @Override
    public void saveOrUpdateByApartment(ApartmentSubmitVo apartmentSubmitVo) {

        boolean isUpdate = apartmentSubmitVo.getId() != null; //id = null 为新增
        /*
        ApartmentSubmitVo 本身没有自己声明 id 属性, id 属性来自ApartmentInfo，最终来自BaseEntity
        所以，apartmentSubmitVo.getId() = apartmentInfo.getId() = (数据库)apartment_info.id
         */

        this.saveOrUpdate(apartmentSubmitVo);
        /*
        ApartmentInfoServiceImpl 本身就是ApartmentInfoService的实现类了，还@Autowired ApartmentInfoService干什么呢？
        直接用this/super 调用 ServiceImpl 预定义的 saveOrUpdate()就好了
        无论 新增 还是 更新， 都要使用这个方法，将apartment_info表的这部分写入进去
        至于ApartmentSubmitVo中其他的属性，要通过下面的指令，单独新增或保存
         */

        Long apartmentId = apartmentSubmitVo.getId();

        if (isUpdate) { //说明是更新，不是新增
            LambdaQueryWrapper<ApartmentFacility> apartmentFacilityLambdaQueryWrapper = new LambdaQueryWrapper<ApartmentFacility>();
            apartmentFacilityLambdaQueryWrapper.eq(ApartmentFacility::getApartmentId, apartmentId); //当apartment_facility表中的apartment_id字段的值 = apartment_info.id
            apartmentFacilityService.remove(apartmentFacilityLambdaQueryWrapper);//就删除apartment_facility这张表中满足条件的所有行数据
            /*
            这里是 条件查询构造器 是哪张表的，就用对应的Service去执行remove
            因为 ApartmentSubmitVo 除了携带 ApartmentInfo 所有字段之外，还带着facilityId, labelId,feeValueId，这三个id 加上 graphInfo 都是需要更新或新增的
            所以你不能用 apartmentInfoService 再去操作他们（也操作不到）
             */

            LambdaQueryWrapper<ApartmentLabel> apartmentLabelQueryWrapper = new LambdaQueryWrapper<ApartmentLabel>();
            apartmentLabelQueryWrapper.eq(ApartmentLabel::getApartmentId, apartmentId);
            apartmentLabelService.remove(apartmentLabelQueryWrapper);

            LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFeeValueLambdaQueryWrapper.eq(ApartmentFeeValue::getApartmentId, apartmentId);
            apartmentFeeValueService.remove(apartmentFeeValueLambdaQueryWrapper);

            LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<GraphInfo>();
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId, apartmentId).eq(GraphInfo::getItemType, ItemType.APARTMENT);
            //这里为什么要传入一个固定值 ItemType.APARTMENT，是因为 整个类的三层架构都是在操作Apartment的，所以你不需要考虑 有可能是Room的可能
            graphInfoService.remove(graphInfoLambdaQueryWrapper);

        }

        //接下来是插入操作（新增 和 更新都要执行）
        List<Long> facilityIds = apartmentSubmitVo.getFacilityIds();
        ArrayList<ApartmentFacility> apartmentFacilityArrayList = new ArrayList<>();
        if (facilityIds != null) {
            facilityIds.forEach(facilityId -> {
                ApartmentFacility apartmentFacility = new ApartmentFacility();
                apartmentFacility.setApartmentId(apartmentId);
                apartmentFacility.setFacilityId(facilityId);
                //ApartmentFacility实体类 就是 数据库表apartment_facility的一行具体数据
                apartmentFacilityArrayList.add(apartmentFacility);
            });
        }
        if (!apartmentFacilityArrayList.isEmpty()) {
            apartmentFacilityService.saveBatch(apartmentFacilityArrayList);
            /*
            saveBatch(List) 是用于 批量插入行数据到 数据库表格中
            save(Entity) 是用于 插入单行数据到 数据库表格中

            如果上面的apartmentFacilityArrayList，使用save进行保存，那么将执行：
            apartmentFacilityService.save(apartmentFacility1);
            apartmentFacilityService.save(apartmentFacility2);
            apartmentFacilityService.save(apartmentFacility3);
            效率非常低

            如果上面的apartmentFacilityArrayList为空，那么将不会执行saveBatch操作，也不会报错
             */
        }


        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        List<ApartmentFeeValue> apartmentFeeValueArrayList = new ArrayList<>();
        if (feeValueIds != null) {
            feeValueIds.forEach(feeValueId -> {
                ApartmentFeeValue apartmentFeeValue = new ApartmentFeeValue();
                apartmentFeeValue.setApartmentId(apartmentId);
                apartmentFeeValue.setFeeValueId(feeValueId);
                apartmentFeeValueArrayList.add(apartmentFeeValue);
            });
        }
        if (!apartmentFeeValueArrayList.isEmpty()) {
            apartmentFeeValueService.saveBatch(apartmentFeeValueArrayList);
            //这里一定是要用 数据库表格的Service层实现类去操作saveBatch方法：
            //首先是业务逻辑要写在Service层，其次是批量操作只有Service层有
        }

        List<Long> labelIds = apartmentSubmitVo.getLabelIds();
        List<ApartmentLabel> apartmentLabelArrayList = new ArrayList<>();
        if (labelIds != null) {
            labelIds.forEach(labelId -> {
                ApartmentLabel apartmentLabel = new ApartmentLabel();
                apartmentLabel.setApartmentId(apartmentId);
                apartmentLabel.setLabelId(labelId);
                apartmentLabelArrayList.add(apartmentLabel);
            });
        }
        if (!apartmentLabelArrayList.isEmpty()) {
            apartmentLabelService.saveBatch(apartmentLabelArrayList);
            /*
            一定是用ApartmentLabelService去操作saveBatch方法，原因很简单：
            因为你本质上是需要向apartment_label表中插入多行数据，
            那么就要操作ApartmentLabelService的实现类去批量保存拥有apartment_label多行数据的List集合
             */

        }

        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
        if (graphVoList != null) {
            graphVoList.forEach(graphVo -> {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setName(graphVo.getName());
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setItemId(apartmentId);
                graphInfo.setUrl(graphVo.getUrl());
                graphInfoList.add(graphInfo);
            });
        }
        if (!graphInfoList.isEmpty()) {
            graphInfoService.saveBatch(graphInfoList);
        }
        /*
        如果不写这个 if(!graphInfoList.isEmpty())
        当 graphInfoList = 空集合 时
        通常情况下，MyBatis-Plus 对空集合不会插入任何数据，数据库不会新增行。
        但不建议依赖这个行为。因为：
        1. 空集合没有必要调用 saveBatch
        2. 某些版本或某些实现可能对空集合处理不一致
        3. 可读性不如显式判断清楚
         */
    }

    @Override
    public IPage<ApartmentItemVo> getPageItem(long current, long size, ApartmentQueryVo queryVo) {
        Page<ApartmentItemVo> page = new Page<>(current, size); //首先创建一个空的分页容器，包含 分页规则（这一步写在Service层，不要写在Controller层）
        /*
        这里用Page<ApartmentItemVo> 而不是Page<ApartmentInfo>，
        与 ApartmentItemVo 不是数据库实体 无关，而是因为 这个分页结果最终要封装成 ApartmentItemVo 类型。
        Page<T> 里的 T 表示：这一页里的每一条记录是什么 Java 类型，而不是表示 T 必须是一张数据库表对应的实体类。

        即，分页查询的结果，被设计成 以ApartmentItemVo类型 返回给前端（所以分页器里要装ApartmentItemVo，而非ApartmentInfo）
         */

        return apartmentInfoMapper.getPageItem(page, queryVo); //返回的是一个装了查询出来的 records、total、pages 等分页信息的page容器
        /*
        不管Vo类中的成员变量只是单独的额外数据字段，还是在数据库里的有这张表，他们都是附加信息。
        即，在Mapper层的操作，还是要用主表的Mapper层对象去操作数据库（用主表去JOIN从表/子结果集）
        虽然返回的是Vo类，但整个Vo类也是围绕着主表数据执行的，所以要通过主表在Mapper层的对象去发起数据库的SQL操作，去JOIN之后，返回Vo类格式的数据
         */

        /*
        方法名可以自定义，但参数列表要遵守 MyBatis-Plus 分页查询的约定，即传入一个分页容器page（带规则） + 查询条件
        MyBatis-Plus 看到 Mapper 方法的第一个参数是 Page，就知道：这是一个分页查询（自动识别）
        于是它会帮你做几件事：
        1. 根据 current 和 size 计算分页范围；
        2. 自动给 SQL 加 LIMIT；（WHERE ...LIMIT 0, 10）
        3. 自动查询总记录数 total；
        4. 把查询结果放进 page.records；
        5. 返回带分页信息的 IPage；（IPage<ApartmentItemVo>）
         */
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //永远不要忘了：ApartmentDetailVo 继承自 ApartmentInfo，所以 ApartmentDetailVo 还有一个 id 属性；
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id); //WHERE id = #{id}，返回apartment_info这张表的全部字段信息。这就是ApartmentInfoMapper的作用

        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo(); //Vo类没有Service，也就没办法@Autowired一个它的实现类对象。但是我们可以使用 new对象的方式，创建一个对象，反正也只有这个方法内需要用到它。
        //查询图片列表 GraphVoList
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemId, id).eq(GraphInfo::getItemType, ItemType.APARTMENT);
        List<GraphInfo> graphInfos = graphInfoService.list(graphInfoQueryWrapper);
        List<GraphVo> graphVoList = new ArrayList<>();
        graphInfos.forEach(graphInfo -> {
            GraphVo graphVo = new GraphVo();
            graphVo.setName(graphInfo.getName());
            graphVo.setUrl(graphInfo.getUrl());
            graphVoList.add(graphVo);
        });

        if (!graphVoList.isEmpty()) {
            apartmentDetailVo.setGraphVoList(graphVoList);
        }


        //查询标签列表 labelInfoList
//        List<LabelInfo> labelInfos = labelInfoService.list(); //这么写的问题是：我需要指定公寓id的label信息（注意，这个label信息是包含label_id的，因为你要的是List<LabelInfo>, LabelInfo是有id属性的，也要返回）
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelQueryWrapper.eq(ApartmentLabel::getApartmentId, id);
        List<ApartmentLabel> apartmentLabels = aparmentLabelService.list(apartmentLabelQueryWrapper);
        List<Long> labelIds = apartmentLabels.stream()
                .map(apartmentLabel -> apartmentLabel.getLabelId()) //把流里的每一个 ApartmentLabel 对象转换成它的 labelId。从 List<ApartmentLabel> 到 List<Long>。
                .collect(Collectors.toList());
        /*
        map()的作用是：逐个转换元素（值/类型），并创建新的流对象
        stream()的 中间操作 都是针对 流里的每一个元素 进行的

        所以.map(ApartmentLabel::getLabelId)的意思是
        把流里的每一个 ApartmentLabel 对象转换成它的成员变量 Long类型的 labelId
        从 Stream<ApartmentLabel> 到 Stream<Long> 类型
         */

        if (!labelIds.isEmpty()) {
            apartmentDetailVo.setLabelInfoList(labelInfoService.listByIds(labelIds));

            /*
            效果等同于：
            LambdaQueryWrapper<LabelInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(LabelInfo::getId, labelIds); //这里是 in，不是eq
            labelInfoService.list(queryWrapper);

            SELECT *
            FROM label_info
            WHERE id IN (1, 2, 3)
              AND is_deleted = 0

            只不过如果要查主键ids，MyBatis-Plus IService 为我们内置了官方写好的方法 listByIds，一键完成
             */
        }

        //查询配套列表 facilityInfoList
        LambdaQueryWrapper<ApartmentFacility> apartmentFacilityQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFacilityQueryWrapper.eq(ApartmentFacility::getApartmentId, id);
        List<ApartmentFacility> apartmentFacilityList = apartmentFacilityService.list(apartmentFacilityQueryWrapper);
        List<Long> facilityIds = apartmentFacilityList.stream()
                .map(ApartmentFacility::getFacilityId)
                .collect(Collectors.toList());

        if (!facilityIds.isEmpty()) {
            apartmentDetailVo.setFacilityInfoList(facilityInfoService.listByIds(facilityIds));
        }

        //查询杂费列表 feeValueVoList
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, id);
        List<ApartmentFeeValue> apartmentFeeValueList = apartmentFeeValueService.list(apartmentFeeValueQueryWrapper);
        List<Long> feeValueIds = apartmentFeeValueList.stream()
                .map(ApartmentFeeValue::getFeeValueId)
                .collect((Collectors.toList()));

        if (!feeValueIds.isEmpty()) {
            List<FeeValue> feeValues = feeValueService.listByIds(feeValueIds);
            List<Long> feeKeyIds = feeValues.stream().map(FeeValue::getFeeKeyId).collect(Collectors.toList());
            Map<Long, String> feeKeyMap = feeKeyService.listByIds(feeKeyIds).stream().collect(Collectors.toMap(FeeKey::getId, FeeKey::getName));

            List<FeeValueVo> feeValueVoList = new ArrayList<>();
            feeValues.forEach(feeValue -> {
                FeeValueVo feeValueVo = new FeeValueVo();
                BeanUtils.copyProperties(feeValue, feeValueVo);
                feeValueVo.setFeeKeyName(feeKeyMap.get(feeValue.getFeeKeyId())); //这里用feeValue的feeKeyId，而没使用feeKeyMap自己的key，是因为这是在feeValues的for循环里，你没办法用Map集合的遍历
                feeValueVoList.add(feeValueVo);
            });
            apartmentDetailVo.setFeeValueVoList(feeValueVoList);
        }
        return apartmentDetailVo;

        /*
        这个根据Id查询详细信息的业务逻辑，写在Service层实现更合适。
        因为ApartmentDetailVo 里有多个集合：
        private List<GraphVo> graphVoList;
        private List<LabelInfo> labelInfoList;
        private List<FacilityInfo> facilityInfoList;
        private List<FeeValueVo> feeValueVoList;

        这些集合里面的元素又都是嵌套的，或者多表关联的，极其复杂。
        如果都写在XML里，会产生结果行膨胀。
         */
    }
}




