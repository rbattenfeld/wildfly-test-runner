package com.six_group.java_ee.common.junit.client;

import junit.framework.Test;
import junit.framework.TestResult;

import org.junit.runner.Describable;
import org.junit.runner.Description;

public class WildflyTestAdapterFacade implements Test, Describable {
    private final Description fDescription;

    WildflyTestAdapterFacade(Description description) {
        fDescription = description;
    }

    @Override
    public String toString() {
        return getDescription().toString();
    }

    public int countTestCases() {
        return 1;
    }

    public void run(TestResult result) {
        throw new RuntimeException(
                "This test stub created only for informational purposes.");
    }

    public Description getDescription() {
        return fDescription;
    }
}
