package com.twenty80partnership.bibliofyadmin.models;

public class CartItem {
    private String itemName,itemPublication,itemAuthor,itemId,itemImg,itemLocation;
    private Integer itemOriginalPrice,itemDiscountedPrice,itemDiscount,quantity;
    private Long timeAdded;


    public CartItem() {
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public void setItemLocation(String bookLocation) {
        this.itemLocation = bookLocation;
    }

    public Long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Long timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getItemImg() {
        return itemImg;
    }

    public void setItemImg(String itemImg) {
        this.itemImg = itemImg;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemPublication(String itemPublication) {
        this.itemPublication = itemPublication;
    }

    public void setItemAuthor(String itemAuthor) {
        this.itemAuthor = itemAuthor;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setItemOriginalPrice(Integer itemOriginalPrice) {
        this.itemOriginalPrice = itemOriginalPrice;
    }

    public void setItemDiscountedPrice(Integer itemDiscountedPrice) {
        this.itemDiscountedPrice = itemDiscountedPrice;
    }

    public void setItemDiscount(Integer itemDiscount) {
        this.itemDiscount = itemDiscount;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemPublication() {
        return itemPublication;
    }

    public String getItemAuthor() {
        return itemAuthor;
    }

    public String getItemId() {
        return itemId;
    }

    public Integer getItemOriginalPrice() {
        return itemOriginalPrice;
    }

    public Integer getItemDiscountedPrice() {
        return itemDiscountedPrice;
    }

    public Integer getItemDiscount() {
        return itemDiscount;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
