package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.model.entity.DistrictInfo;
import com.nocompanyname.lease.web.admin.service.DistrictInfoService;
import com.nocompanyname.lease.web.admin.mapper.DistrictInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class DistrictInfoServiceImpl extends ServiceImpl<DistrictInfoMapper, DistrictInfo>
    implements DistrictInfoService{

    @Override
    public List<DistrictInfo> listByCityInfo(Long cityId) {
        LambdaQueryWrapper<DistrictInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DistrictInfo::getCityId,cityId);
        return this.list(queryWrapper);
    }
}




