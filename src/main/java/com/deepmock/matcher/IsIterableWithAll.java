package com.deepmock.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IsIterableWithAll<T> extends TypeSafeDiagnosingMatcher<Iterable<? extends T>> {
    private final Matcher<? super T> elemMatcher;

    public static <T> IsIterableWithAll all(Matcher<T> elemMatcher) {
        return new IsIterableWithAll(elemMatcher);
    }

    public IsIterableWithAll(Matcher<? super T> elemMatcher) {
        this.elemMatcher = elemMatcher;
    }

    @Override
    public boolean matchesSafely(Iterable<? extends T> iter, Description mismatchDescription) {
        for (T elem : iter) {
            if (!elemMatcher.matches(elem)) {
                elemMatcher.describeMismatch(elem, mismatchDescription);
                return false;
            }
        }
        return true;
    }

    public void describeTo(Description description) {
        description.appendText("iterable with all elements [")
                .appendDescriptionOf(elemMatcher)
                .appendText("]");
    }
}
