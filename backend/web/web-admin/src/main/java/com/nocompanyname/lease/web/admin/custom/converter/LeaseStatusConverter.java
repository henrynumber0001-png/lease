//package com.nocompanyname.lease.web.admin.custom.converter;
//
//import com.nocompanyname.lease.model.enums.LeaseStatus;
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.stereotype.Component;
//
//@Component
//public class LeaseStatusConverter implements Converter<String, LeaseStatus>{
//    @Override
//    public LeaseStatus convert(String source) {
//        try {
//            int code = Integer.parseInt(source);
//            for (LeaseStatus leaseStatus : LeaseStatus.values()) {
//                if (leaseStatus.getCode() == code) {
//                    return leaseStatus;
//                }
//            }
//        } catch (NumberFormatException e) {
//            // 处理解析错误
//            e.printStackTrace();
//        }
//        throw new IllegalArgumentException("LeaseStatus code" + source + "is not found");
//    }
//}

