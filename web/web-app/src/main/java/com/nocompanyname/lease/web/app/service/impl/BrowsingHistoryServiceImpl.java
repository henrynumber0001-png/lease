package com.nocompanyname.lease.web.app.service.impl;

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
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
}