package com.deepmock;

import com.deepmock.reflect.ProxyHelper;
import org.mockito.Mock;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.deepmock.InjectionHelper.getInjectableFields;
import static org.mockito.internal.util.reflection.Whitebox.getInternalState;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

/**
 * This class controls the deep injection of mocks and the restoration of original state.  The mocks will be injected
 * into the object graph of the {@link Subject} of the supplied test.
 *
 * It can be invoked in numerous ways from your test class:
 * <h2>Directly</h2>
 * This gives you the most control, but is also the most intrusive.
 * <pre>
 *   &#64;Before
 *   public void before() {
 *       MockitoAnnotations.initMocks(this);
 *       deepMockHandler = new DeepMockHandler(this);
 *       deepMockHandler.injectMocksIntoObjectGraphOfSubject(false);
 *   }
 *
 *   &#64;After
 *   public void after() {
 *       deepMockHandler.restoreOriginalFields();
 *   }
 * </pre>
 *
 * <h2>As a Spring Junit Runner</h2>
 * This is probably the least intrusive but ties your test to spring.
 * <pre>
 *   &#64;RunWith(SpringWithMockitoRunner.class)
 *   &#64;ContextConfiguration(locations = {"/myTest-applicationContext.xml"})
 *   public final class MyITest {
 *   ...
 * </pre>
 *
 * <h2>As a Rule</h2>
 * This allows you to use whichever runner you like.  Note that the {@link Subject} must be fully instantiated
 * at the point that the rule is invoked.  Because Rules are invoked after Before annotated methods, you can not rely on
 * Before method to setup your subject.
 * <pre>
 *   &#64;Rule
 *   public DeepMockRule deepMockRule = new DeepMockRule();
 * </pre>
 *
 * @see DeepMockRule
 * @see SpringWithMockitoRunner
 * @see Subject
 */
public final class DeepMockHandler {

    private List<FieldAndValue> originalFields = new ArrayList<FieldAndValue>();
    private Object testTarget;

    public DeepMockHandler(Object testTarget) {
        this.testTarget = testTarget;
    }

    /**
     * Inject mocks into the object graph of the subject.
     * @param onlySpringFields Only traverse down spring injectable paths i.e. @Resource, @Autowired, setter injection.
     * This is slightly faster but creates a dependency on Spring.
     */
    public void injectMocksIntoObjectGraphOfSubject(boolean onlySpringFields) {
        recurseObjectGraphInjectingMocks(findSubject(), findMocks(), new ArrayList<Class>(), onlySpringFields);
    }

    public void restoreOriginalFields() {
        for (FieldAndValue field : originalFields) {
            field.reset();
        }
        originalFields.clear();
    }

    private void recurseObjectGraphInjectingMocks(Object target, Map<Class<?>, Object> mocks, List<Class> classStack,
            boolean onlySpringFields) {
        if (target == null || mocks.isEmpty()) {
            return;
        }
        if (ProxyHelper.isProxy(target)) {
            target = ProxyHelper.getProxyTarget(target);
        }
        if (classStack.contains(target.getClass())) {
            return; // prevent endless loop when class contains an instance of itself
        }
        if (target.getClass().isArray()) {
            Object[] arr = ((Object[])target);
            for (Object o : arr) {
                recurseObjectGraphInjectingMocks(o, mocks, new ArrayList<Class>(classStack), onlySpringFields);
            }
        }
        classStack.add(target.getClass());
        List<Field> injectableFields = onlySpringFields ? getInjectableFields(target) : getAllFields(target);
        for (Field field : injectableFields) {
            boolean fieldInjected = injectWithMockIfAvailable(target, field, mocks);
            if (!fieldInjected) {
                Object fieldValue = getInternalState(target, field.getName());
                recurseObjectGraphInjectingMocks(fieldValue, mocks, new ArrayList<Class>(classStack), onlySpringFields);
            }
        }
    }

    public List<Field> getAllFields(Object target) {
        final List<Field> fields = new ArrayList<Field>();
        ReflectionUtils.doWithFields(target.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (!ClassUtils.isPrimitiveOrWrapper(field.getType()) && !ClassUtils.isPrimitiveArray(field.getType()) && !field.getType().getName().startsWith("java.lang")) {
                    fields.add(field);
                }
            }
        });
        return fields;
    }

    private boolean injectWithMockIfAvailable(Object target, Field field, Map<Class<?>, Object> mocks) {
        if (alreadyReplaced(target, field)) {
            return true;
        }
        for (Map.Entry<Class<?>, Object> mock : mocks.entrySet()) {
            if (field.getType() == mock.getKey()) {
                storeOriginalValue(target, field);
                replaceFieldWithMock(target, field, mock.getValue());
                return true;
            } else if (field.getType().isArray() && field.getType().getComponentType() == mock.getKey()) {
                storeOriginalValue(target, field);
                Object arr = Array.newInstance(mock.getKey(), 1);
                Array.set(arr, 0, mock);
                replaceFieldWithMock(target, field, arr);
                return true;
            }
        }
        return false;
    }

    private boolean alreadyReplaced(Object target, Field field) {
        for (FieldAndValue replacedField : originalFields) {
            if (replacedField.sameField(target, field)) {
                return true;
            }
        }
        return false;
    }

    private Object findSubject() {
        Map<Class<?>, Object> subjects = findSubjects();
        if (subjects.size() == 1) {
            return subjects.values().iterator().next();
        }
        throw new IllegalArgumentException("Must annotate a single field with " + Subject.class.getName());
    }

    private Map<Class<?>, Object> findSubjects() {
        return AnnotationHelper.findAnnotatedFields(testTarget, Subject.class);
    }

    private Map<Class<?>, Object> findMocks() {
        return AnnotationHelper.findAnnotatedFields(testTarget, Mock.class);
    }

    private void storeOriginalValue(Object target, Field field) {
        ReflectionUtils.makeAccessible(field);
        Object fieldValue = ReflectionUtils.getField(field, target);
        originalFields.add(new FieldAndValue(target, field, fieldValue));
    }

    private void replaceFieldWithMock(Object target, Field field, Object mockValue) {
        setInternalState(target, field.getName(), mockValue);
    }

}
