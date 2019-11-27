package com.common.web.util;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Rest操作结果对象
 *
 * @version V1.0
 **/
public class RestResult<T> {
    private String code;
    private String msg;
    private String details;
    private T data;

    private RestResult() {}

    public static RestResult<Void> success() {
        return new RestResult<Void>()
                .code("200")
                .msg("操作成功");
    }

    public static <T> RestResult<T> success(T t) {
        return new RestResult<T>()
                .code("200")
                .msg("操作成功")
                .data(t);
    }

    public static RestResult<Void> fail(String code, String msg) {
        return new RestResult<Void>()
                .code(code)
                .msg(msg);
    }

    public static RestResult<Void> fail(String code) {
        return new RestResult<Void>()
                .code(code);
    }

    public boolean isSucceeded() {
        return "200".equals(this.code);
    }

    public RestResult<T> ifSucceeded(Consumer<T> consumer) {
        consumer.accept(this.data);
        return this;
    }

    public <R> RestResult<R> map(Function<T, R> function) {
        return new RestResult<R>()
                .code(this.code)
                .msg(this.msg)
                .data(function.apply(this.data));
    }

    public  RestResult<T> code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public RestResult<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }

    public RestResult<T> data(T data) {
        this.data = data;
        return this;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return this.data;
    }

    public RestResult<T> details(String details) {
        this.details = details;
        return this;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return this.details;
    }
}
