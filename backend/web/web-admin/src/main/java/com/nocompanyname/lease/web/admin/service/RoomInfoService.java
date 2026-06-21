package com.nocompanyname.lease.web.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nocompanyname.lease.model.entity.RoomInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nocompanyname.lease.model.enums.ReleaseStatus;
import com.nocompanyname.lease.web.admin.vo.room.RoomDetailVo;
import com.nocompanyname.lease.web.admin.vo.room.RoomItemVo;
import com.nocompanyname.lease.web.admin.vo.room.RoomQueryVo;
import com.nocompanyname.lease.web.admin.vo.room.RoomSubmitVo;

import java.util.List;


public interface RoomInfoService extends IService<RoomInfo> {

    void saveOrUpdateByRoom(RoomSubmitVo roomSubmitVo);

    IPage<RoomItemVo> getBypageItem(long current, long size, RoomQueryVo queryVo);

    RoomDetailVo getDetailById(Long id);

    void removeByRoomId(Long id);

    void updateReleaseStatusById(Long id, ReleaseStatus status);

    List<RoomInfo> listBasicByApartmentId(Long id);
}
