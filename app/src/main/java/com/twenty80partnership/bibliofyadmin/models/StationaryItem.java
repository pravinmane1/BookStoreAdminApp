package com.twenty80partnership.bibliofyadmin.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StationaryItem implements Serializable {
    private String name,img,atr1,atr2,atr3,atr4,id,searchName,quantityFlag,itemLocation;
    private Integer discount,discountedPrice,originalPrice,quantity,count;
    private Boolean availability;

    public StationaryItem(String name,
                          String img,
                          String atr1,
                          String atr2,
                          String atr3,
                          String atr4,
                          String id,
                          String searchName,
                          Integer discount,
                          Integer discountedPrice,
                          Integer originalPrice,
                          Boolean availability) {
        this.name = name;
        this.img = img;
        this.atr1 = atr1;
        this.atr2 = atr2;
        this.atr3 = atr3;
        this.atr4 = atr4;
        this.id = id;
        this.searchName = searchName;
        this.discount = discount;
        this.discountedPrice = discountedPrice;
        this.originalPrice = originalPrice;
        this.availability = availability;
    }

    public Map<String,Object> toMap(){
        Map<String,Object> result = new HashMap<>();
        result.put("name",name);
        result.put("img",img);
        result.put("atr1",atr1);
        result.put("atr2",atr2);
        result.put("atr3",atr3);
        result.put("atr4",atr4);
        result.put("id",id);
        result.put("searchName",searchName);
        result.put("discount",discount);
        result.put("discountedPrice",discountedPrice);
        result.put("originalPrice",originalPrice);
        result.put("availability",availability);

        return result;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getQuantityFlag() {
        return quantityFlag;
    }

    public void setQuantityFlag(String quantityFlag) {
        this.quantityFlag = quantityFlag;
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public void setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public StationaryItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    public String getAtr1() {
        return atr1;
    }

    public void setAtr1(String atr1) {
        this.atr1 = atr1;
    }

    public String getAtr2() {
        return atr2;
    }

    public void setAtr2(String atr2) {
        this.atr2 = atr2;
    }

    public String getAtr3() {
        return atr3;
    }

    public void setAtr3(String atr3) {
        this.atr3 = atr3;
    }

    public String getAtr4() {
        return atr4;
    }

    public void setAtr4(String atr4) {
        this.atr4 = atr4;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Integer getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(Integer discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public Integer getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Integer originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
