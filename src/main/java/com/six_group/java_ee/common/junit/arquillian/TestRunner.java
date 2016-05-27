package com.six_group.java_ee.common.junit.arquillian;

/**
 * TestRunner
 * 
 * A Generic way to start the test framework.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface TestRunner {
   /**
    * Run a single test method in a test class.
    * 
    * @param testClass The test case class to execute
    * @param methodName The method to execute
    * @return The result of the test
    */
   TestResult execute(Class<?> testClass, final Object testObject, String methodName);
//   TestResult execute(Class<?> testClass, String methodName);
}
