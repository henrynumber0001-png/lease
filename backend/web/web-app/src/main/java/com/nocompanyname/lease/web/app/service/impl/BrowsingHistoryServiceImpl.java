package com.nocompanyname.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.BrowsingHistory;
import com.nocompanyname.lease.web.app.context.LoginUserHolder;
import com.nocompanyname.lease.web.app.mapper.BrowsingHistoryMapper;
import com.nocompanyname.lease.web.app.service.BrowsingHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.web.app.vo.history.HistoryItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class BrowsingHistoryServiceImpl extends ServiceImpl<BrowsingHistoryMapper, BrowsingHistory>
        implements BrowsingHistoryService {

    @Autowired
    private BrowsingHistoryMapper browsingHistoryMapper;

    @Override
    public IPage<HistoryItemVo> getPage(long current, long size) {

        Long userId = LoginUserHolder.getUserId();

        if(userId == null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }

        Page<HistoryItemVo> page = new Page<>(current, size);
        return browsingHistoryMapper.getPage(page, userId);
    }

    @Override
    public void saveHistory(Long roomId) {

        if(roomId == null){
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }

        Long userId = LoginUserHolder.getUserId();
        if(userId == null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }

        LambdaQueryWrapper<BrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowsingHistory::getUserId, userId)
                .eq(BrowsingHistory::getRoomId, roomId);
        BrowsingHistory browsingHistory = this.getOne(queryWrapper);

        if(browsingHistory == null){
            //为什么这个browsingHistory不能直接用？
            //因为这里的 browsingHistory 只是一个 变量名，并没有创建对象，它不代表BrowsingHistory对象
            //这时候你直接拿来 browsingHistory.setUserId(userId) 就会发生 空指针异常（因为这时候没有browsingHistory堆内存对象）

            browsingHistory = new BrowsingHistory();
            //此时，赋予 browsingHistory 这个变量一个 BrowsingHistory对象
            //这样就可以操作这个对象的setter方法了

            browsingHistory.setUserId(userId);
            browsingHistory.setRoomId(roomId);
            browsingHistory.setBrowseTime(LocalDateTime.now());
            this.save(browsingHistory);

        } else {
            browsingHistory.setBrowseTime(LocalDateTime.now());
            this.updateById(browsingHistory);
        }

    }
}
