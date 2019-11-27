package com.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

/**
 * Map类型对象增强器
 *
 * @version V1.0
 **/
public class MapEnhancer<V> {
    private Map<String, V> map;

    private MapEnhancer() {
    }

    /**
     * 创建一个Map增强器
     *
     * @param <V> Value类型
     * @return 增强器
     */
    public static <V> MapEnhancer<V> of() {
        MapEnhancer<V> mapEnhancer = new MapEnhancer<>();
        mapEnhancer.map = new HashMap<>(16);
        return mapEnhancer;
    }

    /**
     * 创建一个Map增强器
     *
     * @param <V> Value类型
     * @return 增强器
     */
    public static <V> MapEnhancer<V> of(int size) {
        MapEnhancer<V> mapEnhancer = new MapEnhancer<>();
        mapEnhancer.map = new HashMap<>(size);
        return mapEnhancer;
    }

    /**
     * 根据指定的Map创建一个Map增强器
     *
     * @param <V> Value类型
     * @return 增强器
     */
    public static <V> MapEnhancer<V> of(Map<String, V> map) {
        MapEnhancer<V> mapEnhancer = new MapEnhancer<>();
        mapEnhancer.map = null == map ? new HashMap<>(16) : map;
        return mapEnhancer;
    }

    /**
     * 往Map中增加数据
     *
     * @param k     键
     * @param value 值
     * @return 当前增强器
     */
    public MapEnhancer<V> put(String k, V value) {
        if (null != value) {
            this.map.put(k, value);
        }
        return this;
    }

    /**
     * 根据条件判断是否往Map增加数据
     *
     * @param k        键
     * @param value    值
     * @param supplier 判断条件
     * @return 当前增强器
     */
    public MapEnhancer<V> putIf(
            String k,
            V value,
            BooleanSupplier supplier) {
        if (supplier.getAsBoolean()) {
            this.map.put(k, value);
        }

        return this;
    }

    /**
     * 批量增加键值对象
     *
     * @param supplier 键值对提供者
     * @return 当前增强器
     */
    public MapEnhancer<V> puts(Supplier<Map<String, V>> supplier) {
        Map<String, V> newMap = supplier.get();
        if (null != newMap) {
            this.map.putAll(newMap);
        }

        return this;
    }

    /**
     * 针对当前Map对象进行处理
     *
     * @param mapConsumer 处理器
     * @return 当前增强器
     */
    public MapEnhancer<V> peek(Consumer<Map<String, V>> mapConsumer) {
        mapConsumer.accept(this.map);
        return this;
    }

    /**
     * 遍历处理每一个元素
     *
     * @param biConsumer 处理器
     * @return 当前增强器
     */
    public MapEnhancer<V> forEach(BiConsumer<String, V> biConsumer) {
        this.map.forEach(biConsumer);
        return this;
    }

    /**
     * 修改Key对象的值
     *
     * @param key            键
     * @param changeFunction 修改函数
     * @return 当前增强器本身
     */
    public MapEnhancer<V> changeValue(String key, Function<V, V> changeFunction) {
        map.put(key, changeFunction.apply(map.get(key)));
        return this;
    }

    /**
     * 清空所有数据
     *
     * @return 当前增强器
     */
    public MapEnhancer<V> clear() {
        this.map.clear();
        return this;
    }

    /**
     * 删除某个键对应的数据
     *
     * @param k 键
     * @return 当前增强器
     */
    public MapEnhancer<V> remove(String k) {
        this.map.remove(k);
        return this;
    }

    /**
     * 生成Map对象
     *
     * @return 生成的Map对象
     */
    public Map<String, V> build() {
        return map;
    }
}
