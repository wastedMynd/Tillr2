package com.wast3dmynd.tillr.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.wast3dmynd.tillr.database.utils.DatabaseDelegate;
import com.wast3dmynd.tillr.entity.InventoryData;

import java.util.ArrayList;

public class InventoryDatabase extends DatabaseDelegate {

    //database table
    private final String DATABASE_TABLE = "Inventory";

    // Columns
    private final String ID_COLUMN = "_id";
    private final String STOCK_UNIT_COUNT_COLUMN = "_stock_unit_count";
    private final String STOCK_PRICE_TOTAL_COLUMN = "_stock_price_total";
    private final String TIMESTAMP_COLUMN = "_timestamp";


    public InventoryDatabase(Context context) {
        super(context);
    }

    @Override
    protected String onCreateScheme() {

        return "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE +
                String.format("(%s INTEGER PRIMARY KEY  AUTOINCREMENT,", ID_COLUMN) +
                String.format("%s LONG,", STOCK_UNIT_COUNT_COLUMN) +
                String.format("%s DOUBLE,", STOCK_PRICE_TOTAL_COLUMN) +
                String.format("%s LONG);", TIMESTAMP_COLUMN);
    }

    @Override
    public boolean addItem(Object param) {
        boolean result;

        if (param == null) return false;

        if (checkExists(param)) {
            return updateItem(param);
        }

        if (!(param instanceof InventoryData)) return false;

        InventoryData inventoryData = (InventoryData) param;

        if (!inventoryData.isValid()) return false;

        ContentValues values = new ContentValues();

        values.put(STOCK_UNIT_COUNT_COLUMN, inventoryData.getStockUnitCount());

        values.put(STOCK_PRICE_TOTAL_COLUMN, inventoryData.getStockPriceTotal());

        values.put(TIMESTAMP_COLUMN, inventoryData.getTimestamp());

        float res;
        openDatabase();
        res = getDatabase().insert(DATABASE_TABLE, null, values);
        closeDatabase();

        result = res > 0;

        inventoryData.setId((int) res);

        if (getDatabaseListener() != null)
            getDatabaseListener().onDatabaseItemInserted(result, inventoryData);

        return result;
    }

    @Override
    public boolean updateItem(Object param) {
        boolean ret;

        if (!checkExists(param)) {
            return addItem(param);
        }

        if (!(param instanceof InventoryData)) return false;

        InventoryData inventoryData = (InventoryData) param;

        if (!inventoryData.isValid()) return false;

        String whereClause = ID_COLUMN + "=?";

        String[] whereArgs = {String.valueOf(inventoryData.getId())};

        ContentValues values = new ContentValues();

        values.put(STOCK_UNIT_COUNT_COLUMN, inventoryData.getStockUnitCount());

        values.put(STOCK_PRICE_TOTAL_COLUMN, inventoryData.getStockPriceTotal());

        values.put(TIMESTAMP_COLUMN, inventoryData.getTimestamp());

        openDatabase();
        ret = getDatabase().update(DATABASE_TABLE, values, whereClause, whereArgs) > 0;
        closeDatabase();


        if (getDatabaseListener() != null)
            getDatabaseListener().onDatabaseItemUpdated(ret, inventoryData);

        return ret;
    }

    @Override
    public boolean removeItem(Object param) {
        boolean ret;

        if (!checkExists(param)) return false;

        InventoryData inventoryData = (InventoryData) param;

        String whereClause = ID_COLUMN + "=?";

        String[] whereArgs = {String.valueOf(inventoryData.getId())};

        openDatabase();
        int res = getDatabase().delete(DATABASE_TABLE, whereClause, whereArgs);
        closeDatabase();

        ret = res > 0;

        if (getDatabaseListener() != null)
            getDatabaseListener().onDatabaseItemRemoved(ret, inventoryData);

        return ret;
    }

    @Override
    public boolean checkExists(Object param) {
        if (param == null) return false;

        boolean exists = false;

        if (param instanceof InventoryData) {
            InventoryData inventoryData = (InventoryData) param;

            ArrayList<Object> items = getItems();

            for (Object obj : items) {
                InventoryData ref = (InventoryData) obj;

                exists = ref.equals(inventoryData);

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
        Cursor c = getDatabase().query(DATABASE_TABLE, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            do {

                try {
                    //region get database content

                    int id;
                    long stockUnitCount;
                    double stockPriceTotal;
                    long timestamp;


                    id = c.getInt(c.getColumnIndex(ID_COLUMN));
                    stockUnitCount = c.getLong(c.getColumnIndex(STOCK_UNIT_COUNT_COLUMN));
                    stockPriceTotal = c.getDouble(c.getColumnIndex(STOCK_PRICE_TOTAL_COLUMN));
                    timestamp = c.getLong(c.getColumnIndex(TIMESTAMP_COLUMN));
                    //endregion

                    //region apply database content to Token item
                    InventoryData inventoryData = new InventoryData();

                    inventoryData.setId(id);

                    inventoryData.setStockUnitCount(stockUnitCount);

                    inventoryData.setStockPriceTotal(stockPriceTotal);

                    inventoryData.setTimestamp(timestamp);
                    //endregion

                    items.add(inventoryData);
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


    public ArrayList<InventoryData> getAll() {
        ArrayList<Object> itemsDb = getItems();
        ArrayList<InventoryData> inventoryDataHolder = new ArrayList<>();

        for (Object objItem : itemsDb) {

            InventoryData inventoryData = (InventoryData) objItem;
            inventoryDataHolder.add(inventoryData);
        }

        return inventoryDataHolder;
    }
}