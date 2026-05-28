package com.nocompanyname.lease.common.exception;

import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LeaseException.class) // 根据 throw 出来的 异常类型 判断使用哪一个异常方法处理
    public Result<Void> handleLeaseException(LeaseException e) { //e不是参数名，而是这个异常类的对象本身
        log.error("业务异常：{}", e.getMessage(), e); //记录异常日志，方便后端排查问题
        return Result.build(null, e.getResultCodeEnum());
        //将异常信息封装成统一的返回结果，返给客户端
        //getResultCodeEnum()是 LeaseException 的 get方法，因为写了@getter + 成员变量resultCodeEnum，所以可以获得这个成员变量的值（异常的传入信息）

    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e); //记录异常日志，方便后端排查问题
        return Result.build(null, ResultCodeEnum.SERVICE_ERROR); //系统异常，统一返回“服务异常”提醒，不暴露具体异常信息给客户端
    }
}
