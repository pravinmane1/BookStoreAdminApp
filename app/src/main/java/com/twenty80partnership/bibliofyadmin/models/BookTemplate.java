package com.twenty80partnership.bibliofyadmin.models;

public class BookTemplate {
    private String defaultBookId,name,templateId,defaultPublication;
    private Float priority;

    public BookTemplate() {
    }

    public String getDefaultBookId() {
        return defaultBookId;
    }

    public void setDefaultBookId(String defaultBookId) {
        this.defaultBookId = defaultBookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getDefaultPublication() {
        return defaultPublication;
    }

    public void setDefaultPublication(String defaultPublication) {
        this.defaultPublication = defaultPublication;
    }

    public Float getPriority() {
        return priority;
    }

    public void setPriority(Float priority) {
        this.priority = priority;
    }
}
