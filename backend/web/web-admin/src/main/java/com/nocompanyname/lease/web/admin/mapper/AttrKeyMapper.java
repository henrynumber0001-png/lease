package com.nocompanyname.lease.web.admin.mapper;

import com.nocompanyname.lease.model.entity.AttrKey;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nocompanyname.lease.web.admin.vo.attr.AttrKeyVo;

import java.util.List;

/**
* @author liubo
* @description 针对表【attr_key(房间基本属性表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.atguigu.lease.model.AttrKey
*/
public interface AttrKeyMapper extends BaseMapper<AttrKey> {

    List<AttrKeyVo> listAttrInfo();
    //如果想获取 全部属性的名称和值，也就是前端的需求，那么就需要获取一个拼接了attr_key和attr_value的中间表，查询并返回全部数据，映射的Java对象（AttrKeyVo对象的集合）
    //所以先在mapper层定义一个接口方法，然后到Mapper.xml文件中，通过SQL语句实现它
}




