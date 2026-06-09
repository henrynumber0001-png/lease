package com.nocompanyname.lease.web.app.interceptor;

import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.common.utils.JwtUtil;
import com.nocompanyname.lease.web.app.context.LoginUserHolder;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AppLoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AppLoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");

        if(!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }

        String token = authorization.substring(7);

        try{
            Long userId = jwtUtil.getUserId(token);
            LoginUserHolder.setUserId(userId);
        }catch (ExpiredJwtException e){//如果token过期，抛异常
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch(JwtException | IllegalArgumentException e){ //其他token异常（被篡改、格式错误、签名验证失败、token未生效，token是空字符串 等）
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
        return true;
    }
}
