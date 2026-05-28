package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.model.entity.AttrValue;
import com.nocompanyname.lease.web.admin.service.AttrValueService;
import com.nocompanyname.lease.web.admin.mapper.AttrValueMapper;
import org.springframework.stereotype.Service;

/**
* @author liubo
* @description 针对表【attr_value(房间基本属性值表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class AttrValueServiceImpl extends ServiceImpl<AttrValueMapper, AttrValue>
    implements AttrValueService{

    //ServiceImpl 是 MyBatis-Plus 提供的默认实现类，它已经把 saveOrUpdate 的具体逻辑实现好了。
    //相当于把 具体的实现方法 封装在 父类ServiceImpl中，你继承之后就可以直接使用了。
    /*
    逻辑链条：
    attrValueService.saveOrUpdate(attrValue)
        ↓
    AttrValueService 继承 IService，所以有这个方法声明
        ↓
    AttrValueServiceImpl 继承 ServiceImpl，所以有这个方法实现
        ↓
    底层通过 AttrValueMapper 操作 attr_value 表
     */
}




