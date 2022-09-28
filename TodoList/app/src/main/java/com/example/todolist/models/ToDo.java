package com.example.todolist.models;

public class ToDo {
    private String key;
    private String title;
    private String description;
    private String user;
    private String fileURL;
    private String fileName;

    public ToDo() {
    }

    public ToDo(String title, String description, String user) {
        this.title = title;
        this.description = description;
        this.user = user;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ToDo(String key, String title, String description, String user) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.user = user;
    }

    public ToDo(String key, String title, String description, String user, String fileURL, String fileName) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.user = user;
        this.fileURL = fileURL;
        this.fileName = fileName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
