package com.deepmock;

import static org.mockito.internal.util.reflection.FieldSetter.setField;

import java.lang.reflect.Field;

public class FieldAndValue {
    private Object target;
    private Field field;
    private Object origValue;

    public FieldAndValue(Object target, Field field, Object origValue) {
        this.target = target;
        this.field = field;
        this.origValue = origValue;
    }

    public void reset() {
        setField(target, field, origValue);
    }

    public boolean sameField(Object target, Field field) {
        return target == this.target && field.equals(this.field);
    }
}
