package com.deepmock.mockito;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;

public class MockitoFactoryBean implements FactoryBean<Object> {

    private Class<?> clazz;

    public void setMockType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Object getObject() throws Exception {
        return Mockito.mock(clazz);
    }

    public Class<?> getObjectType() {
        return this.clazz;
    }

    public boolean isSingleton() {
        return true;
    }

}
