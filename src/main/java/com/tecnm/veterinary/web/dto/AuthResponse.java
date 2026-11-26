package com.tecnm.veterinary.web.dto;

public class AuthResponse {

    private String token;
    private String type;
    private String clientId;

    public AuthResponse(String token, String clientId) {
        this.token = token;
        this.type = "Bearer";
        this.clientId = clientId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
