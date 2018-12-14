package com.wast3dmynd.tillr.entity;

public class InventoryData {
    private int id = 0;
    private long stockUnitCount = 0;
    private double stockPriceTotal = 0;
    private long timestamp = 0;

    //region getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getStockUnitCount() {
        return stockUnitCount;
    }

    public void setStockUnitCount(long stockUnitCount) {
        this.stockUnitCount = stockUnitCount;
    }

    public double getStockPriceTotal() {
        return stockPriceTotal;
    }

    public void setStockPriceTotal(double stockPriceTotal) {
        this.stockPriceTotal = stockPriceTotal;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    //endregion

    public boolean isValid() {
        return getTimestamp() > 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this.getId() == ((InventoryData) obj).getId();
    }
}
