package com.nocompanyname.lease.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.model.entity.CityInfo;
import com.nocompanyname.lease.model.entity.DistrictInfo;
import com.nocompanyname.lease.model.entity.ProvinceInfo;
import com.nocompanyname.lease.web.admin.service.CityInfoService;
import com.nocompanyname.lease.web.admin.service.DistrictInfoService;
import com.nocompanyname.lease.web.admin.service.ProvinceInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "地区信息管理")
@RestController
@RequestMapping("/admin/region")
public class RegionInfoController {

    @Autowired
    ProvinceInfoService provinceInfoService;

    @Autowired
    CityInfoService cityInfoService;
    @Autowired
    DistrictInfoService districtInfoService;

    @Operation(summary = "查询省份信息列表")
    @GetMapping("province/list")
    public Result<List<ProvinceInfo>> listProvince() {
        List<ProvinceInfo> provinceInfos = provinceInfoService.list();
        //虽然最标准的写法，依然是将业务逻辑封装在Service层，但因为list()简单且已预定义好了，所以可以直接用在Controller层

        /*
        关于这个 查询全部信息 的方法，虽然有 单表查询 和 多表查询 之分，
        但总的来说，都是一个list相关的方法，list()/listxxxInfo()，
        返回的就是所选表格中全部的行数据
         */

        return Result.ok(provinceInfos);
    }

    @Operation(summary = "根据省份id查询城市信息列表")
    @GetMapping("city/listByProvinceId")
    public Result<List<CityInfo>> listCityInfoByProvinceId(@RequestParam Long provinceId) {
        /*
        参数列表中的 参数类型 是 客户端http传输过来的字符串 转换成的 Java数据类型（根据参数列表要求）
        然后Java操作数据库时，再转换成数据库的数据类型
         */

        List<CityInfo> cityInfos = cityInfoService.listByProvinceId(provinceId);
        //将业务实现逻辑，即查询方法写在Service层，是项目更实用且正确的方式
        //写在Controller层，如果一旦再需要其他Controller方法也要写这个方法，那么所有业务逻辑要再写一遍
        return Result.ok(cityInfos);
    }

    @GetMapping("district/listByCityId")
    @Operation(summary = "根据城市id查询区县信息")
    public Result<List<DistrictInfo>> listDistrictInfoByCityId(@RequestParam Long cityId) {
        List<DistrictInfo> districtInfos = districtInfoService.listByCityInfo(cityId);
        return Result.ok(districtInfos);
    }

}
