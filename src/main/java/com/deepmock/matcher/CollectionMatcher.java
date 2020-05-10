
package com.deepmock.matcher;

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public final class CollectionMatcher<T, C extends Collection<T>> extends TypeSafeMatcher<C> {
    private T[] expected;

    public static <C extends Collection> Matcher<C> matchesInOrder(C expected) {
        return new CollectionMatcher(expected.toArray());
    }

    public static <T, C extends Collection<T>> Matcher<C> matchesInOrder(T... expected) {
        return new CollectionMatcher(expected);
    }

    public CollectionMatcher(T[] expected) {
        this.expected = expected;
    }

    @Override
    public boolean matchesSafely(C collection) {
        return Arrays.equals(expected, collection.toArray());
    }

    public void describeTo(Description description) {
        description.appendText("Collection matching: " + Arrays.toString(expected));
    }
}
