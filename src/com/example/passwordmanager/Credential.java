package com.example.passwordmanager;

public class Credential {
    public final String service;
    public final String username;
    public final String password;

    public Credential(String service, String username, String password) {
        this.service = service;
        this.username = username;
        this.password = password;
    }
}