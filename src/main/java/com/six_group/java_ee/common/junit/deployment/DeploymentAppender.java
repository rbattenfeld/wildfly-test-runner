package com.six_group.java_ee.common.junit.deployment;

import java.util.Map;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

import com.six_group.java_ee.common.junit.arquillian.Validate;
import com.six_group.java_ee.common.junit.descriptor.ApplicationDescriptor;

public enum DeploymentAppender {
    INSTANCE;
    
    public static final ArchivePath WEB_XML_PATH = ArchivePaths.create("WEB-INF/web.xml");
    public static final ArchivePath APPLICATION_XML_PATH = ArchivePaths.create("META-INF/application.xml");   
    
    public Archive<?> append(Archive<?> applicationArchive) {
        return generateDeployment(applicationArchive);
    }

    // -----------------------------------------------------------------------||
    // -- Private Methods ----------------------------------------------------||
    // -----------------------------------------------------------------------||

    private Archive<?> generateDeployment(final Archive<?> applicationArchive) {
        if (Validate.isArchiveOfType(EnterpriseArchive.class, applicationArchive)) {
            return handleArchive(applicationArchive.as(EnterpriseArchive.class));
        }

        if (Validate.isArchiveOfType(WebArchive.class, applicationArchive)) {
            return handleArchive(applicationArchive.as(WebArchive.class));
        }

        if (Validate.isArchiveOfType(JavaArchive.class, applicationArchive)) {
            return handleArchive(applicationArchive.as(JavaArchive.class));
        }

        throw new IllegalArgumentException(applicationArchive.getName() + " can not handle archive of type " + applicationArchive.getClass().getName());
    }

    private Archive<?> handleArchive(final WebArchive applicationArchive) {
        return applicationArchive.addAsLibraries(DeploymentCreator.INSTANCE.getDeplyomentAsJar());
    }

    private Archive<?> handleArchive(final JavaArchive applicationArchive) {
        return handleArchive(ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsLibrary(applicationArchive)
                .addAsLibrary(DeploymentCreator.INSTANCE.getDeplyomentAsJar()));
    }

    private Archive<?> handleArchive(final EnterpriseArchive applicationArchive) {
        final Map<ArchivePath, Node> applicationArchiveWars = applicationArchive.getContent(Filters.include(".*\\.war"));
        if (applicationArchiveWars.size() == 1) {
            final ArchivePath warPath = applicationArchiveWars.keySet().iterator().next();
            try {
                handleArchive(applicationArchive.getAsType(WebArchive.class, warPath));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Can not manipulate war's that are not of type " + WebArchive.class, ex);
            }
        } else if (applicationArchiveWars.size() > 1) {
            throw new IllegalArgumentException("Multimple war deployment is not supported! ");
        } else {
            final Archive<?> auxArchive = DeploymentCreator.INSTANCE.getDeplyomentAsWar();
            applicationArchive.addAsModule(auxArchive);
            if (applicationArchive.contains(APPLICATION_XML_PATH)) {
                ApplicationDescriptor applicationXml = Descriptors.importAs(ApplicationDescriptor.class).from(applicationArchive.get(APPLICATION_XML_PATH).getAsset().openStream());
                applicationXml.webModule(auxArchive.getName(), calculateContextRoot(auxArchive.getName()));
                applicationArchive.delete(APPLICATION_XML_PATH);
                applicationArchive.setApplicationXML(new StringAsset(applicationXml.exportAsString()));
            }
        }
        return applicationArchive;
    }

    public static String calculateContextRoot(String archiveName) {
        String correctedName = archiveName;
        if (correctedName.startsWith("/")) {
           correctedName = correctedName.substring(1);
        }
        if (correctedName.indexOf(".") != -1) {
           correctedName = correctedName.substring(0, correctedName.lastIndexOf("."));
        }
        return correctedName;
    }
}
