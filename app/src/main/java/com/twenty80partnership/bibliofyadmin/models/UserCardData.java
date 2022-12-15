package com.twenty80partnership.bibliofyadmin.models;

public class UserCardData {
    String photo,name,email,uId;

    public UserCardData(String photo, String name, String email, String uId) {
        this.photo = photo;
        this.name = name;
        this.email = email;
        this.uId = uId;
    }

    public UserCardData() {
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
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

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }
}
