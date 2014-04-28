package com.deepmock.utils;

import com.google.common.base.Predicate;
import org.apache.commons.lang.time.StopWatch;

/**
 * @author edwardsal
 */
public class Predicates {
    /**
     * Wait for the predicate to evaluate to true (ignores the parameter to the predicate passing null always), or for the timeout
     * to occur.
     */
    public static void waitForPredicate(Predicate<Object> predicate, long timeoutMillis) {
        StopWatch sw = new StopWatch();
        sw.start();
        while (!predicate.apply(null)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // ignore
            }
            if (sw.getTime() > timeoutMillis) {
                return;
            }
        }
    }
}
