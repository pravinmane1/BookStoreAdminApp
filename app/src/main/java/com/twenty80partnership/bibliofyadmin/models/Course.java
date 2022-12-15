package com.twenty80partnership.bibliofyadmin.models;

import java.util.HashMap;
import java.util.Map;

public class Course {
    String id,name,pic,currentSem;
    Integer year;
    Float priority;
    HashMap<String,Object> branch;

    public HashMap<String, Object> getBranch() {
        return branch;
    }

    public void setBranch(HashMap<String, Object> branch) {
        this.branch = branch;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer years) {
        this.year = years;
    }

    public Course() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Float getPriority() {
        return priority;
    }

    public void setPriority(Float priority) {
        this.priority = priority;
    }

    public String getCurrentSem() {
        return currentSem;
    }

    public void setCurrentSem(String currentSem) {
        this.currentSem = currentSem;
    }

    public Map<String,Object> toMap(){

        HashMap<String,Object> result = new HashMap<>();
        result.put("name",name);
        result.put("pic",pic);
        result.put("id",id);
        result.put("priority",priority);
        return result;
    }
}
