package com.nocompanyname.lease.web.admin.mapper;

import com.nocompanyname.lease.model.entity.ApartmentInfo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import org.apache.ibatis.annotations.Param;

/**
* @author liubo
* @description 针对表【apartment_info(公寓信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.atguigu.lease.model.ApartmentInfo
*/
public interface ApartmentInfoMapper extends BaseMapper<ApartmentInfo> {

    IPage<ApartmentItemVo> getPageItem(Page<ApartmentItemVo> page, @Param("queryVo") ApartmentQueryVo queryVo);
    /*
    如果你不加这个@Param("queryVo")
    Mybatis-plus会自动将test中的属性值与SQL中的占位符进行匹配，但是为了代码的可读性和清晰性，建议显式地指定参数名称。
    或者它会给参数自动起默认名：
    对应关系大概是：
    第 1 个参数 page    -> arg0 / param1
    第 2 个参数 queryVo -> arg1 / param2

    最终变成：
    <if test="param2.provinceId != null">
    AND province_id = #{param2.provinceId}
    </if>
     */

    /*
    在Mapper.xml中，只需要根据ApartmentItemVo类的要求，查询所有需要的字段，正常的编写SQL语句即可，查询条件就是ApartmentQueryVo中的字段
    不需要管第一个参数，分页容器，因为Mybatis-plus会自动处理分页逻辑
     */


}




