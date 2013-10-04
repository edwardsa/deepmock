package com.deepmock.mockito;

import com.deepmock.AnnotationHelper;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mock;

import java.util.Collection;

/**
 * Use this test rule to automatically perform a verify on all annotated mocks of your test instance
 */
public class MockitoVerifyRule extends TestWatcher {
    private Object testInstance;
    private boolean onlyOnFailure;

    /**
     * Instantiate the rule with your test instance (e.g. this) and whether you want to verify only when there is an exception
     * @param testInstance The test instance
     * @param onlyOnFailure true if you only want to verify when the test fails, false to always verify
     */
    public MockitoVerifyRule(Object testInstance, boolean onlyOnFailure) {
        this.testInstance = testInstance;
        this.onlyOnFailure = onlyOnFailure;
    }

    @Override
    protected void finished(Description description) {
        if (!onlyOnFailure) {
            verifyAllMocks();
        }
    }

    @Override
    protected void failed(Throwable e, Description description) {
        if (onlyOnFailure) {
            verifyAllMocks();
        }
    }

    private void verifyAllMocks() {
        Collection<Object> mockFields = AnnotationHelper.findAnnotatedFields(testInstance, Mock.class).values();
        Verify.verifyExpectations(mockFields.toArray());
    }
}
