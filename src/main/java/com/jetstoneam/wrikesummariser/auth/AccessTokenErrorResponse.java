package com.jetstoneam.wrikesummariser.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/** The error message returned from Wrike on a failed login */
public class AccessTokenErrorResponse {
    @JsonProperty("error")
    public String Error;

    @JsonProperty("error_description")
    public String ErrorDescription;
}
