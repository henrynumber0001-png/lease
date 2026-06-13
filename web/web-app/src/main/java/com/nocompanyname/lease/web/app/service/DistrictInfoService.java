package com.nocompanyname.lease.web.app.service;

import com.nocompanyname.lease.model.entity.DistrictInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author liubo
* @description 针对表【district_info】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface DistrictInfoService extends IService<DistrictInfo> {

    List<DistrictInfo> listByCityId(Long id);
}
