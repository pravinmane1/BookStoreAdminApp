package com.twenty80partnership.bibliofyadmin.models;

public class Item {
String name,pic,id,code,topic,currentSem,searchName;
Long timeAdded;
Float priority;



    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Item(String name, String pic, String id, Long timeAdded, Float priority,String currentSem) {
        this.name = name;
        this.pic = pic;
        this.id = id;
        this.timeAdded = timeAdded;
        this.priority = priority;
        this.currentSem = currentSem;
    }

    public Item(String name, String id, Long timeAdded, Float priority,String currentSem) {
        this.name = name;
        this.id = id;
        this.timeAdded = timeAdded;
        this.priority = priority;
        this.currentSem = currentSem;
    }


    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getCurrentSem() {
        return currentSem;
    }

    public void setCurrentSem(String currentSem) {
        this.currentSem = currentSem;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public Long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Long timeAdded) {
        this.timeAdded = timeAdded;
    }

    public Float getPriority() {
        return priority;
    }

    public void setPriority(Float priority) {
        this.priority = priority;
    }

    public Item() {
    }


}
