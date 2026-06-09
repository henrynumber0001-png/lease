package com.nocompanyname.lease.web.app.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsServiceImplTest {

    @Mock
    private Client client;

    @InjectMocks
    private SmsServiceImpl smsService;

    @Test
    void sendSms() throws Exception {
        smsService.sendSms("17309877757", "1234");

        ArgumentCaptor<SendSmsRequest> requestCaptor = ArgumentCaptor.forClass(SendSmsRequest.class);
        verify(client).sendSms(requestCaptor.capture());

        SendSmsRequest request = requestCaptor.getValue();
        assertEquals("17309877757", request.getPhoneNumbers());
        assertEquals("登录/注册模板", request.getSignName());
        assertEquals("100001", request.getTemplateCode());
        assertEquals("{\"code\":\"1234\"}", request.getTemplateParam());
    }
}
