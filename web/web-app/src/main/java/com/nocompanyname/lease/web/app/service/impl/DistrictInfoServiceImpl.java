package com.nocompanyname.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.model.entity.DistrictInfo;
import com.nocompanyname.lease.web.app.service.DistrictInfoService;
import com.nocompanyname.lease.web.app.mapper.DistrictInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【district_info】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class DistrictInfoServiceImpl extends ServiceImpl<DistrictInfoMapper, DistrictInfo>
    implements DistrictInfoService{

    @Override
    public List<DistrictInfo> listByCityId(Long id) {
        LambdaQueryWrapper<DistrictInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DistrictInfo::getCityId,id);
        return this.list(queryWrapper);
    }
}




