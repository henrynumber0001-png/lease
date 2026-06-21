package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.model.entity.CityInfo;
import com.nocompanyname.lease.web.admin.service.CityInfoService;
import com.nocompanyname.lease.web.admin.mapper.CityInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityInfoServiceImpl extends ServiceImpl<CityInfoMapper, CityInfo>
    implements CityInfoService{

    /*
    讲一下ServiceImpl<CityInfoMapper, CityInfo>
    这是 MyBatis-Plus 提供的一个 Service 实现类基类，帮你提前写好了常用增删改查方法的父类

    语法：ServiceImpl<Mapper接口名, 实体类名> = ServiceImpl<用哪个 Mapper, 操作哪个实体类>
    含义：这个 Service 实现类操作 CityInfo 这张表，底层使用 CityInfoMapper 来访问数据库
    该方法是 MyBatis-Plus 帮你实现好的通用 Service，指定 Mapper 和实体后，就自动拥有这张表的常用 CRUD 方法。（真正实现在Service层实现数据库的增删改查的业务逻辑）
     */

    /*
    CityInfoMapper 接口：它负责真正和数据库交互。
    可以理解成：CityInfoMapper 对应 city_info 表的数据库访问对象

    CityInfo 实体类：对应数据库表：city_info
     */


    @Override
    public List<CityInfo> listByProvinceId(Long provinceId) {
        LambdaQueryWrapper<CityInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CityInfo::getProvinceId, provinceId);
        return this.list(queryWrapper);
    }

    /*
    解释一下：
    这里为什么不调用Mapper层的selectList()，而是直接用Service层的方法list()？

    因为 CityInfoServiceImpl 已经继承了：ServiceImpl<CityInfoMapper, CityInfo>
    ServiceImpl 本身已经帮你持有了 CityInfoMapper，并且提供了很多 Service 层方法，比如：
    this.list()
    this.getById()
    this.save()
    this.removeById()
    this.saveOrUpdate(entity)
    this.updateById(entity)

    所以你不需要再手动写：
    @Autowired
    private CityInfoMapper cityInfoMapper;

    说人话：你可以在 Service层，直接用Service层的方法了，因为方法底层已经帮你自动调用并实现Mapper接口的方法了

    单表查询，可以使用Service层预定义好的方法，不需要再额外调用Mapper了，因为Mybatis-Plus封装好的父类ServiceImpl 已经帮你调用完Mapper了，
    直接调用Service层的方法就好，不需要再去Mapper层写一个selectList()方法了。

    除非是多表查询，需要自己定义Mapper层的方法，
    那么就需要调用Mapper层方法
     */
}




