package com.nocompanyname.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.ApartmentInfo;
import com.nocompanyname.lease.model.entity.BrowsingHistory;
import com.nocompanyname.lease.model.entity.GraphInfo;
import com.nocompanyname.lease.model.entity.RoomInfo;
import com.nocompanyname.lease.model.enums.ItemType;
import com.nocompanyname.lease.web.app.context.LoginUserHolder;
import com.nocompanyname.lease.web.app.mapper.BrowsingHistoryMapper;
import com.nocompanyname.lease.web.app.service.ApartmentInfoService;
import com.nocompanyname.lease.web.app.service.BrowsingHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.app.service.GraphInfoService;
import com.nocompanyname.lease.web.app.service.RoomInfoService;
import com.nocompanyname.lease.web.app.vo.history.HistoryItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class BrowsingHistoryServiceImpl extends ServiceImpl<BrowsingHistoryMapper, BrowsingHistory>
        implements BrowsingHistoryService {

    @Autowired
    private BrowsingHistoryMapper browsingHistoryMapper;
    @Autowired
    private RoomInfoService roomInfoService;
    @Autowired
    private ApartmentInfoService apartmentInfoService;
    @Autowired
    private GraphInfoService graphInfoService;

    @Override
    public IPage<HistoryItemVo> getPage(long current, long size) {

        Long userId = LoginUserHolder.getUserId();

        if(userId == null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }

        Page<HistoryItemVo> page = new Page<>(current, size);
        return browsingHistoryMapper.getPage(page, userId);
    }

    @Override
    public void saveHistory(Long roomId) {

        if(roomId == null){
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        Long userId = LoginUserHolder.getUserId();
        if(userId == null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }

        RoomInfo roomInfo = roomInfoService.getById(roomId); //养成良好习惯，所有的查询指令结束，都要做非空判断
        if(roomInfo == null){
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }

        ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());
        if(apartmentInfo == null){
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }

        LambdaQueryWrapper<BrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowsingHistory::getUserId, userId)
                .eq(BrowsingHistory::getRoomId, roomId); //双重验证，双重保险
        BrowsingHistory browsingHistory = this.getOne(queryWrapper);

        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemId, roomId)
                .eq(GraphInfo::getItemType, ItemType.ROOM)
                .orderByAsc(GraphInfo::getId)
                .last("limit 1");
        //last()表示：把这个指令拼接到SQL末尾；
        //limit 1 表示：只返回结果集中的第 1 条记录
        //但这条记录不一定是 id 最小的，也不一定是 id 最大的（SQL 不保证查询结果顺序），因此需要搭配 orderByAsc/orderByDesc 使用

        GraphInfo graphInfo = graphInfoService.getOne(graphInfoQueryWrapper);
        //如果没查到graphInfo，不应该报错，而是返回null，因为允许房间没有上传图片
        String coverUrl = graphInfo == null ? null : graphInfo.getUrl();

        if(browsingHistory == null){
            //为什么这个browsingHistory不能直接用？
            //因为这里的 browsingHistory 只是一个 变量名，并没有创建对象，它不代表BrowsingHistory对象
            //这时候你直接拿来 browsingHistory.setUserId(userId) 就会发生 空指针异常（因为这时候没有browsingHistory堆内存对象）

            browsingHistory = new BrowsingHistory();
            //此时，赋予 browsingHistory 这个变量一个 BrowsingHistory对象
            //这样就可以操作这个对象的setter方法了

            browsingHistory.setUserId(userId);
            browsingHistory.setRoomId(roomId);
            browsingHistory.setBrowseTime(LocalDateTime.now());
            browsingHistory.setApartmentNameSnapshot(apartmentInfo.getName());
            browsingHistory.setRoomNumberSnapshot(roomInfo.getRoomNumber());
            browsingHistory.setRentSnapshot(roomInfo.getRent());
            browsingHistory.setCityNameSnapshot(apartmentInfo.getCityName());
            browsingHistory.setProvinceNameSnapshot(apartmentInfo.getProvinceName());
            browsingHistory.setDistrictNameSnapshot(apartmentInfo.getDistrictName());
            browsingHistory.setCoverUrlSnapshot(coverUrl);
            this.save(browsingHistory);
        }else {
            browsingHistory.setBrowseTime(LocalDateTime.now());
            browsingHistory.setApartmentNameSnapshot(apartmentInfo.getName());
            browsingHistory.setRoomNumberSnapshot(roomInfo.getRoomNumber());
            browsingHistory.setRentSnapshot(roomInfo.getRent());
            browsingHistory.setCityNameSnapshot(apartmentInfo.getCityName());
            browsingHistory.setProvinceNameSnapshot(apartmentInfo.getProvinceName());
            browsingHistory.setDistrictNameSnapshot(apartmentInfo.getDistrictName());
            browsingHistory.setCoverUrlSnapshot(coverUrl);
            this.updateById(browsingHistory);
        }
    }
}
