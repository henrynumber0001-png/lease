package com.nocompanyname.lease.web.admin.custom.converter;

import com.nocompanyname.lease.model.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> { //⚠️注意：这是一个转换器制造厂，用于生产转换器，但它本身不是转换器
    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        /*
        在Controller方法的参数中，当type=1传入时，spring识别到参数类型是ItemType，
        于是到ConversionService中去找已注册的转换器，发现了StringToBaseEnumConverterFactory，然后发现满足条件，进而传入targetType = ItemType
        */
        return new Converter<String, T>() { //匿名内部类，临时创建一个Converter对象
            @Nullable
            @Override
            public T convert(String source) {
                T[] enumConstants = targetType.getEnumConstants(); //获得目标枚举类中的所有枚举常量（枚举类实例）
                for (T enumConstant : enumConstants) {
                    if (enumConstant.getCode().equals(Integer.valueOf(source))) { //比较枚举常量的code值和输入的字符串转换成的整数值是否相等
                        return enumConstant;
                    }
                }
                throw new IllegalArgumentException("Unknown enum constant: " + source);
            }
        };
    }
}
