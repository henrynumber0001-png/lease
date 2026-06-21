package com.nocompanyname.lease.common.exception;

import com.nocompanyname.lease.common.result.ResultCodeEnum;
import lombok.Getter;

@Getter
public class LeaseException extends RuntimeException {

    private ResultCodeEnum resultCodeEnum;

    public LeaseException(ResultCodeEnum resultCodeEnum) {//用于捕获 可预测的 业务层面的异常，主动创建并抛出一个 LeaseException，没有底层异常 cause，异常就是message信息
        super(resultCodeEnum.getMessage());
        //调用父类的构造方法，将异常信息传进去，目的是为了后端排查问题、日志、异常链，是后端调用异常信息的渠道
        //父类构造方法的参数列表需要一个字符串参数，而resultCodeEnum的成员变量message刚好是一个字符串，于是就传入这个字符串
        /*
        实际调用的是父类的构造方法：RuntimeException(String message)
        它会把这个字符串存储到 LeaseException类的成员变量（继承自父类）detailMessage，便于后面通过异常类的对象调用异常类的方法，获取该成员变量的值：e.getMessage()
        因为 resultCodeEnum 自带两个成员变量：message 和 code，刚好对应上了构造方法的参数类型 String message
         */
        this.resultCodeEnum = resultCodeEnum; //虽然说resultCodeEnum里就包含message，但这是返给客户端看的，不是给后端开发看的

    }

    public LeaseException(ResultCodeEnum resultCodeEnum, Throwable cause) {//用于捕获 不可预测的 系统异常，只不过用 业务异常 包装给前端看，有底层异常 cause，方便后端日志追踪。
        /*
        Throwable cause = 原始异常原因
        比如 MinIO 上传失败时，底层可能抛出：IOException,ServerException,ErrorResponseException等异常
        这些原始异常会作为 cause 传进来。
         */
        super(resultCodeEnum.getMessage(), cause);
        /*
        调用父类 RuntimeException 的构造方法，做两件事：
        1. 把异常消息设置成 resultCodeEnum.getMessage()
        2. 把原始异常 cause 保存到异常链里（便于后端排查问题）
         */
        this.resultCodeEnum = resultCodeEnum;
    }

    /*
    一句话总结：
    LeaseException(ResultCodeEnum resultCodeEnum) 是给 LeaseException异常 准备的
    LeaseException(ResultCodeEnum resultCodeEnum, Throwable cause) 是给系统异常 准备的，只不过是强行包装成 LeaseException异常，目的是为了前端看到的异常信息统一，后端日志追踪也方便。
    因为lease项目返回前端，统一用的是Result和ResultCodeEnum，所以借用它们的信息统一处理返给前端的异常信息
     */
}
