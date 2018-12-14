package com.wast3dmynd.tillr.entity;

import com.google.gson.Gson;

import java.io.Serializable;

public class Item implements Serializable {

    private int id =0;
    private int orderId =0;
    private String itemName, barcode;
    private double itemCostPerUnit =0;
    private int itemUnits = 0;
    private int itemDamage = 0;
    private double itemPriceTotal =0;
    private int itemUnitRemaining=0;
    private long itemTimeStamp= System.currentTimeMillis();
    private ItemGui gui = new ItemGui();

    //region getters and setters

    public long getItemTimeStamp() {
        return itemTimeStamp;
    }

    public void setItemTimeStamp(long itemScannedTime) {
        this.itemTimeStamp = itemScannedTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getItemCostPerUnit() {
        return itemCostPerUnit;
    }

    public void setItemCostPerUnit(double itemCostPerUnit) {
        this.itemCostPerUnit = itemCostPerUnit;
    }

    public int getItemUnits() {
        return itemUnits;
    }

    public void setItemUnits(int itemUnits) {
        this.itemUnits = itemUnits;
    }

    public double getItemPriceTotal() {
         itemPriceTotal = getItemUnits() * getItemCostPerUnit();
         return itemPriceTotal;
    }

    public void setItemPriceTotal(double itemPriceTotal) {
        this.itemPriceTotal = itemPriceTotal;
    }

    public int getItemUnitRemaining() {
        return itemUnitRemaining;
    }

    public void setItemUnitRemaining(int itemUnitRemaining) {
        this.itemUnitRemaining = itemUnitRemaining;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getItemDamage() {
        return itemDamage;
    }

    public void setItemDamage(int itemDamage) {
        this.itemDamage = itemDamage;
    }

    public ItemGui getGui() {
        return gui;
    }

    public void setGui(ItemGui gui) {
        this.gui = gui;
    }

    public boolean isValid() {
        if (getItemName() == null) return false;
        return  (!getItemName().isEmpty()) && (getItemCostPerUnit()> 0);
    }
    //endregion

    public static class ItemGui implements Serializable{
        private  boolean highlighted =false;
        private boolean selected = false;

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public MenuItemMode menuItemMode = MenuItemMode.INCREMENT;

        public MenuItemMode getMenuItemMode() {
            return menuItemMode;
        }

        public void setMenuItemMode(MenuItemMode menuItemMode) {
            this.menuItemMode = menuItemMode;
        }

        public enum MenuItemMode{
            INCREMENT,DECREMENT
        }

        public boolean isHighlighted() {
            return highlighted;
        }

        public void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
        }

    }


    @Override
    public boolean equals(Object obj) {
        return this.getId() == ((Item) obj).getId();
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Item fromJson(String itemStr) {
        Gson gson = new Gson();
        return gson.fromJson(itemStr, Item.class);
    }

}
