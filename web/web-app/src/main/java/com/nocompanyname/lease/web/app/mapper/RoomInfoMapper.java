package com.nocompanyname.lease.web.app.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.model.entity.RoomInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nocompanyname.lease.web.app.vo.room.RoomItemVo;
import com.nocompanyname.lease.web.app.vo.room.RoomQueryVo;
import org.apache.ibatis.annotations.Param;



public interface RoomInfoMapper extends BaseMapper<RoomInfo> {

    IPage<RoomItemVo> getPageItem(Page<RoomItemVo> page, @Param("queryVo") RoomQueryVo queryVo);
}
