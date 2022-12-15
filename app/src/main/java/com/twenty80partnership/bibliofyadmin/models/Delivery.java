package com.twenty80partnership.bibliofyadmin.models;

public class Delivery {
    Integer daysForDelivery,deliveryCharges;
    String pin;

    public Delivery(){

    }

    public Delivery(String pin, Integer daysForDelivery, Integer deliveryCharges) {
        this.pin = pin;
        this.daysForDelivery = daysForDelivery;
        this.deliveryCharges = deliveryCharges;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Integer getDaysForDelivery() {
        return daysForDelivery;
    }

    public void setDaysForDelivery(Integer daysForDelivery) {
        this.daysForDelivery = daysForDelivery;
    }

    public Integer getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(Integer deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }
}
