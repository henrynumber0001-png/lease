package com.nocompanyname.lease.web.app.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.nocompanyname.lease.web.app.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    private final Client client;

    @Autowired
    public SmsServiceImpl(Client client) {
        this.client = client;
    }

    @Override
    public void sendSms(String phone, String code) {
         SendSmsRequest request = new SendSmsRequest();
         request.setPhoneNumbers(phone);
         request.setTemplateCode("SMS_334990739");
         request.setSignName("验证码模板");
         request.setTemplateParam("{\"code\":\"" + code + "\"}");

         try{
             client.sendSms(request);
         }catch(Exception e){
             throw new RuntimeException(e);
         }
    }
}
