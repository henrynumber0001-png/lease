package com.nocompanyname.lease.web.admin.service.impl;

import com.nocompanyname.lease.model.entity.AttrKey;
import com.nocompanyname.lease.web.admin.mapper.AttrKeyMapper;
import com.nocompanyname.lease.web.admin.service.AttrKeyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.admin.vo.attr.AttrKeyVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【attr_key(房间基本属性表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class AttrKeyServiceImpl extends ServiceImpl<AttrKeyMapper, AttrKey>
    implements AttrKeyService{

    @Autowired
    AttrKeyMapper attrKeyMapper; //当mapper层实现了 获取AttrKeyVo对象 list集合的 逻辑之后，让返回值在service层返回
    @Override
    public List<AttrKeyVo> listAttrInfo() {
        return attrKeyMapper.listAttrInfo();
    }
}




