package com.nocompanyname.lease.web.app.context;

/*
LoginUserHolder类的作用
在一次请求处理过程中，临时保存当前登录用户的 userId，让后面的 Controller、Service 可以随时取到。

final 表示这个类不能被继承，它只是一个工具类，不希望别人继承它。
因为是工具类，因此也不需要加 @Component。
 */
public final class LoginUserHolder {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    //给当前线程单独准备一个变量，用来存 Long类型的 userId
    //为什么用 ThreadLocal?
    //ThreadLocal 是线程安全的，每个线程都有自己的副本，不会相互影响，适合在多线程环境下使用，避免了线程安全问题。

    private static final ThreadLocal<String> USER_PHONE_HOLDER = new ThreadLocal<>();

    private LoginUserHolder() {
    }
    //private 构造方法：表示 不允许别人 new 这个类


    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }
    //用于保存 当前用户ID 到 当前线程里

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }
    //获取当前用户ID

    public static void remove(){
        USER_ID_HOLDER.remove();
    }
    /*
    清除当前线程里的 userId。
    这是非常重要的一步。

    因为 Spring Boot 的请求线程通常来自线程池。
    线程处理完用户A的请求后，不会立刻销毁，而是可能继续处理用户B的请求。

    如果不清除，可能出现：
    线程1处理用户A：userId = 1001
    请求结束，但没有 remove
    线程1又处理用户B
    用户B可能读到 userId = 1001
     */

    public static void setUserPhone(String phone){
        USER_PHONE_HOLDER.set(phone);
    }

    public static String getUserPhone(){
        return USER_PHONE_HOLDER.get();
    }

    public static void removeUserPhone(){
        USER_PHONE_HOLDER.remove();
    }
}
