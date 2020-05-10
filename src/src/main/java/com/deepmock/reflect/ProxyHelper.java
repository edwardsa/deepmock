package com.deepmock.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

public final class ProxyHelper {

    public static boolean isProxy(Object target) {
        return AopUtils.isJdkDynamicProxy(target) || AopUtils.isCglibProxy(target);
    }

    public static Object getProxyTarget(Object target) {
        if (AopUtils.isJdkDynamicProxy(target)) {
            return getJdkProxyTarget(target);
        } else if (AopUtils.isCglibProxy(target)) {
            return getCglibProxyTarget(target);
        } else {
            throw new IllegalArgumentException("Object is not a proxy, or is not a proxy we can get the target of");
        }
    }

    private static Object getCglibProxyTarget(Object target) {
        try {
            Field field = target.getClass().getDeclaredField("CGLIB$CALLBACK_0");
            ReflectionUtils.makeAccessible(field);
            Object interceptor = ReflectionUtils.getField(field, target);
            Field advisedField = interceptor.getClass().getDeclaredField("advised");
            ReflectionUtils.makeAccessible(advisedField);
            ProxyFactory proxyFactory = (ProxyFactory)ReflectionUtils.getField(advisedField, interceptor);
            return proxyFactory.getTargetSource().getTarget();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get target of proxy", e);
        }
    }

    private static Object getJdkProxyTarget(Object target) {
        try {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(target);
            Field advisedField = ReflectionUtils.findField(invocationHandler.getClass(), "advised");
            ReflectionUtils.makeAccessible(advisedField);
            ProxyFactory advised = (ProxyFactory)ReflectionUtils.getField(advisedField, invocationHandler);
            return advised.getTargetSource().getTarget();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get target of proxy", e);
        }
    }
}
