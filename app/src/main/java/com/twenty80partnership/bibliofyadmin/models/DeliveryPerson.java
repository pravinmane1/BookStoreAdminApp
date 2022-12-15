package com.twenty80partnership.bibliofyadmin.models;

public class DeliveryPerson {
    String name,email,contact,uid;

    public DeliveryPerson(String name, String email, String contact, String uid) {
        this.name = name;
        this.email = email;
        this.contact = contact;
        this.uid = uid;
    }

    public DeliveryPerson() {
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
