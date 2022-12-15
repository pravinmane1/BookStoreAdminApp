package com.twenty80partnership.bibliofyadmin.models;

public class Pricing {
    Integer profit,buyingPrice,buyingDiscount;

    public Pricing() {
    }

    public Integer getProfit() {
        return profit;
    }

    public void setProfit(Integer profit) {
        this.profit = profit;
    }

    public Integer getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(Integer buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public Integer getBuyingDiscount() {
        return buyingDiscount;
    }

    public void setBuyingDiscount(Integer buyingDiscount) {
        this.buyingDiscount = buyingDiscount;
    }
}
