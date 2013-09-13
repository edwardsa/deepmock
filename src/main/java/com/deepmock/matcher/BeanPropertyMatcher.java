package com.deepmock.matcher;

import org.apache.commons.beanutils.BeanUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * Similar to the {@link org.hamcrest.beans.SamePropertyValuesAs.PropertyMatcher} except makes use of
 * commons bean utils to allow bean notation in the property name
 */
public final class BeanPropertyMatcher<T> extends TypeSafeMatcher<T> {
    private String propertyName;
    private  Matcher<?> expectedValue;

    public static <T> Matcher<T> hasProperty(String propertyName, Matcher<?> expectedValue) {
        return new BeanPropertyMatcher<T>(propertyName, expectedValue);
    }

    public BeanPropertyMatcher(String propertyName, Matcher<?> expectedValue) {
        this.propertyName = propertyName;
        this.expectedValue = expectedValue;
    }

    @Override
    public boolean matchesSafely(T bean) {
        try {
            String propertyValue = BeanUtils.getProperty(bean, propertyName);
            return expectedValue.matches(propertyValue);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("property " + propertyName + " matching: ");
        description.appendDescriptionOf(expectedValue);
    }

}
