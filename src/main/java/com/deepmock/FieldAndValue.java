package com.deepmock;

import org.mockito.internal.util.reflection.Whitebox;

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
        Whitebox.setInternalState(target, field.getName(), origValue);
    }

    public boolean sameField(Object target, Field field) {
        return target == this.target && field.equals(this.field);
    }
}
