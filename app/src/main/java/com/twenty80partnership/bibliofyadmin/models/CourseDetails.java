package com.twenty80partnership.bibliofyadmin.models;

import java.util.HashMap;
import java.util.Map;

public class CourseDetails {
    String id,name,pic,currentSem;
    Float priority;
    Boolean year1common;
    Boolean publicationSystem;

    public Boolean getPublicationSystem() {
        return publicationSystem;
    }

    public void setPublicationSystem(Boolean publicationSystem) {
        this.publicationSystem = publicationSystem;
    }

    public Boolean getYear1common() {
        return year1common;
    }

    public void setYear1common(Boolean year1common) {
        this.year1common = year1common;
    }

    public String getCurrentSem() {
        return currentSem;
    }

    public void setCurrentSem(String currentSem) {
        this.currentSem = currentSem;
    }

    public CourseDetails() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public Float getPriority() {
        return priority;
    }

    public void setPriority(Float priority) {
        this.priority = priority;
    }

    public Map<String,Object> toMap(){

        HashMap<String,Object> result = new HashMap<>();
        result.put("name",name);
        result.put("pic",pic);
        result.put("id",id);
        result.put("currentSem",currentSem);
        result.put("priority",priority);
        return result;
    }
}
