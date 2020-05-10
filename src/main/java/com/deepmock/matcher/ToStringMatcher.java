package com.deepmock.matcher;

import static org.mockito.Matchers.argThat;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

public final class ToStringMatcher<T> extends BaseMatcher<T> implements ArgumentMatcher<T> {
    private Object expected;

    public static <T> T toStringEq(T obj) {
        return argThat(new ToStringMatcher<T>(obj));
    }

    public ToStringMatcher(Object expected) {
        this.expected = expected;
    }

    public boolean matches(Object o) {
        return o.toString().equals(expected.toString());
    }

    public void describeTo(Description description) {
        description.appendText("toStrings matching{").appendValue(expected).appendText("}");
    }

    /* Not currently used */
    private String xmlise(Object o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder xmlEncoder = new XMLEncoder(baos);
        xmlEncoder.writeObject(o);
        xmlEncoder.close();
        return baos.toString();
    }

}
