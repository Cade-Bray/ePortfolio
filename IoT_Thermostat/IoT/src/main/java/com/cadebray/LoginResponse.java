package com.cadebray;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is a small helper class for managing login response objects.
 */
public class LoginResponse {
    @JsonProperty("token")
    private String token;

    @JsonProperty("device")
    private String device;

    /**
     * This is the constructor initialized values are injected by fasterxml.jackson
     */
    public LoginResponse(){}

    public String getToken(){
        return token;
    }

    public String getDevice(){
        return device;
    }
}
