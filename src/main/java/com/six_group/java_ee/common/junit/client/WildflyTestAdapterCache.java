package com.six_group.java_ee.common.junit.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

public class WildflyTestAdapterCache extends HashMap<Description, Test> {
    private static final long serialVersionUID = 1L;
    private static final WildflyTestAdapterCache fInstance = new WildflyTestAdapterCache();

    public static WildflyTestAdapterCache getDefault() {
        return fInstance;
    }

    public Test asTest(Description description) {
        if (description.isSuite()) {
            return createTest(description);
        } else {
            if (!containsKey(description)) {
                put(description, createTest(description));
            }
            return get(description);
        }
    }

    Test createTest(Description description) {
        if (description.isTest()) {
            return new WildflyTestAdapterFacade(description);
        } else {
            TestSuite suite = new TestSuite(description.getDisplayName());
            for (Description child : description.getChildren()) {
                suite.addTest(asTest(child));
            }
            return suite;
        }
    }

    public RunNotifier getNotifier(final TestResult result, final WildflyTestAdapter adapter) {
        RunNotifier notifier = new RunNotifier();
        notifier.addListener(new RunListener() {
            @Override
            public void testFailure(Failure failure) throws Exception {
                result.addError(asTest(failure.getDescription()), failure.getException());
            }

            @Override
            public void testFinished(Description description) throws Exception {
                result.endTest(asTest(description));
            }

            @Override
            public void testStarted(Description description) throws Exception {
                result.startTest(asTest(description));
            }
        });
        return notifier;
    }

    public List<Test> asTestList(Description description) {
        if (description.isTest()) {
            return Arrays.asList(asTest(description));
        } else {
            List<Test> returnThis = new ArrayList<Test>();
            for (Description child : description.getChildren()) {
                returnThis.add(asTest(child));
            }
            return returnThis;
        }
    }
}