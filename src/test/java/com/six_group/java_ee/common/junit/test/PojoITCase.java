package com.six_group.java_ee.common.junit.test;

import static org.junit.Assert.fail;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.six_group.java_ee.common.junit.api.DeploymentClass;
import com.six_group.java_ee.common.junit.api.Remote;
import com.six_group.java_ee.common.junit.client.WildflyTestRunner;

@RunWith(WildflyTestRunner.class)
@Remote(endpoint = "http://localhost:8080/")
@DeploymentClass(deploymentClass = "com.six_group.java_ee.common.junit.test.DeploymentCreator")
@Ignore
public class PojoITCase {

    @Test
    public void successTest() throws Exception {
        final Context ctx = new InitialContext();
        final LocalCacheAccessBean testBean = (LocalCacheAccessBean)ctx.lookup("java:app/test/LocalCacheAccessBean");
        sleep(1000);
    }

    @Test(expected=AssertionError.class)
    public void failedTest() {
        fail();
    }

    //-----------------------------------------------------------------------||
    //-- Private Methods ----------------------------------------------------||
    //-----------------------------------------------------------------------||    

    private static void sleep(final long time) {    	
    	try {
    		Thread.currentThread();
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
    }
}
