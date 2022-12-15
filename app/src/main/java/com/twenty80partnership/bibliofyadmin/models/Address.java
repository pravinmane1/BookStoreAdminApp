package com.twenty80partnership.bibliofyadmin.models;

import java.io.Serializable;

public class Address implements Serializable {
    private String buildingNameNumber,areaRoad,city,state,name,type,number,altNumber;
    private String pincode;
    private Long timeAdded;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Address() {
    }

    public Long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Long timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAltNumber() {
        return altNumber;
    }

    public void setAltNumber(String altNumber) {
        this.altNumber = altNumber;
    }

    public String getBuildingNameNumber() {
        return buildingNameNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBuildingNameNumber(String buildingNameNumber) {
        this.buildingNameNumber = buildingNameNumber;
    }

    public String getAreaRoad() {
        return areaRoad;
    }

    public void setAreaRoad(String areaRoad) {
        this.areaRoad = areaRoad;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

}
