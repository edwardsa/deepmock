package com.deepmock.matcher;

import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.Date;

public final class DateMatcher extends TypeSafeMatcher<Date> {
    private Date expectedDate;
    private int allowanceMillis;
    private Date expectedMaxDate;
    private Date expectedMinDate;

    public static Matcher<Date> matches(Date expected, int allowanceMillis) {
        return new DateMatcher(expected, allowanceMillis);
    }

    public DateMatcher(Date expectedDate, int allowanceMillis) {
        this.expectedDate = expectedDate;
        this.allowanceMillis = allowanceMillis;
        this.expectedMaxDate = DateUtils.addMilliseconds(expectedDate, allowanceMillis);
        this.expectedMinDate = DateUtils.addMilliseconds(expectedDate, -allowanceMillis);
    }

    @Override
    public boolean matchesSafely(Date date) {
        return expectedMaxDate.after(date) && expectedMinDate.before(date);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Date matching approx: " + expectedDate);
    }

}
