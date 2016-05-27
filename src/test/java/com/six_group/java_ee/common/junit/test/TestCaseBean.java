package com.six_group.java_ee.common.junit.test;

import static org.junit.Assert.assertEquals;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

@Stateless
@Ignore
public class TestCaseBean implements ITestCaseImpl {
    private static final Log _Logger = LogFactory.getLog(TestCaseBean.class);
    
    @EJB
    private LocalCacheAccessBean _localCacheAccess;

    @Test
    public void test() throws Exception {
    	assertEquals("hello",_localCacheAccess.sayHello());
    }
	
}
