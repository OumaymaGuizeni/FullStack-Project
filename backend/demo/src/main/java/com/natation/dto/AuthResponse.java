package com.natation.dto;

public class AuthResponse {
    private String token;
    private String email;
    private String username;
    private String role;
    private String message;
    private boolean mfaRequired;

    public AuthResponse() {}

    public AuthResponse(String token, String email, String username, String role) {
        this.token = token;
        this.email = email;
        this.username = username;
        this.role = role;
        this.mfaRequired = false;
    }

    public static AuthResponse mfaRequired(String email) {
        AuthResponse res = new AuthResponse();
        res.email = email;
        res.mfaRequired = true;
        res.message = "Passcode sent to email";
        return res;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isMfaRequired() { return mfaRequired; }
    public void setMfaRequired(boolean mfaRequired) { this.mfaRequired = mfaRequired; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
