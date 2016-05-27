/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.six_group.java_ee.common.junit.arquillian;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * JUnitTestRunner
 *
 * A Implementation of the Arquillian TestRunner SPI for JUnit.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class JUnitTestRunner implements TestRunner {
    private static final Log _Logger = LogFactory.getLog(JUnitTestRunner.class);

    /**
     * Overwrite to provide additional run listeners.
     */
    protected List<RunListener> getRunListeners() {
        return Collections.emptyList();
    }

    public TestResult execute(Class<?> testClass, final Object testObject, String methodName) {
        final ExpectedExceptionHolder exceptionHolder = new ExpectedExceptionHolder();
        TestResult testResult = TestResult.passed();
        try {
            JUnitCore runner = new JUnitCore();
            runner.addListener(exceptionHolder);

            for (RunListener listener : getRunListeners()) {
                runner.addListener(listener);
            }
            
            final Description method = Description.createTestDescription(testClass, methodName);
            final Request req = new InstanceRequest(testClass, testObject, methodName);
            req.filterWith(method);
            Result result = runner.run(req);
            if (result.getFailureCount() > 0) {
                testResult = TestResult.failed(exceptionHolder.getException());
            } else if (result.getIgnoreCount() > 0) {
                testResult = TestResult.skipped(null); // Will this ever happen
                                                       // incontainer?
            } else {
                testResult = TestResult.passed();
            }
            if (testResult.getThrowable() == null) {
                testResult.setThrowable(exceptionHolder.getException());
            }
        } catch (Throwable th) {
            testResult = TestResult.failed(th);
        }
        if (testResult.getThrowable() instanceof AssumptionViolatedException) {
            testResult = TestResult.skipped(testResult.getThrowable());
        }
        testResult.setEnd(System.currentTimeMillis());
        return testResult;
    }

    private class ExpectedExceptionHolder extends RunListener {
        private Throwable exception;

        public Throwable getException() {
            return exception;
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            // AssumptionViolatedException might not be Serializable. Recreate
            // with only String message.
            exception = new AssumptionViolatedException(failure.getException().getMessage());
            exception.setStackTrace(failure.getException().getStackTrace());
            ;
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            if (exception != null) {
                // In case of multiple errors only keep the first exception
                return;
            }
            exception = State.getTestException();
            Test test = failure.getDescription().getAnnotation(Test.class);
            if (!(test != null && test.expected() != Test.None.class)) {
                // Not Expected Exception, and non thrown internally
                if (exception == null) {
                    exception = failure.getException();
                }
            }
        }

        @Override
        public void testFinished(Description description) throws Exception {
            Test test = description.getAnnotation(Test.class);
            if (test != null && test.expected() != Test.None.class) {
                if (exception == null) {
                    exception = State.getTestException();
                }
            }
            State.caughtTestException(null);
        }
    }

    private class InstanceRequest extends Request {
        private final Class<?> _clazz;
        private final Object _testObject;
        private final String _methodName;
        private final ExpectedExceptionHolder _exceptionHolder = new ExpectedExceptionHolder();

        public InstanceRequest(Class<?> clazz, final Object testObject, String methodName) {
            _clazz = clazz;
            _testObject = testObject;
            _methodName = methodName;
        }

        @Override
        public Runner getRunner() {
            try {
                final Description method = Description.createTestDescription(_clazz, _methodName);
                final InstanceRunner runner = new InstanceRunner(_clazz, _testObject);
                runner.filter(Filter.matchMethodDescription(method));
                return runner;
            } catch (InitializationError | NoTestsRemainException ex) {
                _Logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
        
    }

    private class InstanceRunner extends BlockJUnit4ClassRunner {
        private final Class<?> _clazz;
        private final Object _testObject;

        public InstanceRunner(final Class<?> clazz, final Object testObject) throws InitializationError {
            super(clazz);
            _clazz = clazz;
            _testObject = testObject;
        }

        @Override
        public Object createTest() throws Exception {
            return _testObject;
        }
    }
}
