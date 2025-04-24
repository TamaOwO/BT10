package com.example.bt10.model;

public class UserModel {
    private String email;
    private String profileImage;
    private int videoCount;

    public UserModel(String email, String profileImage, int videoCount) {
        this.email = email;
        this.profileImage = profileImage;
        this.videoCount = videoCount;
    }

    public UserModel() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }
}
