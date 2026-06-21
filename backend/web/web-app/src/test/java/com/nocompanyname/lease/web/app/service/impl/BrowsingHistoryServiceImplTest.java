package com.nocompanyname.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.BrowsingHistory;
import com.nocompanyname.lease.web.app.context.LoginUserHolder;
import com.nocompanyname.lease.web.app.mapper.BrowsingHistoryMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BrowsingHistoryServiceImplTest {

    private BrowsingHistoryMapper browsingHistoryMapper;
    private BrowsingHistoryServiceImpl service;

    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                BrowsingHistory.class
        );
    }

    @BeforeEach
    void setUp() {
        browsingHistoryMapper = mock(BrowsingHistoryMapper.class);
        service = new BrowsingHistoryServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", browsingHistoryMapper);
        ReflectionTestUtils.setField(service, "browsingHistoryMapper", browsingHistoryMapper);
        LoginUserHolder.setUserId(7L);
    }

    @AfterEach
    void clearLoginUser() {
        LoginUserHolder.remove();
    }

    @Test
    void saveHistoryInsertsFirstVisitWithBrowseTime() {
        when(browsingHistoryMapper.selectOne(any(), eq(true))).thenReturn(null);
        when(browsingHistoryMapper.insert(any(BrowsingHistory.class))).thenReturn(1);

        LocalDateTime before = LocalDateTime.now();
        service.saveHistory(88L);
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<BrowsingHistory> captor = ArgumentCaptor.forClass(BrowsingHistory.class);
        verify(browsingHistoryMapper).insert(captor.capture());
        BrowsingHistory inserted = captor.getValue();

        assertEquals(7L, inserted.getUserId());
        assertEquals(88L, inserted.getRoomId());
        assertNotNull(inserted.getBrowseTime());
        assertTrue(!inserted.getBrowseTime().isBefore(before));
        assertTrue(!inserted.getBrowseTime().isAfter(after));
        verify(browsingHistoryMapper, never()).updateById(any(BrowsingHistory.class));
    }

    @Test
    void saveHistoryUpdatesBrowseTimeForRepeatVisit() {
        BrowsingHistory existing = new BrowsingHistory();
        existing.setId(15L);
        existing.setUserId(7L);
        existing.setRoomId(88L);
        existing.setBrowseTime(LocalDateTime.of(2026, 1, 1, 0, 0));

        when(browsingHistoryMapper.selectOne(any(), eq(true))).thenReturn(existing);
        when(browsingHistoryMapper.updateById(any(BrowsingHistory.class))).thenReturn(1);

        service.saveHistory(88L);

        ArgumentCaptor<BrowsingHistory> captor = ArgumentCaptor.forClass(BrowsingHistory.class);
        verify(browsingHistoryMapper).updateById(captor.capture());
        assertTrue(captor.getValue().getBrowseTime().isAfter(LocalDateTime.of(2026, 1, 1, 0, 0)));
        verify(browsingHistoryMapper, never()).insert(any(BrowsingHistory.class));
    }

    @Test
    void saveHistoryRejectsUnauthenticatedRequest() {
        LoginUserHolder.remove();

        LeaseException exception = assertThrows(
                LeaseException.class,
                () -> service.saveHistory(88L)
        );

        assertEquals(ResultCodeEnum.APP_LOGIN_AUTH, exception.getResultCodeEnum());
        verify(browsingHistoryMapper, never()).selectOne(any(), eq(true));
    }
}
