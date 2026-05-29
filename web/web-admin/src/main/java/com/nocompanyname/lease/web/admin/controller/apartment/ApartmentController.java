package com.nocompanyname.lease.web.admin.controller.apartment;


import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.model.entity.ApartmentInfo;
import com.nocompanyname.lease.model.enums.ReleaseStatus;
import com.nocompanyname.lease.web.admin.service.ApartmentInfoService;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.nocompanyname.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "公寓信息管理")
@RestController
@RequestMapping("/admin/apartment")
public class ApartmentController {

    @Autowired
    ApartmentInfoService apartmentInfoService;

    @Operation(summary = "保存或更新公寓信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody ApartmentSubmitVo apartmentSubmitVo) {
        apartmentInfoService.saveOrUpdateByApartment(apartmentSubmitVo);
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询公寓列表")
    @GetMapping("pageItem")
    public Result<IPage<ApartmentItemVo>> pageItem(@RequestParam long current, @RequestParam long size, ApartmentQueryVo queryVo) {
        IPage<ApartmentItemVo> page = apartmentInfoService.getPageItem(current,size,queryVo);
        return Result.ok(page);
    }
    /*
    ⚠️这个地方要重点讲一下Vo类：
    1.Vo类不是 数据库实体类，也没有对应的数据库表，它更像是一个 虚拟表；
    2.Vo类只是前端需求返回的信息，被封装到Java的一个实体类中，便于三层架构进行操作；
    3.Vo类通常设计成 一个主表 + 围绕主表产生的关联数据，也就是说，Vo类中的成员变量，是经过设计的，主从之间是有关联的（通常是主键id），不是随意写的
    4.Vo类 只作为 方法参数 或 返回值类型，一般不创建 Service层接口（Service层主要围绕数据库实体类 / 业务主体创建）
    5.执行业务逻辑的方法，要放在业务主体（数据库实体类）的Service层 或 Mapper层，即围绕 业务主体。返回 Vo类 只是为了让前端拿到更丰富的数据。
    6.Vo类是接口数据模型，它的成员变量由业务场景决定。
    属性背后通常来自 数据库表、关联表、查询条件、计算结果或前端交互需求。
    1）数据库表：业务主体，主表，extends 的表；
    2）关联表：多对多/一对多 关联关系表，也就是 成员变量所属的表；
    3）查询条件：不一定返回给前端，也不一定保存数据库，只是用来查询筛选，例如：ApartmentQueryVo（省份、城市、区的id），用于SQL中的WHERE部分;
    4）计算结果：例如：计算出的平均评分、总评分、总评论数等，这些数据不是数据库字段，但对前端展示很有帮助，例如：ApartmentItemVo (totalRoomCount,freeRoomCount);
    5）前端交互需求：前端页面为了完成某个操作，需要额外传一些数据，但这些数据不一定要保存到数据库。
    它不是数据库字段，也不一定是业务核心数据，只是为了让这次页面操作能完成。例如：captchaKey、captchaCode 或 修改密码。

     */

    @Operation(summary = "根据ID获取公寓详细信息")
    @GetMapping("getDetailById")
    public Result<ApartmentDetailVo> getDetailById(@RequestParam Long id) {
        ApartmentDetailVo apartmentDetailVo = apartmentInfoService.getDetailById(id);
        return Result.ok(apartmentDetailVo);
    }

    @Operation(summary = "根据id删除公寓信息")
    @DeleteMapping("removeById")
    public Result removeById(@RequestParam Long id) {
        apartmentInfoService.removeByApartmentId(id);
        return Result.ok();
    }

    @Operation(summary = "根据id修改公寓发布状态")
    @PostMapping("updateReleaseStatusById")
    public Result updateReleaseStatusById(@RequestParam Long id, @RequestParam ReleaseStatus status) {
        apartmentInfoService.updateReleaseStatusById(id, status);
        return Result.ok();
    }

    @Operation(summary = "根据行政区id查询公寓信息列表")
    @GetMapping("listInfoByDistrictId")
    public Result<List<ApartmentInfo>> listInfoByDistrictId(@RequestParam Long id) {
       List<ApartmentInfo> apartmentInfoList = apartmentInfoService.listInfoByDistrictId(id);
        return Result.ok(apartmentInfoList);
    }
}














