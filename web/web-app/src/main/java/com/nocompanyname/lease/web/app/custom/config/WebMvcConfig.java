package com.nocompanyname.lease.web.app.custom.config;

import com.nocompanyname.lease.web.app.interceptor.AppLoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

//    @Autowired
//    private StringToBaseEnumConverterFactory stringToBaseEnumConverterFactory;

    private final AppLoginInterceptor loginInterceptor;

    //构造方法注入 LoginInterceptor
    public WebMvcConfig(AppLoginInterceptor loginInterceptor) {
        this. loginInterceptor =  loginInterceptor;
    }

    /*

    Spring MVC 收到请求后，根据配置的 URL 匹配规则判断是否应用拦截器。
    匹配成功就自动调用拦截器。
    拦截器本身不主动扫描或调用 Controller。
    WebMvcConfig 是注册规则，Interceptor 是具体处理逻辑。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/app/**")
                .excludePathPatterns( "/app/login/**",
                        "/app/region/**",
                        "/app/apartment/**",
                        "/app/room/**");
        /*
        登录和获取验证码时还没有 JWT，因此必须排除。
        /app/history/** 没有被排除，所以会经过拦截器。
         */
    }

//    @Override
//    public void addFormatters(FormatterRegistry registry) {
//        registry.addConverterFactory(this.stringToBaseEnumConverterFactory);
//    }

}
