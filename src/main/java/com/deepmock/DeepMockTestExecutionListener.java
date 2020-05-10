package com.deepmock;

import static org.mockito.MockitoAnnotations.initMocks;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class DeepMockTestExecutionListener extends AbstractTestExecutionListener {
    private DeepMockHandler deepMockHandler;

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        initMocks(testContext.getTestInstance());
        deepMockHandler = new DeepMockHandler(testContext.getTestInstance());
        deepMockHandler.injectMocksIntoObjectGraphOfSubject(true);
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        deepMockHandler.restoreOriginalFields();
    }

}
