/**
 * junit-remote
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * @author Tradeshift - http://www.tradeshift.com
 */

package com.six_group.java_ee.common.junit.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ejb.Stateless;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.wildfly.plugin.deployment.Deployment;
import org.wildfly.plugin.deployment.standalone.StandaloneDeploymentBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.six_group.java_ee.common.junit.api.DeploymentClass;
import com.six_group.java_ee.common.junit.api.IDeployable;
import com.six_group.java_ee.common.junit.api.Managed;
import com.six_group.java_ee.common.junit.api.Utils;
import com.six_group.java_ee.common.junit.arquillian.TestResult;
import com.six_group.java_ee.common.junit.arquillian.Validate;
import com.six_group.java_ee.common.junit.deployment.DeploymentAppender;
import com.six_group.java_ee.common.junit.deployment.DeploymentCreator;

public class ClientRemoteRunner extends BlockJUnit4ClassRunner {
    private static final Log _Logger = LogFactory.getLog(ClientRemoteRunner.class);
    private static final ArchivePath JBOSS_WEB_XML_PATH = ArchivePaths.create("WEB-INF/jboss-web.xml");
    private static final AtomicInteger _runningCount = new AtomicInteger(0);
    private final Class<? extends Runner> _remoteRunnerClass = BlockJUnit4ClassRunner.class;
    private final Class<?> _testClass;
    private final Class<?> _deploymentClass;
    private final Managed _managed;
    private final ExecutorService _serverExecutorService = Executors.newSingleThreadExecutor();
    private final Stateless _stateless;
    private final boolean _isFirstTestCase;
    private final boolean _isLastTestCase;
    private String _internalDeploymentName = null;
    private String _internalWebContext = null;
    private File _internalDeploymentFile = null;    
    private Description _description;
    private Map<Description, String> _methodNames = new HashMap<Description, String>();
    private WildflyServer _server;

    public ClientRemoteRunner(final Class<?> testClass, final boolean isFirstTestCase, final boolean isLastTestCase) throws InitializationError {
    	super(testClass);
    	_managed = Utils.findAnnotation(testClass, Managed.class);
        _testClass = testClass;
        _deploymentClass = getDeploymentCreatorClass(testClass);
		_stateless = Utils.findAnnotation(testClass, Stateless.class);
		_isFirstTestCase = isFirstTestCase;
		_isLastTestCase = isLastTestCase;
		initDeployment();
		initDescription(testClass);
		startWildflyIfRequired();
		deployIfRequired();
    }

    @Override
    public void filter(final Filter filter) throws NoTestsRemainException {
        final List<Description> children = _description.getChildren();
        final Iterator<Description> itr = children.iterator();
        while (itr.hasNext()) {
            Description child = itr.next();
            if (!filter.shouldRun(child)) {
                itr.remove();
                _methodNames.remove(child);
            }
        }

        if (children.isEmpty()) {
            throw new NoTestsRemainException();
        }
    }

    @Override
    public void sort(final Sorter sorter) {
        Collections.sort(_description.getChildren(), sorter);
    }

    @Override
    public Description getDescription() {
        return _description;
    }

    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
    	final Description description = describeChild(method);
    	if (method.getAnnotation(Ignore.class) != null) {
    		notifier.fireTestIgnored(description);
    		return;
    	}
    	try {
    		notifier.fireTestStarted(description);
    		final String methodName = method.getName();
    		final HttpURLConnection connection = getUrl(methodName, "POST");
    		handleError(connection);
    		String enc = connection.getContentEncoding();
    		if (enc == null) {
    		    enc = "ISO-8859-1";
    		}
    		final InputStream is = connection.getInputStream();
    		final ObjectInputStream objectInputStream = new ObjectInputStream(is);
    		final TestResult testResult = (TestResult) objectInputStream.readObject();
    		_Logger.debug(testResult.toString());
    	    if (testResult.getStatus() == TestResult.Status.FAILED) {
    		    notifier.fireTestFailure(new Failure(description, testResult.getThrowable()));
    		} else if (testResult.getStatus() == TestResult.Status.SKIPPED) {
    		    notifier.fireTestIgnored(description);
    		}
    		is.close();
    		connection.disconnect();
    	} catch (Throwable ex) {
    	    _Logger.error(ex.getMessage(), ex);
    		notifier.fireTestFailure(new Failure(description, ex));
    	} finally {
    		notifier.fireTestFinished(description);
    		_runningCount.decrementAndGet();
    		if (_isLastTestCase && _runningCount.get() == 0) {
    		    undeploy(_internalDeploymentName);
                if (_serverExecutorService != null) {
                    _serverExecutorService.shutdown();
                } else {
                    _server.stop();
                }
            }
    	}
    }

    //-----------------------------------------------------------------------||
    //-- Private Methods ----------------------------------------------------||
    //-----------------------------------------------------------------------||

    private void deployIfRequired() {
        if (_isFirstTestCase) {
            waitForServer(_server, _managed.startupTimeout());
            deploy(_internalDeploymentName, _internalDeploymentFile);
        }
    }
    
    private void startWildflyIfRequired() throws InitializationError {
        _server = new WildflyServer();  
        if (!_isFirstTestCase) {
            final WildflyServerManager serverManager = new WildflyServerManager(_server);
            serverManager.start(_managed, _isFirstTestCase); 
        } else {
            _serverExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    final WildflyServerManager serverManager = new WildflyServerManager(_server);
                    serverManager.start(_managed, _isFirstTestCase);            
                }                
            });
        }
    }
    
    private void handleError(HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() != 200) {
            String error = null;
            InputStream err = connection.getErrorStream();
            if (err != null) {
                error = Utils.toString(err);
            }
            if (error == null) {
                error = connection.getResponseMessage();
            }
            throw new RuntimeException("Unable to send request to " + connection.getURL() + ": " + error);
        }
    }
    
    private HttpURLConnection getUrl(String methodName, String httpMethod) {
    	String statelessStr = null;
    	try {
		    final String url = getHttpUrl();
		    if (_stateless != null) {
		        statelessStr = "stateless";
		    }
			final HttpURLConnection connection = (HttpURLConnection) new URL(url + _internalWebContext + "/TestExecutorServlet" + "?className=" + _testClass.getName() + "&methodName=" + methodName + "&runner=" + _remoteRunnerClass.getName() + "&stateless=" + statelessStr).openConnection();
            connection.setReadTimeout(120000);
			connection.setAllowUserInteraction(false);
			connection.setUseCaches(false);
			connection.setRequestMethod(httpMethod);
			connection.setRequestProperty("Connection", "close");
			connection.connect();
			return connection;
		} catch (final Exception ex) {
			throw new RuntimeException("Unable to connect", ex);
		}
    }

    private void deploy(final String archiveName, final File archiveFile) {
    	Deployment.Status status = null;
    	try {
	    	final ModelControllerClient client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), Integer.valueOf(_managed.adminPort()));
	        final Deployment deployment = new StandaloneDeploymentBuilder(client)
	                .setContent(archiveFile)
	                .setName(archiveName)
	                .setType(Deployment.Type.FORCE_DEPLOY)
	                .build();
	        status = deployment.execute();
    	} catch (final Exception ex) {
    		throw new RuntimeException(ex);
    	}
    	if (status != Deployment.Status.SUCCESS) {
        	throw new RuntimeException();
        }
    }
    
    private void undeploy(final String archiveName) {
    	Deployment.Status status = null;
    	try {
	    	final ModelControllerClient client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), Integer.valueOf(_managed.adminPort()));
	        final Deployment deployment = new StandaloneDeploymentBuilder(client)
	                .setName(archiveName)
	                .setType(Deployment.Type.UNDEPLOY)
	                .build();
	        status = deployment.execute();
    	} catch (final Exception ex) {
    		throw new RuntimeException();
    	}
    	if (status != Deployment.Status.SUCCESS) {
        	throw new RuntimeException();
        }
    }
    
    private void initDeployment() throws InitializationError {
    	try {
	    	final Constructor<?>[] constructors = _deploymentClass.getConstructors();
	    	final IDeployable deployCreator = (IDeployable) constructors[0].newInstance();
	    	final Archive<?> appDeployment = deployCreator.getDeplyoment();
	    	final Archive<?> testDeployment = DeploymentAppender.INSTANCE.append(appDeployment);
	    	_internalDeploymentName = testDeployment.getName();
	    	_internalDeploymentFile = DeploymentCreator.INSTANCE.getTempFileFromArchive(testDeployment);
	    	_internalWebContext = getWebContext(testDeployment);
    	} catch (final Exception ex) {
			throw new InitializationError(ex);
    	}
    }

    private String getHttpUrl() throws InitializationError {
        try {
            int httpPort = 8080;
            if (_managed.httpPort() != 0) {
                httpPort = _managed.httpPort();
            } else {
                final ModelControllerClient client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), Integer.valueOf(_managed.adminPort()));
                final ModelNode op = new ModelNode();
                op.get("operation").set("read-resource");           
                op.get("include-runtime").set(true);
                final ModelNode address = op.get("address");         
                address.add("socket-binding-group", "standard-sockets");        
                address.add("socket-binding", "http");
                final ModelNode returnVal = client.execute(op);
                final ModelNode node2 = returnVal.get("result");
                httpPort = node2.get("bound-port").asInt();
            }
            return "http://localhost:" + httpPort;
        } catch (final Exception ex) {
            throw new InitializationError(ex);
        }
    }

    private String getWebContext(final WebArchive applicationArchive) {
        if (applicationArchive.contains(JBOSS_WEB_XML_PATH)) {
            return getWebContextFromJBossXml(applicationArchive);
        } else {
            return getWebContextFromArchiveName(applicationArchive);
        }
    }

    private WebArchive getWebArchive(final Archive<?> testDeployment) {
        WebArchive webArchive = null;
        if (Validate.isArchiveOfType(EnterpriseArchive.class, testDeployment)) {
            final Map<ArchivePath, Node> applicationArchiveWars = testDeployment.getContent(Filters.include(".*\\.war"));
            if (applicationArchiveWars.size() == 1) {
                final ArchivePath warPath = applicationArchiveWars.keySet().iterator().next();
                webArchive = testDeployment.getAsType(WebArchive.class, warPath);
            }
        } else if (Validate.isArchiveOfType(WebArchive.class, testDeployment)) {
            webArchive = testDeployment.as(WebArchive.class);
        }
        return webArchive;
    }

    private String getWebContext(final Archive<?> testDeployment) {
        final WebArchive webArchive = getWebArchive(testDeployment);
        if (webArchive != null) {
            return getWebContext(webArchive);
        } else {
            throw new IllegalArgumentException("No web archive found: " + testDeployment.getName());
        }
    }

    private String getWebContextFromArchiveName(final WebArchive applicationArchive) {
        String webContext = "/" +  applicationArchive.getName();
        int pos = webContext.indexOf('.');
        if (pos > 0) {
            webContext = webContext.substring(0, pos);
        }
        return webContext;
    }

    private String getWebContextFromJBossXml(final WebArchive applicationArchive) {
        String webContext = null;
        try {                
            boolean isStartElementFound = false;
            final XMLInputFactory factory = XMLInputFactory.newInstance();
            final XMLStreamReader reader = factory.createXMLStreamReader(applicationArchive.get(JBOSS_WEB_XML_PATH).getAsset().openStream());
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equals("context-root")) {
                        isStartElementFound = true;
                    }
                } else if (reader.getEventType() == XMLStreamConstants.CHARACTERS) {
                    if (isStartElementFound) {
                        webContext = reader.getText();
                        isStartElementFound = false;
                    }
                }
                reader.next();
            }
            return webContext;
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void initDescription(final Class<?> testClass) {
        final TestClass tc = new TestClass(testClass);
        _description = Description.createTestDescription(testClass, tc.getName(), tc.getAnnotations());        
        for (final FrameworkMethod method : tc.getAnnotatedMethods(Test.class)) {
            final String methodName = method.getName();
            final Description child = Description.createTestDescription(testClass, methodName, method.getAnnotations());    
            _methodNames.put(child, methodName);
            _description.addChild(child);
        }
        _runningCount.set(_description.testCount());
    }

    private static Class<?> getDeploymentCreatorClass(final Class<?> testClass) {
        final DeploymentClass deployment = Utils.findAnnotation(testClass, DeploymentClass.class);
    	try {
    		return Class.forName(deployment.deploymentClass());
    	} catch (final ClassNotFoundException ex) {
    		throw new RuntimeException(ex);
    	}
    }

    private void waitForServer(final WildflyServer server, final long timeout) {
        for (int i = 0; i < timeout; i++) {
            if (server.isRunning()) {
                return;
            }
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (final InterruptedException ignore) {
            }
        }
        throw new RuntimeException("Wildfly server not started in " + timeout + " seconds!");
    }
    
    private class JBossWebXmlSAXHandler extends DefaultHandler {
        private String _context = null;
        private String _content = null;
        
        public String getContext() {
            return _context;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
            case "context-root":
                _context = _content;
                break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            _content = String.copyValueOf(ch, start, length).trim();
        }
    }
    
    private class DummyEntityResolver implements EntityResolver {
        
        public InputSource resolveEntity(String publicID, String systemID) throws SAXException {
            return new InputSource(new StringReader(""));
        }
    }
}

