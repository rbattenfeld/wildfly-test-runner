package com.six_group.java_ee.common.junit.test;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import com.six_group.java_ee.common.junit.api.IDeployable;

public class DeploymentCreator implements IDeployable {

	@Override
	public Archive<?> getDeplyoment() {		
    	final JavaArchive testlocalCacheArchive = ShrinkWrap.create(JavaArchive.class, "testLocalCache.jar")
        		.addPackages(true, "com.six_group.java_ee.common.lifecycle.ejb.local.cache")
                .addClass(PojoManagedITCase.class)
                .addClass(LocalCacheAccessBean.class)
        		.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");    	
//    	final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, _ArchiveName)
//    			.addAsModule(testlocalCacheArchive);    	
		return testlocalCacheArchive;
	}

}
