package com.wast3dmynd.tillr.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;
import com.wast3dmynd.tillr.database.utils.DatabaseDelegate;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.Order;

import java.util.ArrayList;

public class OrderDatabase extends DatabaseDelegate {

    //database table
    private final String DATABASE_TABLE = "Orders";

    // Columns
    private final String ORDER_ID_COLUMN = "ORDER_ID_COLUMN";
    private final String ORDER_TOTAL_COLUMN = "ORDER_TOTAL_COLUMN";
    private final String ORDER_FUNDS_COLUMN = "ORDER_FUNDS_COLUMN";
    private final String ORDER_CREDIT_COLUMN = "ORDER_CREDIT_COLUMN";
    private final String ORDER_DATE_COLUMN = "ORDER_DATE_COLUMN";
    private final String ORDER_TIME_COLUMN = "ORDER_TIME_COLUMN";
    private final String ORDER_ITEMS_COLUMN = "ORDER_ITEMS_COLUMN";


    public OrderDatabase(Context context) {
        super(context);
    }

    @Override
    protected String onCreateScheme() {

        return "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE +
                String.format("(%s INTEGER PRIMARY KEY  AUTOINCREMENT,", ORDER_ID_COLUMN) +
                String.format("%s DOUBLE,", ORDER_FUNDS_COLUMN) +
                String.format("%s DOUBLE,", ORDER_TOTAL_COLUMN) +
                String.format("%s DOUBLE,", ORDER_CREDIT_COLUMN) +
                String.format("%s TEXT NOT NULL,", ORDER_ITEMS_COLUMN) +
                String.format("%s LONG,", ORDER_TIME_COLUMN) +
                String.format("%s LONG);", ORDER_DATE_COLUMN);
    }

    @Override
    public boolean addItem(Object objOrder) {
        boolean result;

        if (objOrder == null) return false;

        if (checkExists(objOrder)) {
            return updateItem(objOrder);
        }

        if (!(objOrder instanceof Order)) return false;

        Order order = (Order) objOrder;

        if (!order.isOrderValid()) return false;

        ContentValues values = new ContentValues();

        values.put(ORDER_TOTAL_COLUMN, order.getTotal());

        values.put(ORDER_FUNDS_COLUMN, order.getFunds());

        values.put(ORDER_CREDIT_COLUMN, order.getCredit());

        values.put(ORDER_DATE_COLUMN, order.getDate());

        values.put(ORDER_TIME_COLUMN, order.getTimeStamp());

        StringBuilder itemsBuilder = new StringBuilder();
        for (Item item : order.getItems()) {
            item.setOrderId(order.getId());
            itemsBuilder.append(item.toJson());
            itemsBuilder.append("~");
        }

        String items = itemsBuilder.replace(itemsBuilder.length() - 1, itemsBuilder.length() - 1, "").toString();
        values.put(ORDER_ITEMS_COLUMN, items);

        float res;
        openDatabase();
        res = getDatabase().insert(DATABASE_TABLE, null, values);
        closeDatabase();

        result = res > 0;

        order.setId((int) res);

        if (getDatabaseListener() != null)
            getDatabaseListener().onDatabaseItemInserted(result, objOrder);

        return result;
    }

    @Override
    public boolean updateItem(Object objOrder) {
        boolean ret;

        if (!checkExists(objOrder)) {
            return addItem(objOrder);
        }

        if (!(objOrder instanceof Order)) return false;

        Order order = (Order) objOrder;

        if (!order.isOrderValid()) return false;

        String whereClause = ORDER_ID_COLUMN + "=?";

        String[] whereArgs = {String.valueOf(order.getId())};

        ContentValues values = new ContentValues();

        values.put(ORDER_TOTAL_COLUMN, order.getTotal());

        values.put(ORDER_FUNDS_COLUMN, order.getFunds());

        values.put(ORDER_CREDIT_COLUMN, order.getCredit());

        values.put(ORDER_DATE_COLUMN, order.getDate());

        values.put(ORDER_TIME_COLUMN, order.getTimeStamp());

        StringBuilder itemsBuilder = new StringBuilder();
        for (Item item : order.getItems()) {
            item.setOrderId(order.getId());
            itemsBuilder.append(item.toJson());
            itemsBuilder.append("~");
        }
        String items = itemsBuilder.replace(itemsBuilder.length() - 1, itemsBuilder.length() - 1, "").toString();
        values.put(ORDER_ITEMS_COLUMN, items);


        openDatabase();
        ret = getDatabase().update(DATABASE_TABLE, values, whereClause, whereArgs) > 0;
        closeDatabase();


        if (getDatabaseListener() != null)
            getDatabaseListener().onDatabaseItemUpdated(ret, objOrder);

        return ret;
    }

    @Override
    public boolean removeItem(Object objOrder) {
        boolean ret;

        if (!(objOrder instanceof Order)) return false;

        if (!checkExists(objOrder)) return false;

        Order order = (Order) objOrder;

        String whereClause = ORDER_ID_COLUMN + "=?";

        String[] whereArgs = {String.valueOf(order.getId())};

        openDatabase();
        int res = getDatabase().delete(DATABASE_TABLE, whereClause, whereArgs);
        closeDatabase();

        ret = res > 0;

        if (getDatabaseListener() != null)
            getDatabaseListener().onDatabaseItemRemoved(ret, objOrder);

        return ret;
    }

    @Override
    public boolean checkExists(Object objOrder) {

        if (objOrder == null) return false;
        if (!(objOrder instanceof Order)) throw new ClassCastException();

        Order order = (Order) objOrder;

        ArrayList<Object> objsOrder = getItems();

        boolean exists = false;
        for (Object objDbOrder : objsOrder) {
            Order tempOrder = (Order) objDbOrder;
            exists = tempOrder.equals(order);
            if (exists) break;
        }

        return exists;
    }

    @Override
    public ArrayList<Object> getItems() {
        ArrayList<Object> orderObjects = new ArrayList<>();

        openDatabase();

        if (getDatabase() == null) {
            closeDatabase();
            return orderObjects;
        }

        Cursor c = getDatabase().query(DATABASE_TABLE, null, null, null, null, null, ORDER_TIME_COLUMN + " DESC");

        if (c.moveToFirst()) {
            do {

                try {
                    //region get database content
                    int id = c.getInt(c.getColumnIndex(ORDER_ID_COLUMN));

                    double total, funds;
                    total = c.getDouble(c.getColumnIndex(ORDER_TOTAL_COLUMN));
                    funds = c.getDouble(c.getColumnIndex(ORDER_FUNDS_COLUMN));

                    long date;
                    date = c.getLong(c.getColumnIndex(ORDER_DATE_COLUMN));

                    long timeStamp;
                    timeStamp = c.getLong(c.getColumnIndex(ORDER_TIME_COLUMN));

                    String itemsStr;
                    itemsStr = c.getString(c.getColumnIndex(ORDER_ITEMS_COLUMN));
                    String[] itemsJson = itemsStr.split("~");
                    ArrayList<Item> itemsDb = new ItemDatabase(getContext()).getAll();
                    ArrayList<Item> orderItems = new ArrayList<>();
                    boolean itemFound;
                    for (Item item : itemsDb) {
                        for (String strItemJSON : itemsJson) {
                            Gson gson = new Gson();
                            Item orderedItem = gson.fromJson(strItemJSON,Item.class);
                            itemFound = (item.getId() == orderedItem.getId());
                            if(itemFound)
                            {
                                orderItems.add(orderedItem);
                                break;
                            }
                        }
                    }
                    //endregion

                    //region apply database content to Token item
                    Order order = new Order();

                    order.setId(id);

                    order.setTotal(total);

                    order.setDate(date);

                    order.setTimeStamp(timeStamp);

                    order.setFunds(funds);

                    order.setItems(orderItems);
                    //endregion

                    orderObjects.add(order);
                } catch (Exception ignored) {
                }

            } while (c.moveToNext());

            if (!c.isClosed()) c.close();
        }

        closeDatabase();

        return orderObjects;
    }



    @Override
    public ArrayList<Object> getItemsOf(Object item) {
        return null;
    }

    public int getCount() {
        int count;
        openDatabase();
        @SuppressLint("Recycle") Cursor c = getDatabase().query(DATABASE_TABLE, null, null, null, null, null, ORDER_TIME_COLUMN + " DESC");
        count = c.getCount();
        closeDatabase();
        return count;
    }

}