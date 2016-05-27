package com.six_group.java_ee.common.junit.test.asEarArchive;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.six_group.java_ee.common.junit.api.IDeployable;
import com.six_group.java_ee.common.junit.test.LocalCacheAccessBean;
import com.six_group.java_ee.common.junit.test.asWarArchive.WarDeploymentAsWarArchiveITCase;
import com.six_group.java_ee.common.junit.test.asWarArchiveWithContext.WarDeploymentWithContextITCase;

public class EarDeploymentCreator implements IDeployable {

	@Override
	public Archive<?> getDeplyoment() {		
    	final WebArchive testlocalCacheArchive = ShrinkWrap.create(WebArchive.class, "test234djdjdjtt34.war")
    	        .addClass(WarTestServlet.class)
                .addClass(EarDeploymentITCase.class)
                .addClass(WarDeploymentAsWarArchiveITCase.class)
                .addClass(WarDeploymentWithContextITCase.class)
                .addClass(LocalCacheAccessBean.class)
        		.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    	final EnterpriseArchive earArchive = ShrinkWrap.create(EnterpriseArchive.class, "testxhdixderttttttrh6.ear")
    	        .addAsModule(testlocalCacheArchive);
		return earArchive;
	}

}
