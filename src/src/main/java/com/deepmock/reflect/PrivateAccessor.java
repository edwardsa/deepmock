package com.deepmock.reflect;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public final class PrivateAccessor {
    public static <T> T getFieldFromObjectOrProxyTarget(Object object, String fieldName) {
        Object objToInspect = ProxyHelper.isProxy(object) ? ProxyHelper.getProxyTarget(object) : object;
        return (T)getField(objToInspect, fieldName);
    }

    public static <T> T getField(Object obj, String fieldName) {
        Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        return (T)ReflectionUtils.getField(field, obj);
    }

    public static void setField(Object obj, String fieldName, Object value) {
        Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
        setField(obj, field, value);
    }

    public static void setField(Object obj, Field field, Object value) {
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, obj, value);
    }
}
