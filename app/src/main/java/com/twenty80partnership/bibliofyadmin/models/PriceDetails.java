package com.twenty80partnership.bibliofyadmin.models;

import java.io.Serializable;

public class PriceDetails implements Serializable {
    Integer amountDiscounted,count,amountOriginal,deliveryCharges;
    String method,targetUpi,tsnId,upiStatus;

    public PriceDetails(Integer amountDiscounted, Integer count, Integer amountOriginal, String method) {
        this.amountDiscounted = amountDiscounted;
        this.count = count;
        this.amountOriginal = amountOriginal;
        this.method = method;
    }

    public Integer getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(Integer deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }

    public String getTargetUpi() {
        return targetUpi;
    }

    public void setTargetUpi(String targetUpi) {
        this.targetUpi = targetUpi;
    }

    public String getTsnId() {
        return tsnId;
    }

    public void setTsnId(String tsnId) {
        this.tsnId = tsnId;
    }

    public String getUpiStatus() {
        return upiStatus;
    }

    public void setUpiStatus(String upiStatus) {
        this.upiStatus = upiStatus;
    }

    public PriceDetails() {
    }

    public Integer getAmountDiscounted() {
        return amountDiscounted;
    }

    public void setAmountDiscounted(Integer amountDiscounted) {
        this.amountDiscounted = amountDiscounted;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getAmountOriginal() {
        return amountOriginal;
    }

    public void setAmountOriginal(Integer amountOriginal) {
        this.amountOriginal = amountOriginal;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
