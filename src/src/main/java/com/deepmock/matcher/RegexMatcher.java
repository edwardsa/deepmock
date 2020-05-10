package com.deepmock.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public final class RegexMatcher extends TypeSafeMatcher<String> {
    private String pattern;

    public static Matcher<String> matches(String pattern) {
        return new RegexMatcher(pattern);
    }

    public RegexMatcher(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matchesSafely(String str) {
        if (str == null && pattern == null) {
            return true;
        } else if ((str == null) || (pattern == null)) {
            return false;
        } else {
            return str.matches(pattern);
        }
    }

    public void describeTo(Description description) {
        description.appendText("String matching: " + pattern);
    }
}
