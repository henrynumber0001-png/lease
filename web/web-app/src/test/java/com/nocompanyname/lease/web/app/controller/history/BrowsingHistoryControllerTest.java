package com.nocompanyname.lease.web.app.controller.history;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.common.exception.GlobalExceptionHandler;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.common.utils.JwtUtil;
import com.nocompanyname.lease.web.app.context.LoginUserHolder;
import com.nocompanyname.lease.web.app.interceptor.AppLoginInterceptor;
import com.nocompanyname.lease.web.app.service.BrowsingHistoryService;
import com.nocompanyname.lease.web.app.vo.history.HistoryItemVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BrowsingHistoryControllerTest {

    private static final Long USER_ID = 7L;
    private static final String PHONE = "13800138000";

    private BrowsingHistoryService browsingHistoryService;
    private JwtUtil jwtUtil;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        browsingHistoryService = mock(BrowsingHistoryService.class);
        jwtUtil = new JwtUtil(
                "web-app-test",
                "test-secret-key-that-is-at-least-32-bytes!",
                3600
        );

        BrowsingHistoryController controller = new BrowsingHistoryController();
        ReflectionTestUtils.setField(controller, "browsingHistoryService", browsingHistoryService);

        AppLoginInterceptor loginInterceptor = new AppLoginInterceptor(jwtUtil);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(loginInterceptor)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void clearLoginUser() {
        LoginUserHolder.remove();
    }

    @Test
    void saveHistoryWithValidTokenUsesLoggedInUser() throws Exception {
        doAnswer(invocation -> {
            assertEquals(USER_ID, LoginUserHolder.getUserId());
            return null;
        }).when(browsingHistoryService).saveHistory(88L);

        mockMvc.perform(post("/app/history/saveHistory")
                        .header("Authorization", bearerToken())
                        .param("roomId", "88"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCodeEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(ResultCodeEnum.SUCCESS.getMessage()));

        verify(browsingHistoryService).saveHistory(88L);
        assertNull(LoginUserHolder.getUserId());
    }

    @Test
    void pageWithValidTokenReturnsCurrentUsersHistory() throws Exception {
        HistoryItemVo history = new HistoryItemVo();
        history.setId(10L);
        history.setUserId(USER_ID);
        history.setRoomId(88L);
        history.setRoomNumber("A101");
        history.setRent(new BigDecimal("2500.00"));
        history.setBrowseTime(LocalDateTime.of(2026, 6, 15, 20, 30));

        Page<HistoryItemVo> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(history));
        when(browsingHistoryService.getPage(1, 10)).thenReturn(page);

        mockMvc.perform(get("/app/history/pageItem")
                        .header("Authorization", bearerToken())
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCodeEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].userId").value(USER_ID))
                .andExpect(jsonPath("$.data.records[0].roomId").value(88))
                .andExpect(jsonPath("$.data.records[0].roomNumber").value("A101"));

        verify(browsingHistoryService).getPage(1, 10);
        assertNull(LoginUserHolder.getUserId());
    }

    @Test
    void requestWithoutTokenIsRejectedBeforeController() throws Exception {
        mockMvc.perform(post("/app/history/saveHistory")
                        .param("roomId", "88"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCodeEnum.APP_LOGIN_AUTH.getCode()))
                .andExpect(jsonPath("$.message").value(ResultCodeEnum.APP_LOGIN_AUTH.getMessage()));

        verify(browsingHistoryService, never()).saveHistory(anyLong());
    }

    private String bearerToken() {
        return "Bearer " + jwtUtil.createToken(USER_ID, PHONE);
    }
}
