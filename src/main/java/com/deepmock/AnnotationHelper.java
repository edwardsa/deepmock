package com.deepmock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mockito.internal.util.reflection.FieldReader;

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
                fields.add(new FieldReader(target, field).read());
            }
        }
        return fields;
    }

    private static void addField(Object target, Field field, Map<Type, Object> fields) {
        Object object = new FieldReader(target, field).read();
        fields.put(field.getGenericType(), object);
    }
}
