package com.jetstoneam.wrikesummariser.auth;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;

/**
 * If the user needs to authenticate, then the response can hit this server which provides
 * the code needed to generate a refresh & access token.
 */
public class UserCodeServer implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(UserCodeServer.class);

    private final CodeHandler codeHandler = new CodeHandler();
    private final Server server;

    private class CodeHandler extends AbstractHandler
    {
        private final Object lockObject = new Object();
        private String code;

        public String waitForUserCode() throws InterruptedException {
            synchronized (lockObject) {
                lockObject.wait();
                return this.code;
            }
        }

        public void handle(
                String target, Request baseRequest, HttpServletRequest request,
                HttpServletResponse response) throws IOException, ServletException
        {
            String queryString = request.getQueryString();

            for (String token : queryString.split("&")) {
                if (token.startsWith("code=")) {
                    this.code = token.substring("code=".length());
                    synchronized (lockObject) {
                        this.lockObject.notify();
                    }
                }
            }

            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            response.getWriter().println("Wrike authenticated successfully");
        }
    }

    public UserCodeServer(int port) throws Exception {
        server = new Server(port);
        server.setHandler(codeHandler);
        server.start();

        logger.info("User-code server configured on port: " + port);
    }

    public void close() {
        try {
            server.stop();
            server.join();
        }
        catch (Exception e) {
            logger.error("Could not shut down server", e);
        }
    }

    public String waitForUserCode() throws InterruptedException {
        logger.info("Blocking for server code...");
        return codeHandler.waitForUserCode();
    }
}
