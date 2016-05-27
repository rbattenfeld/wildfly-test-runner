package com.six_group.java_ee.common.junit.test;

import static org.junit.Assert.fail;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.six_group.java_ee.common.junit.api.DeploymentClass;
import com.six_group.java_ee.common.junit.api.Remote;
import com.six_group.java_ee.common.junit.client.WildflyTestRunner;

@RunWith(WildflyTestRunner.class)
@Remote(endpoint = "http://localhost:8080/")
@DeploymentClass(deploymentClass = "com.six_group.java_ee.common.junit.test.DeploymentCreator")
@Stateless(mappedName="java:test/LocalCacheITCase")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Ignore
public class LocalCacheITCase {

//	@EJB
//	private ITestCaseImpl _testCase;

    @Test
    public void successTest() {
        sleep(1000);
    }

    @Test
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
