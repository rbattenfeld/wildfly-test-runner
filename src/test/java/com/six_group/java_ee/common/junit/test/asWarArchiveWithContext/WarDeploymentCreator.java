package com.six_group.java_ee.common.junit.test.asWarArchiveWithContext;

import java.io.File;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.six_group.java_ee.common.junit.api.IDeployable;
import com.six_group.java_ee.common.junit.test.LocalCacheAccessBean;

public class WarDeploymentCreator implements IDeployable {

	@Override
	public Archive<?> getDeplyoment() {
	    final File jbossXmlFile = new File("src/test/resources/jboss-web.xml");
    	final WebArchive testlocalCacheArchive = ShrinkWrap.create(WebArchive.class, "test234djdjdjtt34.war")
    	        .addClass(WarTestServlet.class)
                .addClass(WarDeploymentWithContextITCase.class)
                .addClass(LocalCacheAccessBean.class)
                .addAsWebInfResource(jbossXmlFile, "jboss-web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml"); 
		return testlocalCacheArchive;
	}

}
