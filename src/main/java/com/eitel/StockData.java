package com.eitel;

import java.math.BigDecimal;

class StockData {
    private String date;
    private BigDecimal price;

    StockData(String date, BigDecimal price) {
        this.date = date;
        this.price = price;
    }

    StockData(String date, String price) {
        this.date = date;
        this.price = new BigDecimal(price);
    }

    String getDate() {
        return this.date;
    }

    BigDecimal getPrice() {
        return this.price;
    }
}
