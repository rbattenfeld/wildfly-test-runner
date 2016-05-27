//package com.six_group.java_ee.common.junit.deprecated;
//
//import java.lang.reflect.Method;
//
//import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
//import org.jboss.arquillian.test.spi.TestMethodExecutor;
//
///**
// * TestRunnerAdaptor
// * 
// * Need to be Thread-safe
// *
// * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
// * @version $Revision: $
// */
//public interface TestRunnerAdaptor {
//
//   /**
//    * Activate a new TestSuite.<br/> 
//    * This will trigger the BeforeSuite event.
//    * 
//    * @throws Exception
//    */
//   void beforeSuite() throws Exception; 
//   
//   /**
//    * Deactivate the TestSuite.<br/>
//    * This will trigger the AfterSuite event.
//    * 
//    * @throws Exception
//    */
//   void afterSuite() throws Exception;
//
//   /**
//    * Activate a new TestClass.<br/>
//    * This will trigger the BeforeClass event.
//    * 
//    * @param testClass
//    * @param executor
//    * @throws Exception
//    */
//   void beforeClass(Class<?> testClass, LifecycleMethodExecutor executor) throws Exception;
//   
//   /**
//    * Deactivate the TestClass.<br/>
//    * This will trigger the AfterClass event.
//    * 
//    * @param testClass
//    * @param executor
//    * @throws Exception
//    */
//   void afterClass(Class<?> testClass, LifecycleMethodExecutor executor) throws Exception;
//   
//   /**
//    * Activate a new TestInstance.<br/>
//    * This will trigger the Before event.
//    * 
//    * @param testInstance
//    * @param testMethod
//    * @param executor
//    * @throws Exception
//    */
//   void before(Object testInstance, Method testMethod, LifecycleMethodExecutor executor) throws Exception;
//   
//   /**
//    * Deactivate the TestInstance.<br/>
//    * This will trigger the After event.
//    * 
//    * @param testInstance
//    * @param testMethod
//    * @param executor
//    * @throws Exception
//    */
//   void after(Object testInstance, Method testMethod, LifecycleMethodExecutor executor) throws Exception;
//
//   /**
//    * Activate a TestMethod execution.<br/>
//    * This will trigger the Test event.
//    * 
//    * @param testMethodExecutor
//    * @return
//    * @throws Exception
//    */
//   TestResult test(TestMethodExecutor testMethodExecutor) throws Exception;
//   
//   /**
//    * Shutdown Arquillian cleanly.  
//    */
//   void shutdown();
//   
//}
