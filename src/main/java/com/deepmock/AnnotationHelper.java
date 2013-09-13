package com.deepmock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.internal.util.reflection.Whitebox.getInternalState;

public class AnnotationHelper {

    public static Map<Type, Object> findAnnotatedFields(Object target, Class<? extends Annotation> annotation) {
        Map<Type, Object> fields = new HashMap<Type, Object>();
        for (Field field : target.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation)) {
                addField(target, field, fields);
            }
        }
        return fields;
    }

    public static Collection<Object> findAnnotatedFieldObjects(Object target, Class<? extends Annotation> annotation) {
        Collection<Object> fields = new ArrayList<Object>();
        for (Field field : target.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation)) {
                fields.add(getInternalState(target, field.getName()));
            }
        }
        return fields;
    }

    private static void addField(Object target, Field field, Map<Type, Object> fields) {
        Object object = getInternalState(target, field.getName());
        fields.put(field.getGenericType(), object);
    }
}
