package com.common.util;

/**
 * 单值对象
 * 线程不安全
 *
 * @version V1.0
 **/
public class SingleValue<T> {
    private T value;

    public boolean isNull() {
        return null == value || "".equals(value.toString().trim());
    }

    public boolean isNotNull() {
        return !isNull();
    }

    public T getValue() {
        return value;
    }

    public T getAndSet(T t) {
        T oldValue = value;
        this.value = t;
        return oldValue;
    }

    public SingleValue<T> setValue(T value) {
        this.value = value;
        return this;
    }
}
