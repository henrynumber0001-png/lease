package com.nocompanyname.lease.web.admin.controller.apartment;


import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.model.entity.RoomInfo;
import com.nocompanyname.lease.model.enums.ReleaseStatus;
import com.nocompanyname.lease.web.admin.service.RoomInfoService;
import com.nocompanyname.lease.web.admin.vo.room.RoomDetailVo;
import com.nocompanyname.lease.web.admin.vo.room.RoomItemVo;
import com.nocompanyname.lease.web.admin.vo.room.RoomQueryVo;
import com.nocompanyname.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "房间信息管理")
@RestController
@RequestMapping("/admin/room")
public class RoomController {

    @Autowired
    private RoomInfoService roomInfoService;

    @Operation(summary = "保存或更新房间信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody RoomSubmitVo roomSubmitVo) {
        roomInfoService.saveOrUpdateByRoom(roomSubmitVo);
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询房间列表")
    @GetMapping("pageItem")
    public Result<IPage<RoomItemVo>> pageItem(@RequestParam long current, @RequestParam long size, RoomQueryVo queryVo) {
        IPage<RoomItemVo> page = roomInfoService.getBypageItem(current, size, queryVo);
        return Result.ok(page);
    }

    @Operation(summary = "根据id获取房间详细信息")
    @GetMapping("getDetailById")
    public Result<RoomDetailVo> getDetailById(@RequestParam Long id) {
        RoomDetailVo detailById = roomInfoService.getDetailById(id);
        return Result.ok(detailById);
    }

    @Operation(summary = "根据id删除房间信息")
    @DeleteMapping("removeById")
    public Result removeById(@RequestParam Long id) {
        roomInfoService.removeByRoomId(id);
        return Result.ok();
    }

    @Operation(summary = "根据id修改房间发布状态")
    @PostMapping("updateReleaseStatusById")
    public Result updateReleaseStatusById(@RequestParam Long id, @RequestParam ReleaseStatus status) {
        roomInfoService.updateReleaseStatusById(id, status);
        //Service层统一采用void + 全局异常处理 的模式
        //这样可以知道异常类型和原因，并且能返回到Controller层的，一定是Result.ok()的
        //如果Service层返回值写Result，就与Controller层绑死了；如果写boolean，就不会知道false的具体原因

        return Result.ok();
    }

    @GetMapping("listBasicByApartmentId")
    @Operation(summary = "根据公寓id查询房间列表")
    public Result<List<RoomInfo>> listBasicByApartmentId(@RequestParam Long id) {
        List<RoomInfo> roomInfoList = roomInfoService.listBasicByApartmentId(id);
        return Result.ok(roomInfoList);
    }

}


















