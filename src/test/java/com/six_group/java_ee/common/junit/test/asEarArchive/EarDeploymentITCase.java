package com.six_group.java_ee.common.junit.test.asEarArchive;

import static org.junit.Assert.fail;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.six_group.java_ee.common.junit.api.DeploymentClass;
import com.six_group.java_ee.common.junit.api.Managed;
import com.six_group.java_ee.common.junit.client.WildflyTestRunner;
import com.six_group.java_ee.common.junit.test.LocalCacheAccessBean;

@RunWith(WildflyTestRunner.class)
@Managed(jbossHomeParent = "./target", 
         startupTimeout = 3000, 
         overwrite = false, 
         zipFile = "./dist/wildfly-10.0.0.Final.zip", 
         adminPort = 10090, 
         javaOpts = "-Djboss.socket.binding.port-offset=100",
         cliScript = "")
@DeploymentClass(deploymentClass = "com.six_group.java_ee.common.junit.test.asEarArchive.EarDeploymentCreator")
public class EarDeploymentITCase {

    @Test
    public void successTest() throws Exception {
        final Context ctx = new InitialContext();
        final LocalCacheAccessBean testBean = (LocalCacheAccessBean)ctx.lookup("java:module/LocalCacheAccessBean!com.six_group.java_ee.common.junit.test.LocalCacheAccessBean");
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
