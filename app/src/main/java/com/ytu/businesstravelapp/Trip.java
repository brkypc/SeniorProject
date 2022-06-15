package com.ytu.businesstravelapp;

public class Trip {
    String id, date, tripTime, taxiType, distance, amount, nameSurname, billPrice, status;

    public Trip() { }

    public String getNameSurname() {
        return nameSurname;
    }

    public String getBillPrice() {
        return billPrice;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getTripTime() {
        return tripTime;
    }

    public String getTaxiType() {
        return taxiType;
    }

    public String getDistance() {
        return distance;
    }

    public String getAmount() {
        return amount;
    }

    public String getId() { return id; }

    public void setId(String id) {
        this.id = id;
    }
}
