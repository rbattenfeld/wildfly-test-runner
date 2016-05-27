package com.six_group.java_ee.common.junit.test.asWarArchive;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.six_group.java_ee.common.junit.api.IDeployable;
import com.six_group.java_ee.common.junit.test.LocalCacheAccessBean;

public class WarDeploymentCreator implements IDeployable {

	@Override
	public Archive<?> getDeplyoment() {		
    	final WebArchive testlocalCacheArchive = ShrinkWrap.create(WebArchive.class, "test234djdjdjtt34.war")
    	        .addClass(WarTestServlet.class)
                .addClass(WarDeploymentAsWarArchiveITCase.class)
                .addClass(LocalCacheAccessBean.class)
        		.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");    	
//    	final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, _ArchiveName)
//    			.addAsModule(testlocalCacheArchive);    	
		return testlocalCacheArchive;
	}

}
