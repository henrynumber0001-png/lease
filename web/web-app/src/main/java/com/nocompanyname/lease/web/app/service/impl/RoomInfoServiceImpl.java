package com.nocompanyname.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.model.entity.RoomInfo;
import com.nocompanyname.lease.web.app.mapper.RoomInfoMapper;
import com.nocompanyname.lease.web.app.service.RoomInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.app.vo.room.RoomItemVo;
import com.nocompanyname.lease.web.app.vo.room.RoomQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Override
    public IPage<RoomItemVo> getPageItem(long current, long size, RoomQueryVo queryVo) {
        // Page must be passed to the mapper so the MyBatis-Plus interceptor can add pagination.
        Page<RoomItemVo> page = new Page<>(current, size);
        if (queryVo == null) {
            queryVo = new RoomQueryVo();
        }
        return roomInfoMapper.getPageItem(page, queryVo);
    }
}


