package com.deepmock.matcher;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Expects the first argument to the invocation to be a runnable, and will just execute that runnable.
 * This allows you to mock executors (which run Runnables in a separate thread) but have the execution happen
 * synchronously
 */
public class CallsRunnable implements Answer<Void> {
    public Void answer(InvocationOnMock invocation) throws Throwable {
        ((Runnable)invocation.getArguments()[0]).run();
        return null;
    }
}

