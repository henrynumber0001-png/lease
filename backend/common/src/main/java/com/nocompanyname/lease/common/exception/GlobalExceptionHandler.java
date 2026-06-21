package com.nocompanyname.lease.common.exception;

import com.nocompanyname.lease.common.result.Result;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LeaseException.class) // 这里接收的 是你在代码中 自己写的 主动抛出来的LeaseException异常
    public Result<Void> handleLeaseException(LeaseException e) { //e不是参数名，而是这个异常类的对象本身
        log.error("业务异常：{}", e.getMessage(), e);
        //记录异常日志，方便后端排查问题。
        // 这个getMessage() 是 LeaseException类的成员变量message，不是 LeaseException的构造方法的参数 resultCodeEnum的成员变量

        return Result.build(null, e.getResultCodeEnum());
        //将异常信息封装成统一的返回结果，返给客户端
        //getResultCodeEnum()是 LeaseException 的 get方法，因为写了@getter + 成员变量resultCodeEnum，所以可以获得这个成员变量的值（异常的传入信息）

    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class}) // 这里接收的 不是你主动 throw 出来的异常
    public Result<Void> handleException(Exception e) {
        log.error("参数异常：{}", e.getMessage(), e); //记录异常日志，方便后端排查问题
        return Result.build(null, ResultCodeEnum.PARAM_ERROR);
    }

    /*
    @ExceptionHandler(...) 作用：声明所需要处理的 异常 的类型。
    也就是说，抛出来的异常中，哪些是归这个@ExceptionHandler(...)处理的,在参数中写出来。
    这样一旦有所包含的异常抛出，可以被接收并处理。

    所以：
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    表示：当抛出的异常是IllegalArgumentException.class 或者 MethodArgumentTypeMismatchException.class时，可以被接收并处理。
     */

    /*
    handleException(Exception e)
    作用：进入这个方法后，用什么类型的变量接收异常对象。
    因为：
    IllegalArgumentException extends RuntimeException extends Exception
    MethodArgumentTypeMismatchException extends TypeMismatchException extends NestedRuntimeException
    它们的共同父类是 Exception。

    所以最终从@ExceptionHandler 到 Exception e 的过程，就是：
    Exception e = new IllegalArgumentException(...);
    Exception e = new MethodArgumentTypeMismatchException(...);

    最后总结：
    Exception e 只是负责在方法体中实现具体的代码逻辑，
    真正接收以及处理哪一个异常类型，是 @ExceptionHandler 说了算的。

     */
}
