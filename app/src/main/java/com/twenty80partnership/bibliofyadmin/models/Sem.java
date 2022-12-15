package com.twenty80partnership.bibliofyadmin.models;

public class Sem {
    private String sem,code;

    public Sem(String sem, String code) {
        this.sem = sem;
        this.code = code;
    }

    public String getSem() {
        return sem;
    }

    public String getCode() {
        return code;
    }

    public Sem() {
    }
}
