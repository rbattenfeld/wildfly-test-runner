package com.six_group.java_ee.common.junit.client;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestResult;

import org.junit.Ignore;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runners.model.InitializationError;

public class WildflyTestAdapter implements Test, Filterable, Sortable, Describable {
    private final Class<?> fNewTestClass;
    private final Runner fRunner;
    private final WildflyTestAdapterCache fCache;

    public WildflyTestAdapter(final Class<?> newTestClass) {
        this(newTestClass, WildflyTestAdapterCache.getDefault(), false, true);
    }
    
    public WildflyTestAdapter(final Class<?> newTestClass, final boolean isTestsOnlyMode, final boolean isLastTestCase) {
        this(newTestClass, WildflyTestAdapterCache.getDefault(), isTestsOnlyMode, isLastTestCase);
    }

    public WildflyTestAdapter(final Class<?> newTestClass, final WildflyTestAdapterCache cache, final boolean isTestsOnlyMode, final boolean isLastTestCase) {
        fCache = cache;
        fNewTestClass = newTestClass;
        try {
            fRunner = new WildflyTestRunner(newTestClass, isTestsOnlyMode, isLastTestCase);
        } catch (final InitializationError ex) {
            throw new RuntimeException(ex);
        }
    }

    public int countTestCases() {
        return fRunner.testCount();
    }

    public void run(TestResult result) {
        fRunner.run(fCache.getNotifier(result, this));
    }

    // reflective interface for Eclipse
    public List<Test> getTests() {
        return fCache.asTestList(getDescription());
    }

    // reflective interface for Eclipse
    public Class<?> getTestClass() {
        return fNewTestClass;
    }

    public Description getDescription() {
        Description description = fRunner.getDescription();
        return removeIgnored(description);
    }

    private Description removeIgnored(Description description) {
        if (isIgnored(description)) {
            return Description.EMPTY;
        }
        Description result = description.childlessCopy();
        for (Description each : description.getChildren()) {
            Description child = removeIgnored(each);
            if (!child.isEmpty()) {
                result.addChild(child);
            }
        }
        return result;
    }

    private boolean isIgnored(Description description) {
        return description.getAnnotation(Ignore.class) != null;
    }

    @Override
    public String toString() {
        return fNewTestClass.getName();
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        filter.apply(fRunner);
    }

    public void sort(Sorter sorter) {
        sorter.apply(fRunner);
    }
}