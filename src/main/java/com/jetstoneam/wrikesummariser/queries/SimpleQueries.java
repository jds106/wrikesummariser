package com.jetstoneam.wrikesummariser.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetstoneam.wrikesummariser.auth.AccessTokenResponse;
import com.jetstoneam.wrikesummariser.auth.WrikeAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple set of queries
 *
 * See here for more details:
 *      https://developers.wrike.com/documentation/api/methods/query-tasks
 */
public class SimpleQueries {
    private static final Logger logger = LoggerFactory.getLogger(SimpleQueries.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final WrikeAuth auth;

    /** Initialise with the authenticator */
    public SimpleQueries(WrikeAuth auth) {
        this.auth = auth;
    }

    public void getCurrentUser() throws IOException {
        String response = sendMessage("tasks?descendants=true&subTasks=true&fields=[\"responsibleIds\"]");
        logger.info("Current tasks: " + response);

//        String response = sendMessage("contacts");
//        logger.info("User: " + response);

//        String response = sendMessage("customfields");
//        logger.info("Custom fields: " + response);

    }

    private String readStream(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private String sendMessage(String cmd) throws IOException {
        AccessTokenResponse accessToken = auth.getAccessToken();
        String urlString = "https://" + accessToken.Host + "/api/v3/" + cmd;

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "bearer " + accessToken.AccessToken);

        logger.info("Sending query: " + urlString);
        if (conn.getResponseCode() == 200) {
            try (InputStream is = conn.getInputStream()) {
                return readStream(is);
            }
        } else {
            try (InputStream is = conn.getErrorStream()) {
                String error = readStream(is);
                logger.error("Received error: " + error);
                return error;
            }
        }
    }

//    private<T> T sendMessage(AccessTokenResponse accessToken, T type) {
//
//    }
}
