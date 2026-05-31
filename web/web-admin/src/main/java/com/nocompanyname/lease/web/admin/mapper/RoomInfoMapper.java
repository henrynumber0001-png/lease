package com.nocompanyname.lease.web.admin.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.model.entity.RoomInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nocompanyname.lease.web.admin.vo.room.RoomItemVo;
import com.nocompanyname.lease.web.admin.vo.room.RoomQueryVo;
import org.apache.ibatis.annotations.Param;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.atguigu.lease.model.RoomInfo
*/
public interface RoomInfoMapper extends BaseMapper<RoomInfo> {


    IPage<RoomItemVo> getBypageItem(Page<RoomItemVo> page,@Param("queryVo") RoomQueryVo queryVo);

    /*
    resultType 和 resultMap 的选择：
    resultType：返回值类型中的成员变量 都是 基本数据类型；
    resultMap：返回值类型中的成员变量 是 实体类/实体类的集合；
     */
}





