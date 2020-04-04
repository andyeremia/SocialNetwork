package com.example.socialnetwork;

public class Comments {
    private String comment, commentDate, commentTime, username;

    public Comments(){}

    public Comments(String comment, String commentDate, String commentTime, String username) {
        this.comment = comment;
        this.commentDate = commentDate;
        this.commentTime = commentTime;
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
