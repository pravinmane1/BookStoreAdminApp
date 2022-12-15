package com.twenty80partnership.bibliofyadmin.models;

public class ReturnableInfo {
    String description;
    Boolean returnable;
    Integer required;
    Integer cartNewBooksRequiredCount;

    public Integer getCartNewBooksRequiredCount() {
        return cartNewBooksRequiredCount;
    }

    public void setCartNewBooksRequiredCount(Integer cartNewBooksRequiredCount) {
        this.cartNewBooksRequiredCount = cartNewBooksRequiredCount;
    }

    public Integer getRequired() {
        return required;
    }

    public void setRequired(Integer required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getReturnable() {
        return returnable;
    }

    public void setReturnable(Boolean returnable) {
        this.returnable = returnable;
    }
}
