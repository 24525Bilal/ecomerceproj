package com.homeelectronics.model;

public class User {
    private String email;
    private String password;

    // constructor
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}