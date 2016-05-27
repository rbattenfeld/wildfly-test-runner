package com.six_group.java_ee.common.junit.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.as.controller.client.helpers.domain.ServerIdentity;
import org.jboss.as.controller.client.helpers.domain.ServerStatus;
import org.wildfly.core.launcher.CommandBuilder;
import org.wildfly.core.launcher.DomainCommandBuilder;
import org.wildfly.core.launcher.Launcher;
import org.wildfly.core.launcher.ProcessHelper;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class AbstractServer {
    private final ScheduledExecutorService timerService;
    private final CommandBuilder commandBuilder;
    private final OutputStream stdout;
    private volatile Thread shutdownHook;
    private Process process;

    public AbstractServer(final CommandBuilder commandBuilder, final OutputStream stdout) {
        this.commandBuilder = commandBuilder;
        timerService = Executors.newScheduledThreadPool(1);
        this.stdout = stdout;
    }

    static AbstractServer create(final CommandBuilder commandBuilder, final ModelControllerClient client) {
        return create(commandBuilder, client, null);
    }

    static AbstractServer create(final CommandBuilder commandBuilder, final ModelControllerClient client, final OutputStream stdout) {
        if (commandBuilder instanceof DomainCommandBuilder) {
            return new AbstractServer(commandBuilder, stdout) {
                final DomainClient domainClient = DomainClient.Factory.create(client);
                final Map<ServerIdentity, ServerStatus> servers = new HashMap<>();
                volatile boolean isRunning = false;

                @Override
                protected void stopServer() {
                    WildflyServerHelper.shutdownDomain(domainClient, servers);
                    if (domainClient != null) try {
                        domainClient.close();
                    } catch (Exception ignore) {
                    }
                }

                @Override
                protected boolean waitForStart(final long timeout) throws IOException, InterruptedException {
                    return WildflyServerHelper.waitForDomain(super.process, domainClient, servers, timeout);
                }

                @Override
                public boolean isRunning() {
                    return isRunning;
                }

                @Override
                protected void checkServerState() {
                    isRunning = WildflyServerHelper.isDomainRunning(domainClient, servers);
                }
            };
        }
        return new AbstractServer(commandBuilder, stdout) {
            volatile boolean isRunning = false;

            @Override
            protected void stopServer() {
                WildflyServerHelper.shutdownStandalone(client);
                if (client != null) try {
                    client.close();
                } catch (Exception ignore) {
                }
            }

            @Override
            protected boolean waitForStart(final long timeout) throws IOException, InterruptedException {
                return WildflyServerHelper.waitForStandalone(super.process, client, timeout);
            }

            @Override
            public boolean isRunning() {
                return isRunning;
            }

            @Override
            protected void checkServerState() {
                isRunning = WildflyServerHelper.isStandaloneRunning(client);
            }
        };
    }

    /**
     * Starts the server.
     *
     * @throws IOException the an error occurs creating the process
     */
    public final synchronized void start(final long timeout) throws IOException, InterruptedException {
        final Launcher launcher = Launcher.of(commandBuilder);
        // Determine if we should consume stdout
        if (stdout == null) {
            launcher.inherit();
        } else {
            launcher.setRedirectErrorStream(true);
        }
        process = launcher.launch();
        if (stdout != null) {
            new Thread(new ConsoleConsumer(process.getInputStream(), stdout)).start();
        }
        // Running maven in a SM is unlikely, but we'll be safe
        shutdownHook = AccessController.doPrivileged(new PrivilegedAction<Thread>() {
            @Override
            public Thread run() {
                return ProcessHelper.addShutdownHook(process);
            }
        });
        if (waitForStart(timeout)) {
            timerService.scheduleWithFixedDelay(new Reaper(), 20, 10, TimeUnit.SECONDS);
        } else {
            try {
                ProcessHelper.destroyProcess(process);
            } catch (InterruptedException ignore) {
            }
            throw new IllegalStateException(String.format("Managed server was not started within [%d] s", timeout));
        }
    }

    /**
     * Stops the server.
     */
    public final synchronized void stop() {
        try {
            // Remove the shutdown hook. Running maven in a SM is unlikely, but we'll be safe
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        Runtime.getRuntime().removeShutdownHook(shutdownHook);
                    } catch (Exception ignore) {
                    }
                    return null;
                }
            });
            // Shutdown the reaper
            try {
                timerService.shutdown();
            } catch (Exception ignore) {
            }
            // Stop the servers
            stopServer();
        } finally {
            try {
                ProcessHelper.destroyProcess(process);
            } catch (InterruptedException ignore) {
                // no-op
            }
        }
    }

    /**
     * Stops the server before the process is destroyed. A no-op override will just destroy the process.
     */
    protected abstract void stopServer();

    protected abstract boolean waitForStart(long timeout) throws IOException, InterruptedException;

    /**
     * Checks the status of the server and returns {@code true} if the server is fully started.
     *
     * @return {@code true} if the server is fully started, otherwise {@code false}
     */
    public abstract boolean isRunning();

    /**
     * Checks whether the server is running or not. If the server is no longer running the {@link #isRunning()} should
     * return {@code false}.
     */
    protected abstract void checkServerState();

    private class Reaper implements Runnable {

        @Override
        public void run() {
            checkServerState();
            if (!isRunning()) {
                stop();
            }
        }
    }

    static class ConsoleConsumer implements Runnable {
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