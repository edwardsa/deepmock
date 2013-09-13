deepmock
========

Allows injection of Mockito mocks and spies deep into an object graph


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
        private MyListElementDependency mock3 = Mockito.mock(MyListElementDependency.class);
        @Spy private List<MyListElementDependency> spy1 = Lists.newArrayList(mock3);

        @Resource @Subject private MySubject subject;
        @Test public void test() { subject.execute(); }
        ...
    }

In the above, subject would be wired with mock1, mock2 and spy1 iff they were dependencies.


Unlike, Mockito's @InjectMocks annotation, this runner will walk the object graph of each dependency injecting mocks if it finds a match to effectively inject multiple layers deep.
ie. Object A has dependency of Object B which has dependency of Object C.
If A were the subject and C the mock then B would be injected with the mocked C, but the Spring wired instance of B would still be injected into A.


The original state of the subject will be reinstated after each test so that downstream tests may rely on an object graph that is accurate of that initially wired by Spring for the defined context.


Injecting into Lists (using Spy)
--------------------------------
As shown above you can inject into a List dependency too.  You can either directly mock a List of the correct type (but then you would have to set expectations on the internal List methods) or you can inject a real List with mocked elements (as shown above).
To get a real List injected you can annotate it with the Mockito Spy annotation and construct your own list containing whatever mocks you like.
Note that the list you are injecting must be declared as exactly the same type as the list you are injecting into (including same generic type)
e.g. If the field you are injecting into is declared as List<String> then your spy list must be declared as List<String> (Not an ArrayList or List<Object>)
In fact this mechanism can be used to inject any "real" object you like (not just lists)

You can also have deepmock automatically create a List of one element if it identifies a Mock type that matches a list element type
e.g. If the field you are injecting into is declared as List<MyDep1> and you have a mock of MyDep1, DeepMock will automatically create a list containing that one mock and inject the list in place.
The same caveat about types as above exists here (the mock must be exactly the same type as the element type including generics)


Mechanisms to instantiate deepmock
----------------------------------
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
