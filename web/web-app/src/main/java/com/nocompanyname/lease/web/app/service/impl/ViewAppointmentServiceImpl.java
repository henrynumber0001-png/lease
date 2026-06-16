package com.nocompanyname.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.ApartmentInfo;
import com.nocompanyname.lease.model.entity.GraphInfo;
import com.nocompanyname.lease.model.entity.ViewAppointment;
import com.nocompanyname.lease.model.enums.ItemType;
import com.nocompanyname.lease.web.app.context.LoginUserHolder;
import com.nocompanyname.lease.web.app.mapper.ViewAppointmentMapper;
import com.nocompanyname.lease.web.app.service.ApartmentInfoService;
import com.nocompanyname.lease.web.app.service.GraphInfoService;
import com.nocompanyname.lease.web.app.service.ViewAppointmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.app.vo.appointment.AppointmentItemVo;
import com.nocompanyname.lease.web.app.vo.graph.GraphVo;
import kotlin.jvm.internal.Lambda;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment>
        implements ViewAppointmentService {

    private final ApartmentInfoService apartmentInfoService;
    private final GraphInfoService graphInfoService;

    public ViewAppointmentServiceImpl(ApartmentInfoService apartmentInfoService, GraphInfoService graphInfoService) {
        this.apartmentInfoService = apartmentInfoService;
        this.graphInfoService = graphInfoService;
    }

    @Override
    public List<AppointmentItemVo> listItem() {

        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }

        LambdaQueryWrapper<ViewAppointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ViewAppointment::getUserId, userId)
                .orderByDesc(ViewAppointment::getAppointmentTime);//这个接口功能毕竟是“查询”，那么记录的返回一定要有顺序才行
        List<ViewAppointment> viewAppointments = this.list(queryWrapper);

        if (viewAppointments.isEmpty()) {
            return Collections.emptyList();
        }

        //apartmentName
        List<Long> apartmentIds = viewAppointments.stream()
                .map(ViewAppointment::getApartmentId)
                .distinct()
                .collect(Collectors.toList());
        List<ApartmentInfo> apartmentInfos = apartmentInfoService.listByIds(apartmentIds);
        Map<Long, String> apartmentInfoMap = apartmentInfos.stream()
                .collect(Collectors
                        .toMap(ApartmentInfo::getId, ApartmentInfo::getName));
        //list.stream().collect(Collectors.toMap())  适合简单键值对场景：同一个对象 -> 一个 key -> 一个 value
        //对于复杂Map集合，键和值的类型不同，且一个键对应的是一个值的集合，要用下面的方法手动自定义
        //或者使用 list.steram()..collect(Collectors.groupingBy()


        //自定义一个Map
        //仔细观察需求文档，从view_Appointment到apartment_info，再到graph_info，只有apartment_id这一个字段是贯穿的，所以必须要用它作为Key，生成一个Map
        //为什么？因为当 两个非空集合需要实现从A赋值到B的话，必须通过forEach + map.get(key)，否则无法实现

        //自己定义泛型，Key不用写任何实体类，apartmentId是Long类型，Value就写AppointmentItemVo的属性：List<GraphVo> graphVoList
        Map<Long, List<GraphVo>> graphVoMap = new HashMap<>();


        //这里的查询思想，值得学习
        //你之前的思路是：因为一个apartmentId会对应一个List<GraphInfo> graphInfoList，所以我要遍历每一个apartmentId，获得它对应的这个graphInfoList。
        //然后graphInfoList里面还有graphInfo等着你去转换到graphVo，这样会使代码变得异常复杂，且无法完成最终的赋值
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT)
                //这里采取的思路是，先根据Ids，查询出所有符合条件的 graphInfo, 这些graphInfo 是属于 全部 符合条件的 apartment 的，不根据apartmentId进行区分，都放在一个List中
                .in(GraphInfo::getItemId, apartmentIds);

        //然后依靠Map集合去识别graphInfo转换的graphVo属于哪个apartmentId
        List<GraphInfo> graphInfos = graphInfoService.list(graphInfoQueryWrapper);

        //不能创建这个forEach之外的graphVoList，这会让所有graphInfo（包含不同apartmentId）的name + url 都保存到 同一个list当中
//        List<GraphVo> graphVoList = new ArrayList<>();
        graphInfos.forEach(graphInfo -> {
            GraphVo graphVo = new GraphVo();
            BeanUtils.copyProperties(graphInfo, graphVo);


            //这一段非常重要
            //第一步：检查key是否存在
            //如果graphVoMap中不存在目前遍历的这个key，说明graphVoMap中还没有创建关于这个key的键值对
            //那么要创建一个 值是空List集合的 键值对，并加入到graphVoMap中
            if (!graphVoMap.containsKey(graphInfo.getItemId())) {
                graphVoMap.put(graphInfo.getItemId(), new ArrayList<>());
            }

            //然后继续执行代码，根据刚刚加入到key，获取对应的这个空List集合，然后往里面添加上面刚刚被赋值的graphVo
            //为什么不能直接在上一步直接 graphVoList，和上面的注释呼应：因为如果你要写graphVoList，那么所有graphVo必然会进同一个graphVoList（因为它要写在最外面）
            //而通过检测key是否存在，而创建List，可以像下面方法一样，让元素只会加入到Key对应的List集合中
            //这一步是在说，graphVoMap中已经有这个key了，接下来继续更新List集合即可
            graphVoMap.get(graphInfo.getItemId()).add(graphVo);
        });


        List<AppointmentItemVo> appointmentItemVoList = new ArrayList<>();
        if (!viewAppointments.isEmpty()) {
            viewAppointments.forEach(viewAppointment -> {
                AppointmentItemVo appointmentItemVo = new AppointmentItemVo();
                appointmentItemVo.setId(viewAppointment.getId());
                appointmentItemVo.setAppointmentTime(viewAppointment.getAppointmentTime());
                appointmentItemVo.setAppointmentStatus(viewAppointment.getAppointmentStatus());
                appointmentItemVo.setApartmentName(apartmentInfoMap.get(viewAppointment.getApartmentId()));
                appointmentItemVo.setGraphVoList(graphVoMap.get(viewAppointment.getApartmentId()));
                appointmentItemVoList.add(appointmentItemVo);
            });
        }

        return appointmentItemVoList;

    }
}




