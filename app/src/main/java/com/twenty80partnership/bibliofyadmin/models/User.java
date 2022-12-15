package com.twenty80partnership.bibliofyadmin.models;

import java.io.Serializable;

public class User implements Serializable {

    String name,uId,phone,college,course,branchCode,courseCode,email,photo,yearCode;
    Long registerDate,lastLogin,lastOpened;
    boolean isPhoneVerified;
    Integer orders;

    public User() {
    }

    public User(String name, String uId, String phone,
                String college, String course, String branchCode,
                String courseCode, String email, String photo,
                String yearCode, Long registerDate, Long lastLogin,
                Long lastOpened, boolean isPhoneVerified, Integer orders) {
        this.name = name;
        this.uId = uId;
        this.phone = phone;
        this.college = college;
        this.course = course;
        this.branchCode = branchCode;
        this.courseCode = courseCode;
        this.email = email;
        this.photo = photo;
        this.yearCode = yearCode;
        this.registerDate = registerDate;
        this.lastLogin = lastLogin;
        this.lastOpened = lastOpened;
        this.isPhoneVerified = isPhoneVerified;
        this.orders = orders;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getYearCode() {
        return yearCode;
    }

    public void setYearCode(String yearCode) {
        this.yearCode = yearCode;
    }

    public Long getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Long registerDate) {
        this.registerDate = registerDate;
    }

    public Long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Long getLastOpened() {
        return lastOpened;
    }

    public void setLastOpened(Long lastOpened) {
        this.lastOpened = lastOpened;
    }

    public void setIsPhoneVerified(boolean isPhoneVerified) {
this.isPhoneVerified=isPhoneVerified;    }

    public boolean getIsPhoneVerified() {
        return isPhoneVerified ;
    }

    public Integer getOrders() {
        return orders;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }
}
