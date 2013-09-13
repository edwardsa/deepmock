package com.deepmock;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.MockitoAnnotations.initMocks;


/**
 * JUnit runner that initialises a Spring context and then injects Mocktio mocks into a test field annotated with the
 * "@Subject" annotation.
 * <p/>
 * <p/>
 * Injection is by type.
 * <p/>
 * <p/>
 * Typical use would be similar to:
 * <pre>
 * &#064;RunWith(SpringWithMockitoRunner.class)
 * &#064;ContextConfiguration(locations = { "/applicationContext.xml" })
 * public class Test {
 *     &#064;Mock private Object mock1;
 *     &#064;Mock private Object mock2;
 *
 *     private Object mock3 = Mockito.mock(Object.class);
 *     &#064;Spy private List&lt;Object&gt; spy1 = Lists.newArrayList(mock3);
 *
 *     &#064;Resource @Subject private Object subject;
 *     &#064;Test public void test() { subject.execute(); }
 *     ...
 * }
 * </pre>
 * <p/>
 * In the above, <code>subject</code> would be wired with <code>mock1</code>, <code>mock2</code> and <code>spy1</code> iff they were
 * dependencies.
 * <p/>
 * <p/>
 * Unlike, Mockito's @InjectMocks annotation, this runner will walk the object graph of each dependency injecting mocks
 * if it finds a match to effectively inject multiple layers deep. ie. Object A has dependency of Object B which has
 * dependency of Object C. If A were the subject and C the mock then B would be injected with the mock but the Spring
 * wired instance of B would still be injected into A.
 * <p/>
 * <p/>
 * The original state of the subject will be reinstated after each test so that downstream tests may rely on an object
 * graph that is accurate of that initially wired by Spring for the defined context.
 */
public class SpringWithMockitoRunner extends SpringJUnit4ClassRunner {

    private Object test;
    private DeepMockHandler deepMockHandler;

    public SpringWithMockitoRunner(Class<?> clazz) throws Exception {
        super(clazz);
    }

    @Override
    protected Object createTest() throws Exception {
        test = createTestWithSpringContext();
        initMocks(test);
        deepMockHandler = new DeepMockHandler(test);
        deepMockHandler.injectMocksIntoObjectGraphOfSubject(true);
        return test;
    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.addListener(new RestoreOriginalFieldsAfterTestRunListener());
        super.run(notifier);
    }

    private Object createTestWithSpringContext() throws Exception {
        return super.createTest();
    }

    private class RestoreOriginalFieldsAfterTestRunListener extends RunListener {

        public void testFinished(Description description) {
            deepMockHandler.restoreOriginalFields();
        }
    }
}
