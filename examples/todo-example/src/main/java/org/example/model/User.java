package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonProperty("sub")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("email")
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
