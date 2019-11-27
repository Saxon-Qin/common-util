package com.common.util;

import java.util.function.Function;

/**
 * 双重值对象
 *
 * @version V1.0
 **/
public class BiValue<K, V> {
    private K key;
    private V value;

    public K getKey() {
        return key;
    }

    public BiValue<K, V> setKey(K key) {
        this.key = key;
        return this;
    }

    /**
     * 根据函数修改Key
     * 如果对象中Key为空，则将其赋值成传入的Key； 否则，根据传入的函数对值进行处理后赋值给对象的key
     * @param key       新key
     * @param change    修改函数
     * @return          当前对象
     */
    public BiValue<K, V> changeKey(K key, Function<K, K> change) {
        if (null == this.key) {
            this.key = key;
        } else {
            this.key = change.apply(this.key);
        }

        return this;
    }

    /**
     * 根据函数修改Value
     * 如果对象中Value为空，则将其赋值成传入的Value；
     * 否则，根据传入的函数对Value进行处理后赋值成对象的Value
     * @param value     新Value
     * @param change    修改函数
     * @return          当前对象
     */
    public BiValue<K, V> changeValue(V value, Function<V, V> change) {
        if (null == this.value) {
            this.value = value;
        } else {
            this.value = change.apply(this.value);
        }

        return this;
    }

    public V getValue() {
        return value;
    }

    public BiValue<K, V> setValue(V value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return key + ":" + value;
    }
}
