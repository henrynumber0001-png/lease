package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.model.entity.FacilityInfo;
import com.nocompanyname.lease.model.enums.ItemType;
import com.nocompanyname.lease.web.admin.service.FacilityInfoService;
import com.nocompanyname.lease.web.admin.mapper.FacilityInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【facility_info(配套信息表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class FacilityInfoServiceImpl extends ServiceImpl<FacilityInfoMapper, FacilityInfo>
    implements FacilityInfoService{

    @Override
    public List<FacilityInfo> listByType(ItemType type) {
        LambdaQueryWrapper<FacilityInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(type != null, FacilityInfo::getType, type); //当 type = null, 也就是条件查询为空，那么queryWrapper = {}
        return this.list(queryWrapper);  // 那么 list(queryWrapper) 就等价于 list()，查询全部配套信息列表。
    }
}




