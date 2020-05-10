package com.deepmock.mockito;

import static org.mockito.internal.util.StringUtil.join;

import com.deepmock.AnnotationHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.mockito.Mock;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.internal.debugging.LocationImpl;
import org.mockito.internal.exceptions.Reporter;
import org.mockito.internal.exceptions.stacktrace.StackTraceFilter;
import org.mockito.internal.invocation.InvocationMarker;
import org.mockito.internal.invocation.InvocationsFinder;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;

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
                throw Reporter.nullPassedToVerifyNoMoreInteractions();
            }
            Invocation unverified = getFirstUnverifiedInvocation(mock);
            if (unverified != null) {
                throw new NoInteractionsWanted(buildNoMoreInteractionsMessage(unverified));
            }
        } catch (NotAMockException e) {
            throw Reporter.notAMockPassedToVerifyNoMoreInteractions();
        }
    }

    private static Invocation getFirstUnverifiedInvocation(Object mock) {
        MockHandler<Object> mockHandler = MockUtil.getMockHandler(mock);
        MockCreationSettings<Object> mockSettings = mockHandler.getMockSettings();
        InvocationContainerImpl invocationContainer = new InvocationContainerImpl(mockSettings);
        return InvocationsFinder.findFirstUnverified(invocationContainer.getInvocations());
    }

    private static void verifyExpectationsOn(Object mock) {
        MockCreationSettings<Object> mockSettings =
                MockUtil.getMockHandler(mock).getMockSettings();
        InvocationContainerImpl invocationContainer = new InvocationContainerImpl(mockSettings);
        List<StubbedInvocationMatcher> invocationMatchers =
                getStubbedInvocationsInOrder(invocationContainer);
        for (StubbedInvocationMatcher invocationMatcher : invocationMatchers) {
            verifyInvoked(invocationMatcher, invocationContainer);
        }
    }

    private static List<StubbedInvocationMatcher> getStubbedInvocationsInOrder(InvocationContainerImpl container) {
        return container.getInvocations().stream().map(container::findAnswerFor)
                .sorted(Comparator.comparingInt(o -> o.getInvocation().getSequenceNumber()))
                .collect(Collectors.toList());
    }

    private static void verifyInvoked(StubbedInvocationMatcher invocationMatcher,
                                      InvocationContainerImpl container) {
        if (invocationMatcher.wasUsed()) {
            markVerified(invocationMatcher, container);
        } else {
            String message = buildWantedMessage(invocationMatcher, container);
            throw new WantedButNotInvoked(message);
        }
    }

    private static String buildWantedMessage(StubbedInvocationMatcher stubbedMatcher,
                                             InvocationContainerImpl container) {
        Invocation invocation = stubbedMatcher.getInvocation();
        StringBuilder message = new StringBuilder("\nexpected method call: " + stubbedMatcher.toString() + "\n" );
        Invocation similar = findSimilar(stubbedMatcher, container.getInvocations());
        if (similar != null) {
            message.append("actual method call => ").append(similar);
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
        return InvocationsFinder.findSimilarInvocation(invocations, stubbedMatcher);
    }

    private static void markVerified(StubbedInvocationMatcher stubbedInvocation,
                                     InvocationContainerImpl container) {
        List<Invocation> invs = container.getInvocations();
        for (Invocation inv : invs) {
            if (stubbedInvocation.matches(inv)) {
                InvocationMarker.markVerified(inv, stubbedInvocation);
            }
        }
    }

    private static Location getLocation() {
        return new LocationImpl(new StackTraceFilter() {
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
