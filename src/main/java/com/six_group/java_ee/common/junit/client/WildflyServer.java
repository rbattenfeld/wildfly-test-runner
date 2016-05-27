package com.six_group.java_ee.common.junit.client;

import static org.jboss.as.controller.client.helpers.ClientConstants.CONTROLLER_PROCESS_STATE_STARTING;
import static org.jboss.as.controller.client.helpers.ClientConstants.CONTROLLER_PROCESS_STATE_STOPPING;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.wildfly.core.launcher.CommandBuilder;
import org.wildfly.core.launcher.Launcher;
import org.wildfly.core.launcher.ProcessHelper;


public class WildflyServer {
    private static final ModelNode EMPTY_ADDRESS = new ModelNode().setEmptyList();
    private static final Log _Logger = LogFactory.getLog(WildflyServer.class);
    private volatile Thread _timerService = null;
    private volatile boolean _isRunning = false;
    private volatile Thread shutdownHook;
    private ModelControllerClient _client;
    private CommandBuilder _commandBuilder;
    private OutputStream _stdout;
    private Process _process;

    static {
        EMPTY_ADDRESS.protect();
    }
    
    public WildflyServer() {
        _client = null;
        _commandBuilder = null;
        _stdout = null;
    }

    public static WildflyServer getInstance(CommandBuilder commandBuilder, final ModelControllerClient client, OutputStream stdout) {
        return new WildflyServer(commandBuilder, client, stdout);
    }

    public WildflyServer(CommandBuilder commandBuilder, final ModelControllerClient client, OutputStream stdout) {
        _client = client;
        _commandBuilder = commandBuilder;
        _stdout = stdout;
    }

    public ModelControllerClient getClient() {
        return _client;
    }

    public void setClient(ModelControllerClient client) {
        _client = client;
    }

    public CommandBuilder getCommandBuilder() {
        return _commandBuilder;
    }

    public void setCommandBuilder(CommandBuilder commandBuilder) {
        _commandBuilder = commandBuilder;
    }

    public OutputStream getStdout() {
        return _stdout;
    }

    public void setStdout(OutputStream stdout) {
        _stdout = stdout;
    }

    public final synchronized void start(final long timeout) throws IOException, InterruptedException {
        _Logger.info("Server started ...");
        final Launcher launcher = Launcher.of(_commandBuilder);
        if (_stdout == null) {
            launcher.inherit();
        } else {
            launcher.setRedirectErrorStream(true);
        }
        _process = launcher.launch();
        if (_stdout != null) {
            new Thread(new ConsoleConsumer(_process.getInputStream(), _stdout)).start();
        }
        shutdownHook = ProcessHelper.addShutdownHook(_process);
        if (!waitForStart(timeout)) {
            try {
                ProcessHelper.destroyProcess(_process);
            } catch (InterruptedException ignore) {
            }
            throw new IllegalStateException(String.format("Managed server was not started within [%d] s", timeout));
        }
        _Logger.info("Server started ... done");
    }
    
    public final synchronized void stop() {
        _Logger.info("Server stop ...");
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            try {
                _timerService.stop();
            } catch (Exception ignore) {
            }
            stopServer();
        } finally {
            try {
                ProcessHelper.destroyProcess(_process);
            } catch (InterruptedException ignore) {
            }
            _Logger.info("Server stop ... done");
        }
    }
    
    public void stopServer() {
        shutdownStandalone(_client);
        if (_client != null) {
            try {
                _client.close();
            } catch (Exception ignore) {
            }
        }
    }

    protected boolean waitForStart(long timeout) throws IOException, InterruptedException {
        return waitForStandalone(_process, _client, timeout);
    }

    public boolean isRunning() {
        return _isRunning;
    }

    protected void checkServerState() {
        _isRunning = isStandaloneRunning(_client);
    }

    //-----------------------------------------------------------------------||
    //-- Private Methods ----------------------------------------------------||
    //-----------------------------------------------------------------------||

    private static void shutdownStandalone(final ModelControllerClient client) {
        try {
            final ModelNode op = Operations.createOperation("shutdown");
            final ModelNode response = client.execute(op);
            if (Operations.isSuccessfulOutcome(response)) {
                while (true) {
                    if (isStandaloneRunning(client)) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(20L);
                        } catch (InterruptedException e) {
                            _Logger.debug("Interrupted during sleep", e);
                        }
                    } else {
                        break;
                    }
                }
            } else {
                _Logger.debug(String.format("Failed to execute %s: %s", op, Operations.getFailureDescription(response)));
            }
        } catch (IOException e) {
            _Logger.debug("Interrupted shutting down standalone", e);
        }
    }

    private static boolean isStandaloneRunning(final ModelControllerClient client) {
        try {
            final ModelNode response = client.execute(Operations.createReadAttributeOperation(EMPTY_ADDRESS, "server-state"));
            if (Operations.isSuccessfulOutcome(response)) {
                final String state = Operations.readResult(response).asString();
                return !CONTROLLER_PROCESS_STATE_STARTING.equals(state) && !CONTROLLER_PROCESS_STATE_STOPPING.equals(state);
            }
        } catch (RuntimeException | IOException e) {
            _Logger.debug("Interrupted determining if standalone is running", e);
        }
        return false;
    }

    private static boolean waitForStandalone(final Process process, final ModelControllerClient client, final long startupTimeout) throws InterruptedException, IOException {
        long timeout = startupTimeout * 1000;
        final long sleep = 100L;
        while (timeout > 0) {
            long before = System.currentTimeMillis();
            if (isStandaloneRunning(client))
                return true;
            timeout -= (System.currentTimeMillis() - before);
            if (ProcessHelper.processHasDied(process)) {
                return false;
            }
            TimeUnit.MILLISECONDS.sleep(sleep);
            timeout -= sleep;
        }
        return false;
    }

    private class Reaper implements Runnable {

        @Override
        public void run() {
            checkServerState();
            if (!isRunning()) {
                stop();
            }            
            try {
                Thread.currentThread();
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    private static class ConsoleConsumer implements Runnable {
        private final InputStream in;
        private final OutputStream out;

        ConsoleConsumer(final InputStream in, final OutputStream out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[64];
            try {
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            } catch (IOException ignore) {
            }
        }
    }
}
