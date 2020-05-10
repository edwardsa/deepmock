package com.deepmock.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class StaticFieldUtil {

    public static void setFinalStatic(Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
