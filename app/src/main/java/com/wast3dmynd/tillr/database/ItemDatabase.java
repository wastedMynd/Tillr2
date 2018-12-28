package com.wast3dmynd.tillr.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.wast3dmynd.tillr.database.utils.DatabaseDelegate;
import com.wast3dmynd.tillr.database.utils.DatabaseListener;
import com.wast3dmynd.tillr.entity.InventoryData;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.ItemSpecial;

import java.util.ArrayList;
import java.util.Date;

public class ItemDatabase extends DatabaseDelegate {


    //database table
    public static final String DATABASE_TABLE = "Items";

    // Columns
    public static final String ID_COLUMN = "id";
    private final String FOREIGN_KEY_COLUMN = "order_id";
    private final String ITEM_NAME_COLUMN = "item_name";
    private final String ITEM_BARCODE_COLUMN = "item_barcode";
    private final String ITEM_COST_PER_UNIT_COLUMN = "item_cost_per_unit";
    private final String ITEM_UNITS_COLUMN = "item_units";
    private final String ITEM_DAMAGE_COLUMN = "item_damage";
    private final String ITEM_TOTAL_COLUMN = "item_total";
    private final String ITEM_REMAINING_COLUMN = "item_remaining";
    private final String ITEM_TIMESTAMP_COLUMN = "item_timestamp";

    private ItemSpecialDatabase itemSpecialDatabase;

    public ItemDatabase(Context context) {
        super(context);
        itemSpecialDatabase = new ItemSpecialDatabase(context);
        setDatabaseListener(new DatabaseListener() {
            @Override
            public void onDatabaseItemRemoved(boolean isItemRemoved, Object objItem) {
                if (!isItemRemoved) return;
                Item item = (Item) objItem;
                itemSpecialDatabase.updateItem(item.getSpecial());
            }

            @Override
            public void onDatabaseItemInserted(boolean isItemInserted, Object objItem) {
                if (!isItemInserted) return;
                Item item = (Item) objItem;
                itemSpecialDatabase.addItem(item.getSpecial());
            }

            @Override
            public void onDatabaseItemUpdated(boolean isItemUpdated, Object objItem) {
                if (!isItemUpdated) return;
                Item item = (Item) objItem;
                itemSpecialDatabase.updateItem(item.getSpecial());
            }
        });
    }

    @Override
    protected String onCreateScheme() {

        return "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE +
                String.format("(%s INTEGER PRIMARY KEY  AUTOINCREMENT,", ID_COLUMN) +
                String.format("%s INTEGER,", FOREIGN_KEY_COLUMN) +
                String.format("%s TEXT NOT NULL,", ITEM_NAME_COLUMN) +
                String.format("%s TEXT,", ITEM_BARCODE_COLUMN) +
                String.format("%s DOUBLE,", ITEM_COST_PER_UNIT_COLUMN) +
                String.format("%s INTEGER,", ITEM_DAMAGE_COLUMN) +
                String.format("%s DOUBLE,", ITEM_TOTAL_COLUMN) +
                String.format("%s INTEGER,", ITEM_REMAINING_COLUMN) +
                String.format("%s INTEGER,", ITEM_UNITS_COLUMN) +
                String.format("%s LONG,", ITEM_TIMESTAMP_COLUMN) +
                String.format("FOREIGN KEY(%s) REFERENCES %s(%s));", FOREIGN_KEY_COLUMN, OrderDatabase.DATABASE_TABLE,
                        OrderDatabase.ORDER_ID_COLUMN);
    }

    @Override
    public boolean addItem(Object item) {
        boolean result;

        if (item == null) return false;

        if (checkExists(item)) {
            return updateItem(item);
        }

        if (!(item instanceof Item)) return false;

        Item itemDB = (Item) item;

        if (!itemDB.isValid()) return false;

        ContentValues values = new ContentValues();

        values.put(ITEM_NAME_COLUMN, itemDB.getItemName());

        values.put(ITEM_BARCODE_COLUMN, itemDB.getBarcode());

        values.put(ITEM_COST_PER_UNIT_COLUMN, itemDB.getItemCostPerUnit());

        values.put(ITEM_UNITS_COLUMN, itemDB.getItemUnits());

        values.put(ITEM_DAMAGE_COLUMN, itemDB.getItemDamage());

        values.put(ITEM_TOTAL_COLUMN, itemDB.getItemPriceTotal());

        values.put(ITEM_REMAINING_COLUMN, itemDB.getItemUnitRemaining());

        values.put(ITEM_TIMESTAMP_COLUMN, itemDB.getItemTimeStamp());

        float res;
        startAuditInventoryProcess(itemDB, AuditOnCondition.onInsert);
        openDatabase();
        res = getDatabase().insert(DATABASE_TABLE, null, values);
        closeDatabase();

        result = res > 0;

        itemDB.setId((int) res);

        if (getDatabaseListener() != null)
            getDatabaseListener().onDatabaseItemInserted(result, item);

        return result;
    }

    @Override
    public boolean updateItem(Object item) {
        boolean ret;

        if (!checkExists(item)) {
            return addItem(item);
        }

        if (!(item instanceof Item)) return false;


        Item itemDB = (Item) item;

        if (!itemDB.isValid()) return false;

        startAuditInventoryProcess(itemDB, AuditOnCondition.onUpdate);


        String whereClause = ID_COLUMN + "=?";

        String[] whereArgs = {String.valueOf(itemDB.getId())};

        ContentValues values = new ContentValues();

        values.put(ITEM_NAME_COLUMN, itemDB.getItemName());

        values.put(ITEM_BARCODE_COLUMN, itemDB.getBarcode());

        values.put(ITEM_COST_PER_UNIT_COLUMN, itemDB.getItemCostPerUnit());

        values.put(ITEM_UNITS_COLUMN, itemDB.getItemUnits());

        values.put(ITEM_DAMAGE_COLUMN, itemDB.getItemDamage());

        values.put(ITEM_REMAINING_COLUMN, itemDB.getItemUnitRemaining() - itemDB.getItemUnits());

        values.put(ITEM_TOTAL_COLUMN, itemDB.getItemPriceTotal());

        values.put(ITEM_TIMESTAMP_COLUMN, itemDB.getItemTimeStamp());

        openDatabase();
        itemDB.setId(getDatabase().update(DATABASE_TABLE, values, whereClause, whereArgs));
        closeDatabase();

        ret = itemDB.getId() > 0;
        if (getDatabaseListener() != null) getDatabaseListener().onDatabaseItemUpdated(ret, item);
        return ret;
    }

    @Override
    public boolean removeItem(Object item) {
        boolean ret;

        if (!checkExists(item)) return false;

        Item itemDB = (Item) item;

        String whereClause = ID_COLUMN + "=?";

        String[] whereArgs = {String.valueOf(itemDB.getId())};


        startAuditInventoryProcess(itemDB, AuditOnCondition.onRemove);

        openDatabase();
        int res = getDatabase().delete(DATABASE_TABLE, whereClause, whereArgs);
        closeDatabase();

        ret = res > 0;

        if (getDatabaseListener() != null) getDatabaseListener().onDatabaseItemRemoved(ret, item);
        return ret;
    }

    @Override
    public boolean checkExists(Object item) {
        if (item == null) return false;

        boolean exists = false;

        if (item instanceof Item) {
            Item storageToken = (Item) item;

            ArrayList<Object> items = getItems();

            for (Object obj : items) {
                Item ref = (Item) obj;

                exists = ref.equals(storageToken);

                if (exists) break;
            }
        }

        return exists;
    }

    @Override
    public ArrayList<Object> getItems() {
        ArrayList<Object> items = new ArrayList<>();
        ArrayList<ItemSpecial> specials = itemSpecialDatabase.getAll();

        openDatabase();

        if (getDatabase() == null) {
            closeDatabase();
            return items;
        }
        Cursor c = getDatabase().query(DATABASE_TABLE, null, null, null, null, null, ITEM_NAME_COLUMN + " ASC");

        if (c.moveToFirst()) {
            do {

                try {
                    //region get database content
                    int id = c.getInt(c.getColumnIndex(ID_COLUMN));
                    String itemName, barcode;
                    itemName = c.getString(c.getColumnIndex(ITEM_NAME_COLUMN));
                    barcode = c.getString(c.getColumnIndex(ITEM_BARCODE_COLUMN));

                    double itemCostPerUnit;
                    itemCostPerUnit = c.getDouble(c.getColumnIndex(ITEM_COST_PER_UNIT_COLUMN));

                    int itemUnits;
                    itemUnits = c.getInt(c.getColumnIndex(ITEM_UNITS_COLUMN));

                    int itemDamage;
                    itemDamage = c.getInt(c.getColumnIndex(ITEM_DAMAGE_COLUMN));

                    double itemPriceTotal;
                    itemPriceTotal = c.getDouble(c.getColumnIndex(ITEM_TOTAL_COLUMN));

                    int itemUnitRemaining;
                    itemUnitRemaining = c.getInt(c.getColumnIndex(ITEM_REMAINING_COLUMN));

                    long itemTimeStamp;
                    itemTimeStamp = c.getLong(c.getColumnIndex(ITEM_TIMESTAMP_COLUMN));


                    //endregion

                    //region apply database content to Token item
                    Item item = new Item();

                    item.setId(id);

                    item.setItemName(itemName);

                    item.setItemUnits(itemUnits);

                    item.setItemTimeStamp(itemTimeStamp);

                    item.setItemUnitRemaining(itemUnitRemaining);

                    item.setBarcode(barcode);

                    item.setItemCostPerUnit(itemCostPerUnit);

                    item.setItemPriceTotal(itemPriceTotal);

                    item.setItemDamage(itemDamage);

                    //this links the special to this item reference
                    for (ItemSpecial itemSpecial : specials) {
                        if (itemSpecial.getItemId() != item.getId()) continue;
                        item.setSpecial(itemSpecial);
                        break;
                    }

                    //endregion

                    items.add(item);
                } catch (Exception ignored) {
                }

            } while (c.moveToNext());

            if (!c.isClosed()) c.close();
        }

        closeDatabase();

        //get ItemSpecial
        ArrayList<ItemSpecial> itemSpecials = new ItemSpecialDatabase(getContext()).getAll();
        int previousIndex = 0;
        for (ItemSpecial special : itemSpecials) {
            for (int currentIndex = previousIndex; currentIndex < items.size(); currentIndex++) {
                Object objItem = items.get(currentIndex);
                Item item = (Item) objItem;
                if (special.getItemId() == item.getId()) {
                    item.setSpecial(special);
                    previousIndex = currentIndex;
                    break;
                }
            }
        }

        return items;
    }

    @Override
    public ArrayList<Object> getItemsOf(Object item) {
        return null;
    }

    public int getCount() {
        int count;
        openDatabase();
        try {
            @SuppressLint("Recycle") Cursor c = getDatabase().query(DATABASE_TABLE, null, null, null, null, null, null);
            count = c.getCount();

        } catch (Exception e) {
            count = 0;
        }
        closeDatabase();
        return count;
    }

    public ArrayList<Item> getAll() {
        ArrayList<Object> itemsDb = getItems();
        ArrayList<Item> items = new ArrayList<>();

        for (Object objItem : itemsDb) {

            Item item = (Item) objItem;
            items.add(item);
        }

        return items;
    }


    enum AuditOnCondition {
        onInsert, onUpdate, onRemove
    }

    private synchronized void auditInventory(Item item, AuditOnCondition auditCondition) {
        //get All InventoryData
        InventoryDatabase inventoryDatabase = new InventoryDatabase(getContext());
        ItemDatabase itemDatabase = new ItemDatabase(getContext());
        ArrayList<Item> itemsDatabase = itemDatabase.getAll();
        ArrayList<InventoryData> inventoryDataHolder = inventoryDatabase.getAll();
        Date itemDate = new Date(item.getItemTimeStamp());

        boolean inventoryExists = false;
        for (InventoryData inventoryData : inventoryDataHolder) {
            Date inventoryDate = new Date(inventoryData.getTimestamp());
            if (itemDate.getDate() == inventoryDate.getDate()) {
                inventoryExists = true;
                break;
            }
        }

        long stockUnits = 0;
        double stockPriceTotal = 0.00;

        switch (auditCondition) {
            case onInsert:
                if (!inventoryExists) {
                    InventoryData data = new InventoryData();
                    data.setTimestamp(item.getItemTimeStamp());

                    stockUnits = item.getItemUnitRemaining() - item.getItemUnits();
                    item.setItemUnitRemaining((int) stockUnits);
                    stockPriceTotal = (item.getItemCostPerUnit() * stockUnits);

                    if (!inventoryDataHolder.isEmpty()) {
                        stockUnits += inventoryDataHolder.get(inventoryDataHolder.size() - 1).getStockUnitCount();
                        stockPriceTotal += inventoryDataHolder.get(inventoryDataHolder.size() - 1).getStockPriceTotal();
                    }

                    data.setStockUnitCount(stockUnits);
                    data.setStockPriceTotal(stockPriceTotal);

                    inventoryDatabase.addItem(data);
                    inventoryDataHolder.add(data);
                    return;
                } else if (!inventoryDataHolder.isEmpty()) {
                    stockUnits = inventoryDataHolder.get(inventoryDataHolder.size() - 1).getStockUnitCount();
                    stockUnits += item.getItemUnitRemaining();
                    stockPriceTotal = inventoryDataHolder.get(inventoryDataHolder.size() - 1).getStockPriceTotal();
                    stockPriceTotal += (item.getItemUnitRemaining() * item.getItemCostPerUnit());
                }
                break;
            case onUpdate:
                if (!inventoryDataHolder.isEmpty()) {
                    if (item.getItemUnits() > 0) {

                        //get Item's unitRemaining before update
                        stockUnits = item.getItemUnitRemaining() + item.getItemUnits();
                        item.setItemUnitRemaining((int) stockUnits);

                        stockUnits = inventoryDataHolder.get(inventoryDataHolder.size() - 1).getStockUnitCount();
                        stockUnits += item.getItemUnits();

                        //stock Price Total -> before update:
                        stockPriceTotal = inventoryDataHolder.get(inventoryDataHolder.size() - 1).getStockPriceTotal();

                        //on Update:
                        stockPriceTotal += (item.getItemCostPerUnit() * item.getItemUnits());
                    }
                }
                break;
            case onRemove:
                if (!inventoryDataHolder.isEmpty()) {
                    //get Item's unitRemaining before being removed
                    Item itemBeforeBeingRemoved = itemsDatabase.get(itemsDatabase.indexOf(item));
                    stockUnits = inventoryDataHolder.get(inventoryDataHolder.size() - 1).getStockUnitCount();
                    stockUnits -= itemBeforeBeingRemoved.getItemUnitRemaining();

                    //stock Price Total -> before remove:
                    stockPriceTotal = inventoryDataHolder.get(inventoryDataHolder.size() - 1).getStockPriceTotal();
                    //on remove:
                    stockPriceTotal -= (itemBeforeBeingRemoved.getItemUnitRemaining() * itemBeforeBeingRemoved.getItemCostPerUnit());
                }
                break;
        }


        for (InventoryData inventoryData : inventoryDataHolder) {
            Date inventoryDate = new Date(inventoryData.getTimestamp());
            if (itemDate.getDate() == inventoryDate.getDate()) {
                for (Item databaseItem : itemsDatabase) {
                    if (!item.equals(databaseItem)) continue;

                    inventoryData.setStockUnitCount(stockUnits);
                    inventoryData.setStockPriceTotal(stockPriceTotal);
                    inventoryData.setTimestamp(System.currentTimeMillis());
                    inventoryDatabase.updateItem(inventoryData);
                    break;
                }
            }
        }
    }

    private void startAuditInventoryProcess(final Item item, final AuditOnCondition auditOnCondition) {
        auditInventory(item, auditOnCondition);
    }
}