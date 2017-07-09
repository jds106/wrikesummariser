package com.jetstoneam.wrikesummariser.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Handles the OAuth2 dance with Wrike
 */
public class WrikeAuth {
    private static final Logger logger = LoggerFactory.getLogger(WrikeAuth.class);

    private final String id = "";
    private final String secret = "";

    private final String refreshTokenPath = "./refreshtoken.txt";
    private final ObjectMapper mapper = new ObjectMapper();

    private final UserCodeServer userCodeServer;

    private AccessTokenResponse currentAccessToken = null;

    /** Initialise the authenticator */
    public WrikeAuth(UserCodeServer userCodeServer) {
        this.userCodeServer = userCodeServer;
    }

    /** Gets the current access token */
    public AccessTokenResponse getAccessToken() throws IOException {
        if (currentAccessToken == null) {
            File refreshTokenFile = new File(refreshTokenPath);

            if (refreshTokenFile.exists()) {
                currentAccessToken = mapper.readValue(refreshTokenFile, AccessTokenResponse.class);
            } else {
                currentAccessToken = requestFromUser();
            }
        }

        return currentAccessToken;
    }

    /** Refreshes the access token with the current refresh token */
    public AccessTokenResponse refreshAccessToken(AccessTokenResponse token) throws IOException {
        currentAccessToken = refreshAccessToken(token.RefreshToken, "refresh_token", token.Host);
        return currentAccessToken;
    }

    /** When no refresh token is available, request a code from the user */
    private AccessTokenResponse requestFromUser() throws IOException {
        logger.info(
                "User needs to authenticate - please visit: "
                        + "https://www.wrike.com/oauth2/authorize?client_id=" + id + "&response_type=code");

        try {
            String userCode = userCodeServer.waitForUserCode();
            logger.info("Had user code: " + userCode);

            return refreshAccessToken(userCode, "authorization_code", "www.wrike.com");
        }
        catch (InterruptedException ie) {
            throw new IOException("Interrupted while waiting for user code", ie);
        }
    }

    /** Make a token request from Wrike */
    private AccessTokenResponse refreshAccessToken(String refreshCode, String grantType, String host) throws IOException {
        String urlParams =
                "client_id=" + id
                        + "&client_secret=" + secret
                        + "&grant_type=" + grantType
                        + "&code=" + refreshCode;

        byte[] postData = urlParams.getBytes(StandardCharsets.UTF_8);
        URL url = new URL("https://" + host + "/oauth2/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.getOutputStream().write(postData);

        if (conn.getResponseCode() == 200) {
            try (InputStream is = conn.getInputStream()) {
                AccessTokenResponse response = mapper.readValue(is, AccessTokenResponse.class);
                mapper.writeValue(new File(refreshTokenPath), response);
                logger.info("Had response: " + mapper.writeValueAsString(response));
                return response;
            }
        } else {
            try (InputStream is = conn.getErrorStream()) {
                AccessTokenErrorResponse response = mapper.readValue(is, AccessTokenErrorResponse.class);
                throw new IOException("Failed to get refresh token. Error: " + response.Error + ": " + response.ErrorDescription);
            }
        }
    }
}
