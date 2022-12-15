package com.twenty80partnership.bibliofyadmin.models;

public class Year {
    private String code,year;

    public Year(String code, String year) {
        this.code = code;
        this.year = year;
    }

    public String getCode() {
        return code;
    }

    public String getYear() {
        return year;
    }


    public Year() {
    }
}
