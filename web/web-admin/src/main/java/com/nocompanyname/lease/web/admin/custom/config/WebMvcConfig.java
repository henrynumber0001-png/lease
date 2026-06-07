package com.nocompanyname.lease.web.admin.custom.config;

import com.nocompanyname.lease.web.admin.custom.context.LoginUserHolder;
import com.nocompanyname.lease.web.admin.custom.converter.StringToBaseEnumConverterFactory;
import com.nocompanyname.lease.web.admin.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private StringToBaseEnumConverterFactory stringToBaseEnumConverterFactory;

    private final LoginInterceptor loginInterceptor;

    //构造方法注入 LoginInterceptor
    public WebMvcConfig(LoginInterceptor loginInterceptor) {
        this. loginInterceptor =  loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login", "/admin/login/captcha");
        /*
        登录和获取验证码时还没有 JWT，因此必须排除。
        /admin/info 没有被排除，所以会经过拦截器。
         */
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(this.stringToBaseEnumConverterFactory);
//        registry.addConverterFactory(new StringToBaseEnumConverterFactory());
    }



}
