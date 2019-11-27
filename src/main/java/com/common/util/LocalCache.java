package com.common.util;

import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 服务器本地缓存
 *
 * @version V1.0
 **/
public class LocalCache {

    /**
     * 数据Map
     */
    private Map<String, CacheValue> dataMap;

    public LocalCache() {
        this.dataMap = new ConcurrentHashMap<>(16);
        new Thread(() -> {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ignored) {
            }

            clearTimeoutData();
        }).start();
    }

    public void clearTimeoutData() {
        for (String key : dataMap.keySet()) {
            CacheValue value = dataMap.get(key);
            if (null == value || !value.isAlive()) {
                dataMap.remove(key);
            }
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        this.dataMap.clear();
    }

    /**
     * 缓存对象
     */
    public void cache(String key, Object value) {
        this.dataMap.put(key, new CacheValue(ObjectUtils.clone(value)));
    }

    /**
     * 缓存对象，指定对象的存活时间（分钟)
     */
    public void cacheWithTimeoutInMinutes(String key, Object value, long aliveTimeInMinutes) {
        this.dataMap.put(key, new CacheValue(ObjectUtils.clone(value), aliveTimeInMinutes * 60));
    }

    /**
     * 查询缓存中存储的List类型对象，如果不存在则返回Null
     *
     * @param key 关键字
     * @param <T> List存储对象类型
     * @return List类型的对象，注意返回的对象不能修改，否则会抛出异常
     */
    public <T> List<T> getList(String key) {
        CacheValue cacheValue = this.dataMap.get(key);
        if (null != cacheValue && null != cacheValue.getValue()) {
            //noinspection unchecked
            List<T> list = (List<T>) cacheValue.getValue();
            return Collections.unmodifiableList(list);
        }

        return null;
    }

    /**
     * 查询缓存中存储的List类型对象，如果不存在则使用supplier生成List对象并保存到缓存中，然后返回生成的对象
     *
     * @param key                关键字
     * @param supplier           当缓存中不存在时的数据提供者
     * @param aliveTimeInMinutes 缓存失效时间
     * @param <T>                数据类型
     * @return 缓存的数据，注意返回的对象不能修改，否则会抛出异常
     */
    public <T> List<T> getList(String key, Supplier<List<T>> supplier, long aliveTimeInMinutes) {
        Optional<List<T>> list = getOrCache(key, supplier, aliveTimeInMinutes);
        return list.map(Collections::unmodifiableList).orElseGet(() -> Collections.unmodifiableList(new ArrayList<>(0)));
    }

    /**
     * 从缓存中获取指定对象
     * 注意对返回的对象进行修改并不会影响缓存中的对象
     */
    public <T> Optional<T> get(String key) {
        CacheValue cacheValue = this.dataMap.get(key);
        if (null == cacheValue || null == cacheValue.getValue()) {
            return Optional.empty();
        }

        //noinspection unchecked
        return Optional.ofNullable((T) cacheValue.getValue()).map(ObjectUtils::clone);
    }

    /**
     * 从缓存中获取指定对象，如果对象不存在则返回指定的默认对象
     *
     * @param key      Key
     * @param defValue 默认值
     * @param <T>      对象类型
     * @return 返回缓存中的对象或者默认值，注意对返回的对象进行修改并不会影响缓存中的对象
     */
    public <T> T get(String key, T defValue) {
        return this.<T>get(key).map(ObjectUtils::clone).orElse(defValue);
    }

    /**
     * 从缓存中获取指定对象，如果对象不存在则返回指定的Supplier提供的值
     *
     * @param key      Key
     * @param defValue 默认值
     * @param <T>      对象类型
     * @return 返回缓存中的对象或者默认值，注意对返回的对象进行修改并不会影响缓存中的对象
     */
    public <T> T get(String key, Supplier<T> supplier) {
        return this.<T>get(key).map(ObjectUtils::clone).orElseGet(supplier);
    }

    /**
     * 从缓存中获取指定对象，如果对象不存在则调用supplier生成对象后进行缓存再返回
     * @param key   Key
     * @param supplier 值生成器
     * @param <T>   值对象类型
     * @return  缓存的值或者是Supplier生成的值
     */
    public <T> T getOrCache(String key, Supplier<T> supplier) {
        return this.<T>get(key).orElseGet(() -> {
            T v = supplier.get();
            if (null != v) {
                cache(key, v);
            }

            return v;
        });
    }

    /**
     * 从缓存中获取对象，如果对象不存在则使用supplier生成值，并将生成值缓存后返回
     *
     * @param <T>                存储的对象类型
     * @param key                关键字
     * @param supplier           当缓存中不存在数据时的数据提供者
     * @param aliveTimeInMinutes 缓存失效时间
     * @return 缓存的对象，注意对返回的对象进行修改并不会影响缓存中的对象
     */
    public <T> Optional<T> getOrCache(String key, Supplier<T> supplier, long aliveTimeInMinutes) {
        return Optional.ofNullable(this.<T>get(key).map(ObjectUtils::clone).orElseGet(() -> {
            Optional<T> v = Optional.ofNullable(supplier.get());

            if (v.isPresent()) {
                this.cacheWithTimeoutInMinutes(key, v.get(), aliveTimeInMinutes);
                return v.get();
            }
            return null;
        }));

    }

    /**
     * 缓存对象
     */
    private static class CacheValue {
        /**
         * 缓存对象的值
         */
        private Object value;

        /**
         * 缓存对象的添加时间，从基准时间开始多少秒
         */
        private long addTime;

        /**
         * 缓存对象存活时间(秒)
         */
        private long lifeTime;

        CacheValue(Object value) {
            this.value = value;
            this.addTime = System.currentTimeMillis() / 1000;
        }

        CacheValue(Object value, long lifeTime) {
            this.value = value;
            this.addTime = System.currentTimeMillis() / 1000;
            this.lifeTime = lifeTime;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public long getLifeTime() {
            return lifeTime;
        }

        public void setLifeTime(long lifeTime) {
            this.lifeTime = lifeTime;
        }

        /**
         * 对象是否存活
         */
        boolean isAlive() {
            if (0 == lifeTime) {
                return true;
            }

            long now = System.currentTimeMillis() / 1000;
            return now - addTime <= lifeTime;
        }
    }
}
