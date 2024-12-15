package com.example.firebasetodo;

public class Task {
    String title;
    String description;
    boolean isCompleted;
    String createdAt;
    String key;

    public Task() {
    }

    public Task(String title, String description, boolean isCompleted, String createdAt) {
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    public Task(String title, String description, String createdAt) {
        this.title = title;
        this.description = description;
        this.isCompleted = false;
        this.createdAt = createdAt;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getKey() {
        return key;
    }
}
