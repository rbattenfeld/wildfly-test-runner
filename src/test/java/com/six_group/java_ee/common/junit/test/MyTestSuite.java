package com.six_group.java_ee.common.junit.test;

import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.six_group.java_ee.common.junit.client.WildflyTestAdapter;

@RunWith(AllTests.class)
public class MyTestSuite {

    public static TestSuite suite() {
        final TestSuite suite = new TestSuite();
        suite.addTest(new WildflyTestAdapter(PojoManagedITCase.class));
        suite.addTest(new WildflyTestAdapter(PojoManaged2ITCase.class, false, true));
        return suite;
     }
}
