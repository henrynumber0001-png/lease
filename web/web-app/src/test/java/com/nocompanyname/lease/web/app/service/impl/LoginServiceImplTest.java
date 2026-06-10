package com.nocompanyname.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.redisConstant.RedisConstant;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.common.utils.JwtUtil;
import com.nocompanyname.lease.model.entity.UserInfo;
import com.nocompanyname.lease.model.enums.BaseStatus;
import com.nocompanyname.lease.web.app.context.LoginUserHolder;
import com.nocompanyname.lease.web.app.mapper.UserInfoMapper;
import com.nocompanyname.lease.web.app.service.SmsService;
import com.nocompanyname.lease.web.app.vo.user.LoginVo;
import com.nocompanyname.lease.web.app.vo.user.UserInfoVo;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                UserInfo.class
        );
    }

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SmsService smsService;

    @Mock
    private UserInfoMapper userInfoMapper;

    @Mock
    private JwtUtil jwtUtil;

    @AfterEach
    void clearLoginUser() {
        LoginUserHolder.remove();
    }

    @Test
    void getCodeRejectsRequestsWithinResendWindow() {
        LoginServiceImpl service = createService();
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("1"), eq(60L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        LeaseException exception = assertThrows(
                LeaseException.class,
                () -> service.getCode("13800138000")
        );

        assertEquals(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN, exception.getResultCodeEnum());
        verify(smsService, never()).sendSms(anyString(), anyString());
    }

    @Test
    void getCodeStoresAndSendsCode() {
        LoginServiceImpl service = createService();
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("1"), eq(60L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        service.getCode(" 13800138000 ");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(
                eq(RedisConstant.APP_LOGIN_PREFIX + "13800138000"),
                codeCaptor.capture(),
                eq(RedisConstant.APP_LOGIN_CODE_TTL_SEC.longValue()),
                eq(TimeUnit.SECONDS)
        );
        assertEquals(4, codeCaptor.getValue().length());
        verify(smsService).sendSms("13800138000", codeCaptor.getValue());
    }

    @Test
    void loginKeepsCodeWhenSubmittedCodeIsWrong() {
        LoginServiceImpl service = createService();
        LoginVo loginVo = loginVo("13800138000", "0000");
        when(stringRedisTemplate.execute(
                any(RedisScript.class),
                eq(List.of(
                        RedisConstant.APP_LOGIN_PREFIX + "13800138000",
                        RedisConstant.APP_LOGIN_PREFIX + "attempt:13800138000"
                )),
                eq("0000"),
                eq("600"),
                eq("5")
        )).thenReturn(0L);

        LeaseException exception = assertThrows(LeaseException.class, () -> service.login(loginVo));

        assertEquals(ResultCodeEnum.APP_LOGIN_CODE_ERROR, exception.getResultCodeEnum());
        verify(userInfoMapper, never()).selectOne(any());
    }

    @Test
    void loginConsumesCorrectCodeAndReturnsToken() {
        LoginServiceImpl service = createService();
        LoginVo loginVo = loginVo("13800138000", "1234");
        UserInfo userInfo = new UserInfo();
        userInfo.setId(7L);
        userInfo.setPhone("13800138000");
        userInfo.setStatus(BaseStatus.ENABLE);

        when(stringRedisTemplate.execute(
                any(RedisScript.class),
                any(List.class),
                eq("1234"),
                eq("600"),
                eq("5")
        ))
                .thenReturn(1L);
        when(userInfoMapper.selectOne(any())).thenReturn(userInfo);
        when(jwtUtil.createToken(7L, "13800138000")).thenReturn("token");

        assertEquals("token", service.login(loginVo));
    }

    @Test
    void getInfoRejectsDisabledUser() {
        LoginServiceImpl service = createService();
        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(BaseStatus.DISABLE);
        LoginUserHolder.setUserId(7L);
        when(userInfoMapper.selectById(7L)).thenReturn(userInfo);

        LeaseException exception = assertThrows(LeaseException.class, service::getInfo);

        assertEquals(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR, exception.getResultCodeEnum());
    }

    @Test
    void getInfoMapsUserProfile() {
        LoginServiceImpl service = createService();
        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(BaseStatus.ENABLE);
        userInfo.setNickname("tester");
        userInfo.setAvatarUrl("avatar.png");
        LoginUserHolder.setUserId(7L);
        when(userInfoMapper.selectById(7L)).thenReturn(userInfo);

        UserInfoVo result = service.getInfo();

        assertEquals("tester", result.getNickname());
        assertEquals("avatar.png", result.getAvatarUrl());
    }

    private LoginServiceImpl createService() {
        return new LoginServiceImpl(stringRedisTemplate, smsService, userInfoMapper, jwtUtil);
    }

    private LoginVo loginVo(String phone, String code) {
        LoginVo loginVo = new LoginVo();
        loginVo.setPhone(phone);
        loginVo.setCode(code);
        return loginVo;
    }
}
