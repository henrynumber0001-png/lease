package com.nocompanyname.lease.web.app.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.nocompanyname.lease.common.sms.AliyunSMSProperties;
import com.nocompanyname.lease.web.app.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private AliyunSMSProperties properties;

    private final Client client;

    public SmsServiceImpl(Client client) {
        //这是构造方法注入Client对象
        //但是参数列表只在创建 SmsServiceImpl 对象时存在，创建完成即退出(数据局部变量)
        //因此需要再创建一个 成员变量 client，然后将 Bean实例 client 注入到成员变量中
        this.client = client;
        //建立与阿里云客户端的连接
    }

    @Override
    public void sendSms(String phone, String code) {
         SendSmsRequest request = new SendSmsRequest();
         //SendSmsRequest类 来自 阿里云SDK (dysmsapi20170525)
         //一个专门用来描述“这条短信应该怎么发”的 Java 对象，里面装载短信发送参数（快递单号）

         request.setPhoneNumbers(phone); //发给谁
         request.setTemplateCode(properties.getTemplateCode()); //需要使用的 短信模板（写在application.yml中，解耦）
         request.setSignName(properties.getSignName()); //短信签名（标题）
         request.setTemplateParam("{\"code\":\"" + code + "\"}"); //设置验证码变量，JSON字符串类型{"code":"123456"}
        //保存本次短信的发送参数

         try{
             client.sendSms(request); //将短信请求参数 发送至 阿里云服务器（从而完成整个“发送短信”的业务动作）
         }catch(Exception e){
             throw new RuntimeException(e);
         }
    }
}
