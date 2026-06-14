package com.nocompanyname.lease.web.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nocompanyname.lease.model.entity.RoomInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nocompanyname.lease.web.app.vo.room.RoomDetailVo;
import com.nocompanyname.lease.web.app.vo.room.RoomItemVo;
import com.nocompanyname.lease.web.app.vo.room.RoomQueryVo;


public interface RoomInfoService extends IService<RoomInfo> {
    IPage<RoomItemVo> getPageItem(long current, long size, RoomQueryVo queryVo);

    RoomDetailVo getDetailById(Long id);

    IPage<RoomItemVo> pageItemByApartmentId(long current, long size, Long apartmentId);
}
