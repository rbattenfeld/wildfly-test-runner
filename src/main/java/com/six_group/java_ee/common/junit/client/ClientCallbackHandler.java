package com.six_group.java_ee.common.junit.client;

import java.io.Console;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;

import org.apache.maven.plugin.logging.Log;


/**
 * A CallbackHandler implementation to supply the username and password if required when
 * connecting to the server - if these are not available the user will be prompted to
 * supply them.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
class ClientCallbackHandler implements CallbackHandler {

    private final Console console;
    private final Log log;
    private boolean promptShown = false;
    private String username;
    private char[] password;

    ClientCallbackHandler(final String username, final String password, final Log log) {
        this.log = log;
        console = System.console();
        this.username = username;
        if (password != null) {
            this.password = password.toCharArray();
        }
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        // Special case for anonymous authentication to avoid prompting user for their name.
        if (callbacks.length == 1 && callbacks[0] instanceof NameCallback) {
            ((NameCallback) callbacks[0]).setName("anonymous demo user");
            return;
        }

        for (Callback current : callbacks) {
            if (current instanceof RealmCallback) {
                final RealmCallback rcb = (RealmCallback) current;
                final String defaultText = rcb.getDefaultText();
                rcb.setText(defaultText); // For now just use the realm suggested.

                prompt(defaultText);
            } else if (current instanceof RealmChoiceCallback) {
                throw new UnsupportedCallbackException(current, "Realm choice not currently supported.");
            } else if (current instanceof NameCallback) {
                final NameCallback ncb = (NameCallback) current;
                final String userName = obtainUsername("Username:");

                ncb.setName(userName);
            } else if (current instanceof PasswordCallback) {
                PasswordCallback pcb = (PasswordCallback) current;
                char[] password = obtainPassword("Password:");

                pcb.setPassword(password);
            } else {
                throw new UnsupportedCallbackException(current);
            }
        }
    }

    private void prompt(final String realm) {
        if (!promptShown) {
            promptShown = true;
            log.info("Authenticating against security realm: " + realm);
        }
    }

    private String obtainUsername(final String prompt) {
        if (username == null) {
            checkConsole();
            username = console.readLine(prompt);
        }
        return username;
    }

    private char[] obtainPassword(final String prompt) {
        if (password == null) {
            checkConsole();
            password = console.readPassword(prompt);
        }

        return password;
    }

    private void checkConsole() {
        if (console == null) {
            throw new IllegalStateException("The environment does not have a usable console. Cannot prompt for user name and password");
        }
    }

}