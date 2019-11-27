package com.common.util;

import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;


/**
 * 反射辅助类
 *
 * @version V1.0
 **/
public class ReflectionUtils {
    public static void setField(Object o, String fieldName, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (o instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) o;
            map.put(fieldName, value);
        }

        PropertyUtils.setProperty(o, fieldName, value);
    }

    public static <T> T getFieldValue(Object o, String field) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (o instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) o;
            return (T) map.get(field);
        }

        return (T) PropertyUtils.getProperty(o, field);
    }
}
