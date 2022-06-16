package com.ytu.businesstravelapp.Classes;

public class Taxi {
    String type, startPrice, kmPrice, minPrice;

    public Taxi(String type, String startPrice, String kmPrice, String minPrice) {
        this.type = type;
        this.startPrice = startPrice;
        this.kmPrice = kmPrice;
        this.minPrice = minPrice;
    }

    public String getType() {
        return type;
    }

    public String getStartPrice() {
        return startPrice;
    }

    public String getKmPrice() {
        return kmPrice;
    }

    public String getMinPrice() {
        return minPrice;
    }
}
