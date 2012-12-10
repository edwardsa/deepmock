deepmock
========

Allows injection of Mockito mocks deep into an object graph


Usage
=====
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
 