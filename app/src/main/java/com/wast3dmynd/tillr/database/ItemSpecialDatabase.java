package com.wast3dmynd.tillr.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.wast3dmynd.tillr.database.utils.DatabaseDelegate;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.ItemSpecial;

import java.util.ArrayList;

public class ItemSpecialDatabase extends DatabaseDelegate {

    //database table
    private final String DATABASE_TABLE = "Specials";

    // Columns
    private final String ID_COLUMN = "id";
    private final String FOREIGN_KEY_COLUMN = "item_id";
    private final String ITEM_SPECIAL_PRICE_COLUMN = "price";
    private final String ITEM_SPECIAL_START_DATE_COLUMN = "start_date";
    private final String ITEM_SPECIAL_END_DATE_COLUMN = "end_date";
    private final String ITEM_SPECIAL_START_TIME_COLUMN = "start_time";
    private final String ITEM_SPECIAL_END_TIME_COLUMN = "end_time";
    private final String ITEM_TIMESTAMP_COLUMN = "timestamp";

    public ItemSpecialDatabase(Context context) {
        super(context);

    }

    @Override
    protected String onCreateScheme() {

        return "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE +
                String.format("(%s INTEGER PRIMARY KEY  AUTOINCREMENT,", ID_COLUMN) +
                String.format("%s INTEGER,", FOREIGN_KEY_COLUMN) +
                String.format("%s DOUBLE,", ITEM_SPECIAL_PRICE_COLUMN) +
                String.format("%s LONG,", ITEM_SPECIAL_START_DATE_COLUMN) +
                String.format("%s LONG,", ITEM_SPECIAL_END_DATE_COLUMN) +
                String.format("%s LONG,", ITEM_SPECIAL_START_TIME_COLUMN) +
                String.format("%s LONG,", ITEM_SPECIAL_END_TIME_COLUMN) +
                String.format("%s LONG,", ITEM_TIMESTAMP_COLUMN) +
                String.format("FOREIGN KEY(%s) REFERENCES %s(%s));", FOREIGN_KEY_COLUMN, ItemDatabase.DATABASE_TABLE,
                        ItemDatabase.ID_COLUMN);
    }

    @Override
    public boolean addItem(Object item) {
        boolean result;

        if (item == null) return false;

        if (checkExists(item)) {
            return updateItem(item);
        }

        if (!(item instanceof ItemSpecial)) return false;

        ItemSpecial itemSpecialDB = (ItemSpecial) item;

        if (!itemSpecialDB.isSpecialValid()) return false;

        ContentValues values = new ContentValues();

        values.put(FOREIGN_KEY_COLUMN, itemSpecialDB.getItemId());
        values.put(ITEM_SPECIAL_PRICE_COLUMN, itemSpecialDB.getSpecialPrice());
        values.put(ITEM_SPECIAL_START_DATE_COLUMN, itemSpecialDB.getSpecialStartDate());
        values.put(ITEM_SPECIAL_END_DATE_COLUMN, itemSpecialDB.getSpecialEndDate());
        values.put(ITEM_SPECIAL_START_TIME_COLUMN, itemSpecialDB.getSpecialStartTime());
        values.put(ITEM_SPECIAL_END_TIME_COLUMN, itemSpecialDB.getSpecialEndTime());
        values.put(ITEM_TIMESTAMP_COLUMN, itemSpecialDB.getTimestamp());

        float res;
        openDatabase();
        res = getDatabase().insert(DATABASE_TABLE, null, values);
        closeDatabase();

        result = res > 0;

        itemSpecialDB.setId((int) res);

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


        ItemSpecial itemSpecialDB = (ItemSpecial) item;

        if (!itemSpecialDB.isSpecialValid()) return false;

        String whereClause = ID_COLUMN + "=?";

        String[] whereArgs = {String.valueOf(itemSpecialDB.getId())};

        ContentValues values = new ContentValues();
        values.put(FOREIGN_KEY_COLUMN, itemSpecialDB.getItemId());
        values.put(ITEM_SPECIAL_PRICE_COLUMN, itemSpecialDB.getSpecialPrice());
        values.put(ITEM_SPECIAL_START_DATE_COLUMN, itemSpecialDB.getSpecialStartDate());
        values.put(ITEM_SPECIAL_END_DATE_COLUMN, itemSpecialDB.getSpecialEndDate());
        values.put(ITEM_SPECIAL_START_TIME_COLUMN, itemSpecialDB.getSpecialStartTime());
        values.put(ITEM_SPECIAL_END_TIME_COLUMN, itemSpecialDB.getSpecialEndTime());
        values.put(ITEM_TIMESTAMP_COLUMN, itemSpecialDB.getTimestamp());

        openDatabase();
        itemSpecialDB.setId(getDatabase().update(DATABASE_TABLE, values, whereClause, whereArgs));
        closeDatabase();

        ret = itemSpecialDB.getId() > 0;
        if (getDatabaseListener() != null) getDatabaseListener().onDatabaseItemUpdated(ret, item);

        return ret;
    }

    @Override
    public boolean removeItem(Object item) {
        boolean ret;

        if (!checkExists(item)) return false;

        ItemSpecial itemSpecial = (ItemSpecial) item;

        String whereClause = ID_COLUMN + "=? AND " + FOREIGN_KEY_COLUMN + "=?";

        String[] whereArgs = {String.valueOf(itemSpecial.getId()), String.valueOf(itemSpecial.getItemId())};

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
            ItemSpecial itemSpecial = (ItemSpecial) item;

            ArrayList<Object> items = getItems();

            for (Object obj : items) {
                ItemSpecial ref = (ItemSpecial) obj;

                exists = ref.equals(itemSpecial);

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
        Cursor c = getDatabase().query(DATABASE_TABLE, null, null, null, null, null, ID_COLUMN + " ASC");

        if (c.moveToFirst()) {
            do {

                try {
                    //region get database content
                    int id = c.getInt(c.getColumnIndex(ID_COLUMN));
                    int foreign_key = c.getInt(c.getColumnIndex(FOREIGN_KEY_COLUMN));
                    double price = c.getDouble(c.getColumnIndex(ITEM_SPECIAL_PRICE_COLUMN));
                    long startDate = c.getLong(c.getColumnIndex(ITEM_SPECIAL_START_DATE_COLUMN));
                    long endDate = c.getLong(c.getColumnIndex(ITEM_SPECIAL_END_DATE_COLUMN));
                    long startTime = c.getLong(c.getColumnIndex(ITEM_SPECIAL_START_TIME_COLUMN));
                    long endTime = c.getLong(c.getColumnIndex(ITEM_SPECIAL_END_TIME_COLUMN));
                    long timestamp = c.getLong(c.getColumnIndex(ITEM_TIMESTAMP_COLUMN));
                    //endregion

                    //region apply database content to Token item
                    ItemSpecial itemSpecial = new ItemSpecial();
                    itemSpecial.setId(id);
                    itemSpecial.setItemId(foreign_key);
                    itemSpecial.setSpecialPrice(price);
                    itemSpecial.setSpecialStartDate(startDate);
                    itemSpecial.setSpecialEndDate(endDate);
                    itemSpecial.setSpecialStartTime(startTime);
                    itemSpecial.setSpecialEndTime(endTime);
                    itemSpecial.setTimestamp(timestamp);
                    //endregion

                    items.add(itemSpecial);
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
        try {
            @SuppressLint("Recycle") Cursor c = getDatabase().query(DATABASE_TABLE, null, null, null, null, null, null);
            count = c.getCount();

        } catch (Exception e) {
            count = 0;
        }
        closeDatabase();
        return count;
    }


    public ArrayList<ItemSpecial> getAll() {
        ArrayList<Object> itemsDb = getItems();
        ArrayList<ItemSpecial> itemSpecials = new ArrayList<>();

        for (Object objItem : itemsDb) {

            ItemSpecial special = (ItemSpecial) objItem;
            itemSpecials.add(special);
        }

        return itemSpecials;
    }

}