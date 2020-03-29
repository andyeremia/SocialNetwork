package com.example.socialnetwork;

public class Posts {
    private String userid, time, date, postimage, description, profileimage, fullname;

    public Posts(){}

    public Posts(String userid, String time, String date, String postimage, String description, String profileimage, String fullname) {
        this.userid = userid;
        this.time = time;
        this.date = date;
        this.postimage = postimage;
        this.description = description;
        this.profileimage = profileimage;
        this.fullname = fullname;
    }

    public String getUid() {
        return userid;
    }

    public void setUid(String userid) {
        this.userid = userid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostImage() {
        return postimage;
    }

    public void setPostImage(String postimage) {
        this.postimage = postimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImage() {
        return profileimage;
    }

    public void setProfileImage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullName() {
        return fullname;
    }

    public void setFullName(String fullname) {
        this.fullname = fullname;
    }
}
