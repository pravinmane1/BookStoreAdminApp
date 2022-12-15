package com.twenty80partnership.bibliofyadmin.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Book implements Serializable {
    private String name,searchName,author,publication,img,id,code,type  ;
    private Integer originalPrice,discountedPrice,discount,count;
    private Boolean availability,visibility;

    public Book() {
    }

    public Book(String name, String searchName, String author, String publication,
                String img, String id, String code, Integer originalPrice,
                Integer discountedPrice, Integer discount, Boolean availability,Boolean visibility,String type) {
        this.name = name;
        this.searchName = searchName;
        this.author = author;
        this.publication = publication;
        this.img = img;
        this.id = id;
        this.code = code;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.discount = discount;
        this.availability = availability;
        this.visibility = visibility;
        this.type = type;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Map<String,Object> toMap(){

        HashMap<String,Object> result = new HashMap<>();
        result.put("name",name);
        result.put("searchName",searchName);
        result.put("author",author);
        result.put("publication",publication);
        result.put("img",img);
        result.put("id",id);
        result.put("code",code);
        result.put("originalPrice",originalPrice);
        result.put("discountedPrice",discountedPrice);
        result.put("discount",discount);
        result.put("availability",availability);
        result.put("visibility",visibility);
        result.put("type",type);
        return result;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setOriginalPrice(Integer originalPrice) {
        this.originalPrice = originalPrice;
    }

    public void setDiscountedPrice(Integer discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public String getPublication() {
        return publication;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public Integer getOriginalPrice() {
        return originalPrice;
    }

    public Integer getDiscountedPrice() {
        return discountedPrice;
    }

    public Integer getDiscount() {
        return discount;
    }

    public Boolean getAvailability() {
        return availability;
    }
}
