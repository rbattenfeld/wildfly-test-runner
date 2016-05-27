package com.six_group.java_ee.common.junit.client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.ModelControllerClientConfiguration;
import org.wildfly.core.launcher.StandaloneCommandBuilder;

import com.six_group.java_ee.common.junit.api.Managed;

public class WildflyServerManager {
    private static final Log _Logger = LogFactory.getLog(WildflyServerManager.class);
    private static final String _USER_HOME = System.getProperty( "user.home" );
    private static final File USERM_MAVEN_CONFIGURATION_HOME = new File(_USER_HOME, ".m2");
    private static final String ENV_M2H_HOME = System.getenv("M2_HOME");
    private static final File DEFAULT_USER_SETTINGS_FILE = new File( USERM_MAVEN_CONFIGURATION_HOME, "settings.xml");
    private static final File DEFAULT_GLOBAL_SETTINGS_FILE = new File( System.getProperty("maven.home", ENV_M2H_HOME != null ? ENV_M2H_HOME : ""), "conf/settings.xml");
    private static final String WILDFLY_DIR = "wildfly-run";
    private static final String _TEMP_CONFIG_FILE_NAME = "standalone-temp928374.xml";
    private WildflyServer _server;
    
    public WildflyServerManager(WildflyServer wildflyServer) {
        _server = wildflyServer;
    }

    public void start(final Managed managed, final boolean isFirstTestCase) {
        final Path jbossHome = extractIfRequired(managed);
        if (!Files.isDirectory(jbossHome)) {
            throw new RuntimeException(String.format("JBOSS_HOME '%s' is not a valid directory.", jbossHome));
        }
        
        String javaHome = managed.javaHome();
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
        }
        
        final StandaloneCommandBuilder commandBuilder = StandaloneCommandBuilder.of(jbossHome).setJavaHome(javaHome);
        if (managed.javaOpts() != null) {
            commandBuilder.setJavaOptions(managed.javaOpts());
        }

        setTemporaryConfigurationFile(managed, commandBuilder);
     
        if (managed.propertiesFile() != null && !managed.propertiesFile().isEmpty()) {
            commandBuilder.setPropertiesFile(managed.propertiesFile());
        }

        if (managed.serverArgs() != null && managed.serverArgs().length > 0) {
            commandBuilder.addServerArguments(managed.serverArgs());
        }

        Long timeout = managed.startupTimeout();
        if (timeout == null || timeout == 0) {
            timeout = new Long(60L);
        }
        
        commandBuilder.toString();
        
        _Logger.info(String.format("JAVA_HOME=%s", commandBuilder.getJavaHome()));
        _Logger.info(String.format("JBOSS_HOME=%s%n", commandBuilder.getWildFlyHome()));
        try {   
            final ModelControllerClient client = createClient(null, "localhost", managed.adminPort());
            _server.setCommandBuilder(commandBuilder);
            _server.setClient(client);
            _server.setStdout(null);
            if (isFirstTestCase) {
                _server.start(managed.startupTimeout());
                _server.checkServerState();
                while (_server.isRunning()) {
                    TimeUnit.SECONDS.sleep(1L);
                }
            }
        } catch (final Exception ex) {
            _Logger.error(ex.getMessage(), ex);
            throw new RuntimeException("The server failed to start", ex);
        }
    }

    public void stop() {
        if (_server != null) {
            _server.stop();
        }        
    }

    //-----------------------------------------------------------------------||
    //-- Private Methods ----------------------------------------------------||
    //-----------------------------------------------------------------------||

    private static Path extractIfRequired(final Managed managed) {
        if (managed.jbossHomeParent() != null && managed.zipFile() == null) {
            return Paths.get(managed.jbossHomeParent());
        }
        final Path result = new File(managed.zipFile()).toPath();
        final Path target = new File(managed.jbossHomeParent()).toPath();
        final Path jbossHome = new File(managed.jbossHomeParent() + "/" + getJBossHome(result)).toPath();
        if (Files.exists(jbossHome) && managed.overwrite()) {
            try {
                Archives.deleteDirectory(jbossHome);
            } catch (IOException e) {
                throw new RuntimeException("Could not delete target directory: " + target, e);
            }
        }
        try {
            if (!Files.exists(jbossHome) || managed.overwrite()) {
            	System.out.println(result.toAbsolutePath().toString());
                Archives.unzip(result, target);
            }
            return jbossHome;
        } catch (IOException e) {
            throw new RuntimeException("Artifact was not successfully extracted: " + result, e);
        }
    }

    private static String getJBossHome(final Path result) {
        final String filename = result.getFileName().toString();
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    private static ModelControllerClient createClient(final String protocol, final String hostName, final int port) throws UnknownHostException {
        return ModelControllerClient.Factory.create(protocol, hostName, port);
    }

    private void setTemporaryConfigurationFile(final Managed managed, final StandaloneCommandBuilder commandBuilder) {
        try {
            String configFileName = "standalone.xml";
            if (managed.serverConfig() != null && !managed.serverConfig().isEmpty()) {
                configFileName = managed.serverConfig();
            }
            final String configFileNameNew = configFileName.replaceAll(".xml", "-tmp84756.xml");
            final Path srcPath  = new File(commandBuilder.getConfigurationDirectory().toString(), configFileName).toPath();
            final Path destPath = new File(commandBuilder.getConfigurationDirectory().toString(), configFileNameNew).toPath();
            Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
            commandBuilder.setServerConfiguration(configFileNameNew);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
