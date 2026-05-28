//package com.nocompanyname.lease.web.admin.custom.converter;
//
//
//import org.springframework.core.convert.converter.Converter;
//import com.nocompanyname.lease.model.enums.ItemType;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ItemTypeConverter implements Converter<String,ItemType> {
//    @Override
//    public ItemType convert(String source) {
//        try {
//            int code = Integer.parseInt(source);
//            for (ItemType itemType : ItemType.values()) {
//                if (itemType.getCode() == code) {
//                    return itemType;
//                }
//            }
//        } catch (NumberFormatException e) {
//            // 处理解析错误
//            e.printStackTrace();
//        }
//        throw new IllegalArgumentException("ItemType code" + source + "is not found");
//
//    }
//
//}
