package com.deepmock.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.Matchers;
import org.mockito.internal.matchers.Equality;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DeepReflectionEqualsMatcher<T> extends BaseMatcher<T> {
    private Object value;
    private T expected;
    private boolean veryDeep;

    /**
     * Reflection equality test that deeply reflects through the object graph.  This will
     * reflect down the graph until it hits objects that implement .equals 
     * @param expected The expected value
     * If true, will ignore .equals on all objects excpet core java
     * @param <T> The type of object being tested for equality
     */
    public static <T> T refEq(T expected) {
        return refEq(expected, false);
    }

    /**
     * Reflection equality test that deeply reflects through the object graph 
     * @param expected The expected value
     * @param veryDeep Whether to use .equals methods when found on objects.
     * If true, will ignore .equals on all objects excpet core java
     * @param <T> The type of object being tested for equality
     */
    public static <T> T refEq(T expected, boolean veryDeep) {
        return Matchers.argThat(new DeepReflectionEqualsMatcher<T>(expected, veryDeep));
    }

    public DeepReflectionEqualsMatcher(T expected, boolean veryDeep) {
        this.expected = expected;
        this.veryDeep = veryDeep;
    }

    @Override
    public boolean matches(Object value) {
        this.value = value;
        return isEqualRecursive(expected, value);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("refEq");
        description.appendValue(value);
    }

    private boolean isEqualRecursive(final Object expected, final Object value) {
        if (expected == null && value == null) {
            return true;
        } else if (expected == null || value == null) {
            return false;
        } else if (expected.getClass() != value.getClass()) {
            return false;
        }
        if (expected == value) {
            return true;
        } else if (hasEqualsMethod(value) && isRightDepth(value)) {
            return value.equals(expected);
        } else if (value.getClass().isEnum()) {
            return value == expected;
        } else {
            List<Field> fields = getAllInheritedFields(value.getClass());
            if (fields.size() == 0) {
                return Equality.areEqual(expected, value);
            }
            for (Field field : fields) {
                field.setAccessible(true);
                if (!isEqualRecursive(tryGetField(expected, field), tryGetField(value, field))) {
                    System.out.println("Field not equal: " + field);
                    return false;
                }
            }
            return true;
        }
    }

    private boolean isRightDepth(Object value) {
        return !veryDeep || isLangClass(value);
    }

    private boolean hasEqualsMethod(Object value) {
        try {
            value.getClass().getDeclaredMethod("equals", Object.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private boolean isLangClass(Object value) {
        return value.getClass().getPackage() == null || value.getClass().getPackage().getName().startsWith("java.");
    }

    private static List<Field> getAllInheritedFields(Class<? extends Object> aClass) {
        List<Field> result = new ArrayList<Field>();

        Class<?> i = aClass;
        while (i != null && i != Object.class) {
            for (Field field : i.getDeclaredFields()) {
                if (!field.isSynthetic()) {
                    result.add(field);
                }
            }
            i = i.getSuperclass();
        }
        return result;
    }

    private static Object tryGetField(Object expected, Field field) {
        try {
            return field.get(expected);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
