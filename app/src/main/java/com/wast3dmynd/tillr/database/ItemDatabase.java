package com.wast3dmynd.tillr.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.wast3dmynd.tillr.database.utils.DatabaseDelegate;
import com.wast3dmynd.tillr.entity.Item;

import java.util.ArrayList;

public class ItemDatabase extends DatabaseDelegate {

    //database table
    private final String DATABASE_TABLE = "Items";

    // Columns
    private final String ID_COLUMN = "id";
    private final String ITEM_NAME_COLUMN = "item_name";
    private final String ITEM_BARCODE_COLUMN = "item_barcode";
    private final String ITEM_COST_PER_UNIT_COLUMN = "item_cost_per_unit";
    private final String ITEM_UNITS_COLUMN = "item_units";
    private final String ITEM_DAMAGE_COLUMN = "item_damage";
    private final String ITEM_TOTAL_COLUMN = "item_total";
    private final String ITEM_REMAINING_COLUMN = "item_remaining";
    private final String ITEM_TIMESTAMP_COLUMN = "item_timestamp";


    public ItemDatabase(Context context) {
        super(context);
    }

    @Override
    protected String onCreateScheme() {

        return "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE +
                String.format("(%s INTEGER PRIMARY KEY  AUTOINCREMENT,", ID_COLUMN) +
                String.format("%s TEXT NOT NULL,", ITEM_NAME_COLUMN) +
                String.format("%s TEXT,", ITEM_BARCODE_COLUMN) +
                String.format("%s DOUBLE,", ITEM_COST_PER_UNIT_COLUMN) +
                String.format("%s INTEGER,", ITEM_DAMAGE_COLUMN) +
                String.format("%s DOUBLE,", ITEM_TOTAL_COLUMN) +
                String.format("%s INTEGER,", ITEM_REMAINING_COLUMN) +
                String.format("%s LONG,", ITEM_TIMESTAMP_COLUMN) +
                String.format("%s INTEGER);", ITEM_UNITS_COLUMN);
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

        String whereClause = ID_COLUMN + "=?";

        String[] whereArgs = {String.valueOf(itemDB.getId())};

        ContentValues values = new ContentValues();

        values.put(ITEM_NAME_COLUMN, itemDB.getItemName());

        values.put(ITEM_BARCODE_COLUMN, itemDB.getBarcode());

        values.put(ITEM_COST_PER_UNIT_COLUMN, itemDB.getItemCostPerUnit());

        values.put(ITEM_UNITS_COLUMN, itemDB.getItemUnits());

        values.put(ITEM_DAMAGE_COLUMN, itemDB.getItemDamage());

        values.put(ITEM_TOTAL_COLUMN, itemDB.getItemPriceTotal());

        values.put(ITEM_REMAINING_COLUMN, itemDB.getItemUnitRemaining());

        values.put(ITEM_TIMESTAMP_COLUMN, itemDB.getItemTimeStamp());

        openDatabase();
        ret = getDatabase().update(DATABASE_TABLE, values, whereClause, whereArgs) > 0;
        closeDatabase();


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
                    //endregion

                    items.add(item);
                } catch (Exception ignored) {
                }

            } while (c.moveToNext());

            if (!c.isClosed()) c.close();
        }

        closeDatabase();

        return items;
    }

    @Override
    public ArrayList<Object> getItemsOf(Object item) {
        return null;
    }

    public int getCount() {
        int count;
        openDatabase();
        @SuppressLint("Recycle") Cursor c = getDatabase().query(DATABASE_TABLE, null, null, null, null, null, null);
        count = c.getCount();
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
}