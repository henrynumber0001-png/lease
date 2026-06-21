package com.nocompanyname.lease.web.admin.interceptor;

import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.common.utils.JwtUtil;
import com.nocompanyname.lease.web.admin.custom.context.LoginUserHolder;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
/*
类的作用：
每次访问需要登录的接口之前，先检查请求里有没有合法的JWT；合法就放行，不合法就拒绝。
 */
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public LoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    //用构造器 注入 JwtUtil（解析、验证 token，并 从token中获取 userId，存入 ThreadLocal)


    //前端的请求，例如：GET /admin/user/info
    //如果 preHandle() 返回 true，继续执行 Controller。
    //如果返回 false，Controller 将不会执行。
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){

        //从请求头里获取 Authorization，例如：Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...（Bearer 后面的部分才是真正的 JWT）
        String authorization = request.getHeader("Authorization");

        if(!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")){
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }
        //authorization 为null或空字符串，或者不是以 Bearer 开头，说明 浏览器没有正确携带 token，于是抛出异常

        String token = authorization.substring(7);
        //从 authorization 中获取 真正的 token（Bearer 后面的部分，从第7位索引开始）


        //真正验证token的地方
        try{
            Long userId = jwtUtil.getUserId(token);
            /*
            解析JWT token、验证签名、检查是否过期、检查issuer, 取出 subject 里的 userId
            parseToken(token).getSubject();
             */

            LoginUserHolder.setUserId(userId);
            //把当前用户 ID 存到 ThreadLocal 里。
            //这样后面的 Controller 或 Service 就可以直接调用 拦截器的getUserId() 使用这个 userId, 拿到当前登录用户。

        } catch (ExpiredJwtException e){//如果token过期，抛异常
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch(JwtException | IllegalArgumentException e){ //其他token异常（被篡改、格式错误、签名验证失败、token未生效，token是空字符串 等）
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
        return true;
        /*
        到这一步，说明：
        Authorization 有值
        格式正确：Bearer xxx
        token 未过期且没有被篡改或其他错误
        成功获取 userId

        拦截器放行，request进入Controller
         */
    }

    @Override
    //整个请求处理完成后执行。
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        LoginUserHolder.remove();
        /*
        LoginUserHolder 使用了 ThreadLocal。
        而 Web 服务器的线程会复用。
        不清理可能导致下一个请求读取到旧用户 ID。
         */
    }
}
