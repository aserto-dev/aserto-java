package org.example.model;

public class User {
    private String key;
    private String name;
    private String email;
    private String picture;

    public User() {
    }

    public User(String key, String name, String email, String picture) {
        this.key = key;
        this.name = name;
        this.email = email;
        this.picture = picture;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
