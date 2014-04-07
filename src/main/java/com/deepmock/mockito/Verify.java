package com.deepmock.mockito;

import com.deepmock.AnnotationHelper;
import org.mockito.Mock;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.internal.MockHandlerInterface;
import org.mockito.internal.debugging.Location;
import org.mockito.internal.exceptions.base.StackTraceFilter;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMarker;
import org.mockito.internal.invocation.InvocationsFinder;
import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.util.MockUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.mockito.internal.util.StringJoiner.join;

public final class Verify {

    /**
     * Verify all "when" calls were executed for supplied mocks
     */
    public static void verifyExpectations(Object... mocks) {
        for (Object mock : mocks) {
            verifyExpectationsOn(mock);
        }
    }

    /**
     * Same General behaviour as Mockito#verifyNoMoreInteractions
     * Adds better logging stating the details of the call that was unexpected
     */
    public static void verifyNoMoreInteractions(Object... mocks) {
        for (Object mock : mocks) {
            verifyNoMoreInteractionsOn(mock);
        }
    }

    /**
     * Verify all "when" calls have been executed on all mocks (annotated Mock) for the given test instance. Also verifies that there are no more mock
     * interactions if requested.
     * @param testInstance The test instance with Mock annotated fields
     * @param verifyNoMoreInteraction Whether to verify that there are no more interaction on mocks for this test instance
     */
    public static void verifyAllMockExpectations(Object testInstance, boolean verifyNoMoreInteraction) {
        Collection<Object> mockFields = AnnotationHelper.findAnnotatedFields(testInstance, Mock.class).values();
        Verify.verifyExpectations(mockFields.toArray());
        if (verifyNoMoreInteraction) {
            Verify.verifyNoMoreInteractions(mockFields.toArray());
        }
    }


    private static void verifyNoMoreInteractionsOn(Object mock) {
        try {
            if (mock == null) {
                new Reporter().nullPassedToVerifyNoMoreInteractions();
            }
            Invocation unverified = getFirstUnverifiedInvocation(mock);
            if (unverified != null) {
                throw new NoInteractionsWanted(buildNoMoreInteractionsMessage(unverified));
            }
        } catch (NotAMockException e) {
            new Reporter().notAMockPassedToVerifyNoMoreInteractions();
        }
    }

    private static Invocation getFirstUnverifiedInvocation(Object mock) {
        MockHandlerInterface<Object> mockHandler = new MockUtil().getMockHandler(mock);
        List<Invocation> allInvocations = mockHandler.getInvocationContainer().getInvocations();
        Invocation unverified = new InvocationsFinder().findFirstUnverified(allInvocations);
        return unverified;
    }

    private static void verifyExpectationsOn(Object mock) {
        InvocationContainer invocationContainer = new MockUtil().getMockHandler(mock).getInvocationContainer();
        List<StubbedInvocationMatcher> invocationMatchers = getStubbedInvocationsInOrder(invocationContainer);
        for (StubbedInvocationMatcher invocationMatcher : invocationMatchers) {
            verifyInvoked(invocationMatcher, invocationContainer);
        }
    }

    private static List<StubbedInvocationMatcher> getStubbedInvocationsInOrder(InvocationContainer container) {
        List<StubbedInvocationMatcher> sorted = new ArrayList<StubbedInvocationMatcher>(container.getStubbedInvocations());
        Collections.sort(sorted, new Comparator<StubbedInvocationMatcher>() {
            @Override
            public int compare(StubbedInvocationMatcher o1, StubbedInvocationMatcher o2) {
                return o1.getInvocation().getSequenceNumber() - o2.getInvocation().getSequenceNumber();
            }
        });
        return sorted;
    }

    private static void verifyInvoked(StubbedInvocationMatcher invocationMatcher, InvocationContainer container) {
        if (invocationMatcher.wasUsed()) {
            markVerified(invocationMatcher, container);
        } else {
            String message = buildWantedMessage(invocationMatcher, container);
            throw new WantedButNotInvoked(message);
        }
    }

    private static String buildWantedMessage(StubbedInvocationMatcher stubbedMatcher, InvocationContainer container) {
        Invocation invocation = stubbedMatcher.getInvocation();
        StringBuffer message = new StringBuffer("\nexpected method call: " + stubbedMatcher.toString() + "\n" );
        Invocation similar = findSimilar(stubbedMatcher, container.getInvocations());
        if (similar != null) {
            message.append("actual method call => " + similar);
        } else {
            message.append("but never called");
        }
        message.append("\nAll method calls: \n" + container.getInvocations());
        return message.toString();
    }

    private static String buildNoMoreInteractionsMessage(Invocation unverified) {
        return join(
                "No interactions wanted here:",
                getLocation(),
                "But found this interaction:",
                unverified.toString(),
                "here:",
                unverified.getLocation(),
                ""
        );
    }

    private static Invocation findSimilar(StubbedInvocationMatcher stubbedMatcher, List<Invocation> invocations) {
        return new InvocationsFinder().findSimilarInvocation(invocations, stubbedMatcher);
    }

    private static void markVerified(StubbedInvocationMatcher stubbedInvocation, InvocationContainer container) {
        List<Invocation> invs = container.getInvocations();
        for (Invocation inv : invs) {
            if (stubbedInvocation.matches(inv)) {
                new InvocationMarker().markVerified(inv, stubbedInvocation);
            }
        }
    }

    private static Location getLocation() {
        return new Location(new StackTraceFilter() {
            @Override
            public StackTraceElement[] filter(StackTraceElement[] target, boolean keepTop) {
                StackTraceElement[] firstFiltered = super.filter(target, keepTop);
                List<StackTraceElement> filtered = new ArrayList<StackTraceElement>();
                for (StackTraceElement ele : firstFiltered) {
                    if (!ele.getClassName().startsWith(this.getClass().getPackage().getName())) {
                        filtered.add(ele);
                    }
                }
                return filtered.toArray(new StackTraceElement[]{});
            }

        });
    }
}
