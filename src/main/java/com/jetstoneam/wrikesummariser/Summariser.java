package com.jetstoneam.wrikesummariser;

import com.jetstoneam.wrikesummariser.auth.UserCodeServer;
import com.jetstoneam.wrikesummariser.auth.WrikeAuth;
import com.jetstoneam.wrikesummariser.queries.SimpleQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

/**
 * Connects to Wrike and generates a summary of tasks
 */
public class Summariser {
    private static final Logger logger = LoggerFactory.getLogger(Summariser.class);
    private static final String VERSION = "0.1";

    public static void main(String[] args) throws Exception {
        setupJavaUtilLogging();

        logger.info("Wrike Summariser version: " + VERSION);

        // Setup a simple local server to retrieve the code when the user authenticates d
        try (UserCodeServer userCodeServer = new UserCodeServer(8585)) {
            // Handle the OAuth2 dance
            WrikeAuth auth = new WrikeAuth(userCodeServer);

            // Run queries
            SimpleQueries simpleQueries = new SimpleQueries(auth);
            simpleQueries.getCurrentUser();
        }
    }

    /**
     * Redirects the output of standard java loggers to our slf4j handler.
     */
    private static void setupJavaUtilLogging() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
}
