package com.deepmock.mockito;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Use this test rule to automatically perform a verify on all annotated mocks of your test instance
 */
public class MockitoVerifyRule extends TestWatcher {
    private Object testInstance;
    private boolean onlyOnFailure;
    private boolean verifyNoMore;

    /**
     * If the test fails, verify the expected calls have been executed
     */
    public static MockitoVerifyRule verifyOnFailure(Object testInstance) {
        return new MockitoVerifyRule(testInstance, true, false);
    }

    /**
     * Verify the expected calls have been executed
     */
    public static MockitoVerifyRule verifyAlways(Object testInstance) {
        return new MockitoVerifyRule(testInstance, false, false);
    }

    /**
     * Verify the expected calls have been executed and that no more invocations have occurred
     */
    public static MockitoVerifyRule verifyAlwaysStrict(Object testInstance) {
        return new MockitoVerifyRule(testInstance, false, true);
    }

    /**
     * Instantiate the rule with your test instance (e.g. this) and whether you want to verify only when there is an exception
     * @param testInstance The test instance
     * @param onlyOnFailure true if you only want to verify when the test fails, false to always verify
     * @param verifyNoMore true if you want to verify there are no more unexpected mock invocations
     */
    public MockitoVerifyRule(Object testInstance, boolean onlyOnFailure, boolean verifyNoMore) {
        this.testInstance = testInstance;
        this.onlyOnFailure = onlyOnFailure;
        this.verifyNoMore = verifyNoMore;
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
        Verify.verifyAllMockExpectations(testInstance, verifyNoMore);
    }
}
