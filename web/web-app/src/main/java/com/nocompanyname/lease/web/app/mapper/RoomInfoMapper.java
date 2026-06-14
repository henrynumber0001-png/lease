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

/*
解释：为什么不能用 LEFT JOIN 实现。
因为这个查询涉及多个一对多关系，直接联表会破坏“按房间分页”的语义。

哪些表可以直接 JOIN
房间和公寓通常是多对一：
room_info.apartment_id -> apartment_info.id

每个房间只对应一个公寓，所以可以直接：
LEFT JOIN apartment_info ai ON ri.apartment_id = ai.id
查询结果仍然是一个房间一行，不影响分页。

问题出在哪里
一个房间可能有：多张图片、多个标签、多种支付方式

假设房间 101 有：
图片：3张
标签：2个
数据库会产生笛卡尔组合：
图片1 + 标签1
图片1 + 标签2
图片2 + 标签1
图片2 + 标签2
图片3 + 标签1
图片3 + 标签2
一个房间变成了 6 行。
对分页的影响

因为这是以room_info为主表的查询，所以你要保证room_info.id不重复。
 */