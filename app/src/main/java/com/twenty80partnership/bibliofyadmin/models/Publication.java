package com.twenty80partnership.bibliofyadmin.models;

public class Publication {
    private String name,code;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Publication(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public Publication() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
