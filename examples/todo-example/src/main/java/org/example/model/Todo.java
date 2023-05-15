package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Todo {
    private String id;
    private String title;
    private String ownerID;
    private boolean completed;

    public Todo() {
    }

    public Todo(String id, String title, String ownerID, boolean completed) {
        this.id = id;
        this.title = title;
        this.ownerID = ownerID;
        this.completed = completed;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @JsonProperty("ID")
    public String getId() {
        return id;
    }

    @JsonProperty("Title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("OwnerID")
    public String getOwnerID() {
        return ownerID;
    }

    @JsonProperty("Completed")
    public boolean getCompleted() {
        return completed;
    }
}
