package com.deepmock;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.MockitoAnnotations;

public final class DeepMockStatement extends Statement {
    private Statement base;
    private Object target;

    public DeepMockStatement(Statement base, FrameworkMethod method, Object target) {
        this.base = base;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        // create annotated Mockito mocks
        MockitoAnnotations.initMocks(target);
        DeepMockHandler deepMockHandler = new DeepMockHandler(target);
        deepMockHandler.injectMocksIntoObjectGraphOfSubject(false);
        try {
            base.evaluate();
        } finally {
            deepMockHandler.restoreOriginalFields();
        }
    }


}
