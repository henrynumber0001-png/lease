package com.nocompanyname.lease.web.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nocompanyname.lease.model.entity.BrowsingHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nocompanyname.lease.web.app.vo.history.HistoryItemVo;


public interface BrowsingHistoryService extends IService<BrowsingHistory> {
    IPage<HistoryItemVo> getPage(long current, long size);
}
