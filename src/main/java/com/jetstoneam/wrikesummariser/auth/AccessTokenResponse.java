package com.jetstoneam.wrikesummariser.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/** The message returned from Wrike on a successful login */
public class AccessTokenResponse {
    @JsonProperty("access_token")
    public String AccessToken;

    @JsonProperty("refresh_token")
    public String RefreshToken;

    @JsonProperty("token_type")
    public String TokenType;

    @JsonProperty("expires_in")
    public String ExpiresIn;

    @JsonProperty("host")
    public String Host;
}
