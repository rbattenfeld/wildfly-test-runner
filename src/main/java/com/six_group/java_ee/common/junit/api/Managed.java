package com.six_group.java_ee.common.junit.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Managed {

    /**
     * The WildFly Application Server's home directory. If not used, WildFly will be downloaded.
     * @return
     */
    String jbossHomeParent() default "";
    
    /**
     * The WildFly Application Server's admin port.
     * @return
     */
    int adminPort() default 9990;

    /**
     * The WildFly Application Server's http port.
     * @return
     */
    int httpPort() default 0;
    
    /**
     * The downloaded wildfly zip file.
     * @return
     */
    String zipFile() default "";

    /**
     * The clears the target folder.
     * @return
     */
    boolean overwrite() default true;
    
    /**
     * A string of the form groupId:artifactId:version[:packaging][:classifier]. Any missing portion of the artifact
     * will be replaced with the it's appropriate default property value
     */
    String artifact() default "";
    
    /**
     * The modules path or paths to use. A single path can be used or multiple paths by enclosing them in a paths
     * element.
     */
    String modulesPath() default "";
    
    /**
     * The JVM options to use.
     */
    String[] javaOpts() default {};
    
    /**
     * The {@code JAVA_HOME} to use for launching the server.
     */
    String javaHome() default "";

    /**
     * The path to the server configuration to use.
     */
    String serverConfig() default "standalone.xml";

    /**
     * The path to the system properties file to load.
     */
    String propertiesFile() default "";

    /**
     * The path to the cli script to be applied.
     */
    String cliScript() default "";
    
    /**
     * The timeout value to use when starting the server.
     */
    long startupTimeout() default 60L;

    /**
     * The arguments to be passed to the server.
     */
    String[] serverArgs() default {};
}
