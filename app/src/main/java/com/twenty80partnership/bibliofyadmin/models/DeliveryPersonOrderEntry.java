package com.twenty80partnership.bibliofyadmin.models;

public class DeliveryPersonOrderEntry {
    String orderId,place;

    public DeliveryPersonOrderEntry() {
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
