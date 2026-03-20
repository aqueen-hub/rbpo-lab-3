package com.example.demo2.dto;

public class RegisterResponse {
    private String message;
    private String username;

    public RegisterResponse(String message, String username) {
        this.message = message;
        this.username = username;
    }

    // геттеры и сеттеры
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}