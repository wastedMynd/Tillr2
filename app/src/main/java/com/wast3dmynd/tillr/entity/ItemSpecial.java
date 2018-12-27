package com.wast3dmynd.tillr.entity;

import com.google.gson.Gson;

import java.util.Date;

public class ItemSpecial {

    //region attributes
    int id = 0;
    int itemId = 0;
    private double specialPrice = 0;
    private long specialStartDate = System.currentTimeMillis();
    private long specialEndDate = System.currentTimeMillis();
    private long specialStartTime = System.currentTimeMillis();
    private long specialEndTime = System.currentTimeMillis();
    private long timestamp = System.currentTimeMillis();
    //endregion

    //region getters and setters


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public double getSpecialPrice() {
        return specialPrice;
    }

    public void setSpecialPrice(double specialPrice) {
        this.specialPrice = specialPrice;
    }

    public long getSpecialStartDate() {
        return specialStartDate;
    }

    public void setSpecialStartDate(long specialStartDate) {
        this.specialStartDate = specialStartDate;
    }

    public long getSpecialEndDate() {
        return specialEndDate;
    }

    public void setSpecialEndDate(long specialEndDate) {
        this.specialEndDate = specialEndDate;
    }

    public long getSpecialStartTime() {
        return specialStartTime;
    }

    public void setSpecialStartTime(long specialStartTime) {
        this.specialStartTime = specialStartTime;
    }

    public long getSpecialEndTime() {
        return specialEndTime;
    }

    public void setSpecialEndTime(long specialEndTime) {
        this.specialEndTime = specialEndTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    //endregion

    public boolean isSpecialValid() {

        //region startDate logic
        Date currentDate = new Date(System.currentTimeMillis()), startDate = new Date(getSpecialStartDate());
        boolean isSpecialDateRangeValid = startDate.after(currentDate) || startDate.equals(currentDate);
        if (!isSpecialDateRangeValid) return false;
        //endregion

        //region endDate logic
        Date endDate = new Date(getSpecialEndDate());
        isSpecialDateRangeValid = endDate.after(startDate) || endDate.equals(startDate);
        if (!isSpecialDateRangeValid) return false;
        //endregion

        //region startTime logic
        if (getSpecialStartTime() == 0) return false;
        //endregion

        //region endTime logic
        boolean isSpecialTimeRangeValid = getSpecialEndTime() >= getSpecialStartTime();
        if (getSpecialEndTime() == 0) return false;
        else return isSpecialTimeRangeValid;
        //endregion
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static ItemSpecial fromJson(String itemStr) {
        Gson gson = new Gson();
        return gson.fromJson(itemStr, ItemSpecial.class);
    }

    @Override
    public boolean equals(Object obj) {
        ItemSpecial objSpecial = (ItemSpecial) obj;
        return ((getId() == objSpecial.getId()) && (getItemId() == objSpecial.getItemId()));
    }
}
