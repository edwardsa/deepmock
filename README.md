deepmock
========

Allows injection of Mockito mocks deep into an object graph


Usage
=====

The most common usage is via the SpringWIthMockitoRunner JUnit runner. This initialises a Spring context and then injects Mocktio mocks into a test field annotated with the "@Subject" annotation. 

Injection is by type. 

Typical use would be similar to: 

    @RunWith(SpringWithMockitoRunner.class)
    @ContextConfiguration(locations = { "/applicationContext.xml" })
    public class Test {
        @Mock private MyFirstDependency mock1;
        @Mock private MySecondDependency mock2;
        @Resource @Subject private MySubject subject;
        @Test public void test() { subject.execute(); }
        ...
    }
 
In the above, subject would be wired with mock1 and mock2 iff they were dependencies. 


Unlike, Mockito's @InjectMocks annotation, this runner will walk the object graph of each dependency injecting mocks if it finds a match to effectively inject multiple layers deep.
ie. Object A has dependency of Object B which has dependency of Object C.
If A were the subject and C the mock then B would be injected with the mocked C, but the Spring wired instance of B would still be injected into A. 


The original state of the subject will be reinstated after each test so that downstream tests may rely on an object graph that is accurate of that initially wired by Spring for the defined context.


There are a number of ways to do deepmock injection. The mechanisms vary in intrusiveness and flexibility as follows:

Directly
--------
This gives you the most control, but is also the most intrusive.

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        deepMockHandler = new DeepMockHandler(this);
        deepMockHandler.injectMocksIntoObjectGraphOfSubject(false);
    }
    
    @After
    public void after() {
        deepMockHandler.restoreOriginalFields();
    }
 
As a Spring Junit Runner
------------------------
This is probably the least intrusive but ties your test to spring.

    @RunWith(SpringWithMockitoRunner.class)
    @ContextConfiguration(locations = {"/myTest-applicationContext.xml"})
    public final class MyITest {
    ...
 
As a Rule
---------
This allows you to use whichever runner you like.
Note that the Subject must be fully instantiated at the point that the rule is invoked. Because Rules are invoked after Before annotated methods, you can not rely on Before method to setup your subject.

    @Rule
    public DeepMockRule deepMockRule = new DeepMockRule();
 