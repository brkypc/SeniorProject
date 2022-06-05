package com.ytu.businesstravelapp;

public class Trip {
    String date, tripTime, taxiType, distance, amount;

    public Trip(String date, String tripTime, String taxiType, String distance, String amount) {
        this.date = date;
        this.tripTime = tripTime;
        this.taxiType = taxiType;
        this.distance = distance;
        this.amount = amount;
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
}
