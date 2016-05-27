package com.six_group.java_ee.common.junit.client;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class WildflyTestSuite extends TestSuite {

    /**
     * Constructs a WildflyTestSuite from the given array of classes.
     *
     * @param classes {@link TestCase}s
     */
    public WildflyTestSuite(final Class<?>... classes) {
        if (classes.length == 1) {
            addTest(new WildflyTestAdapter(classes[0], true, true));
        } else if (classes.length == 2) {
            addTest(new WildflyTestAdapter(classes[0], true, false));
            addTest(new WildflyTestAdapter(classes[1], false, true));
        } else if (classes.length > 2) {
            for (int i = 0; i < classes.length; i++) {
                if (i == 0) {
                    addTest(new WildflyTestAdapter(classes[i], true, false));
                } else if (i >= classes.length - 1) {
                    addTest(new WildflyTestAdapter(classes[i], false, true));
                } else {
                    addTest(new WildflyTestAdapter(classes[i], false, false));
                }
            }
        }
    }
    
}
