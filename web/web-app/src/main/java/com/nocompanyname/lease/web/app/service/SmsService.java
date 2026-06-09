package com.nocompanyname.lease.web.app.service;

public interface SmsService {

    void sendSms(String phone, String code) throws Exception;
}
