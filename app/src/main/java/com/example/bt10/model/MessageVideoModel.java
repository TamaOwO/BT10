package com.example.bt10.model;

import java.util.List;

public class MessageVideoModel {
    private boolean success;
    private String message;
    private List<VideoModelThay> result; // Assuming VideoModel is another class representing video data>

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<VideoModelThay> getResult() {
        return result;
    }
}
