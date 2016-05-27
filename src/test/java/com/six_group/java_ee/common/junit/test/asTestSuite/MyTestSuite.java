package com.six_group.java_ee.common.junit.test.asTestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.six_group.java_ee.common.junit.client.WildflyTestSuite;
import com.six_group.java_ee.common.junit.test.asEarArchive.EarDeploymentITCase;
import com.six_group.java_ee.common.junit.test.asWarArchive.WarDeploymentAsWarArchiveITCase;

import junit.framework.TestSuite;

@RunWith(AllTests.class)
public class MyTestSuite {

	public static TestSuite suite() {
		return new WildflyTestSuite(
				EarDeploymentITCase.class, 
				WarDeploymentAsWarArchiveITCase.class
		);
	}
}