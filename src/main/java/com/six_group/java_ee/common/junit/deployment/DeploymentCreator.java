package com.six_group.java_ee.common.junit.deployment;

import java.io.File;
import java.io.IOException;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public enum DeploymentCreator {
    INSTANCE;
    public final String _warTestArchiveName = "testa9jrhvn45mff.war";
    
	public Archive<?> getDeplyomentAsWar() {	    
	    return ShrinkWrap.create(WebArchive.class, _warTestArchiveName)
            .addPackages(true, "junit", "org.junit", "org.hamcrest", 
                    "com.six_group.java_ee.common.junit.arquillian",
                    "com.six_group.java_ee.common.junit.container",
                    "com.six_group.java_ee.common.junit.servlet",
                    "com.six_group.java_ee.common.junit.client", 
                    "com.six_group.java_ee.common.junit.api",
                    "org.jboss.arquillian");
	}
	
    public Archive<?> getDeplyomentAsJar() {     
        return ShrinkWrap.create(JavaArchive.class, "testa9jrhvn45mff.jar")
            .addPackages(true, "junit", "org.junit", "org.hamcrest", 
                    "com.six_group.java_ee.common.junit.arquillian",
                    "com.six_group.java_ee.common.junit.container",
                    "com.six_group.java_ee.common.junit.servlet",
                    "com.six_group.java_ee.common.junit.client", 
                    "com.six_group.java_ee.common.junit.api",
                    "org.jboss.arquillian")
            .addAsManifestResource("com/six_group/java_ee/common/junit/servlet/web-fragment.xml", "web-fragment.xml");
    }

    public File getTempFileFromArchive(final Archive<?> archive) {
        try {
            final String[] items = archive.getName().split("\\.", -1);
            final File temp = File.createTempFile(items[0], "." + items[1]);
            archive.as(ZipExporter.class).exportTo(temp, true);
            return temp;
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
