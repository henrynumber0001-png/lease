package com.nocompanyname.lease.web.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nocompanyname.lease.model.entity.RoomInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nocompanyname.lease.web.app.vo.room.RoomItemVo;
import com.nocompanyname.lease.web.app.vo.room.RoomQueryVo;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface RoomInfoService extends IService<RoomInfo> {
    IPage<RoomItemVo> getPageItem(long current, long size, RoomQueryVo queryVo);
}
