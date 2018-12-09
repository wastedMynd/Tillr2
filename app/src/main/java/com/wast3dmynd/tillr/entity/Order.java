package com.wast3dmynd.tillr.entity;

import android.content.Context;

import com.wast3dmynd.tillr.database.OrderDatabase;
import com.wast3dmynd.tillr.utils.DayFormats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Order implements Serializable {

    //region entity attributes
    private int id;
    private double total = 0, funds = 0;
    private long date = System.currentTimeMillis(), timeStamp = System.currentTimeMillis();
    private ArrayList<Item> items = new ArrayList<>();
    //endregion

    //region helper attribute(s)
    private boolean isLastOrder = false;
    private boolean isSummarized = false;

    public static int getNewID(Context context) {
        OrderDatabase database = new OrderDatabase(context);

        ArrayList<Object> orderObjs = database.getItems();

        return ((Order) orderObjs.get(0)).getId() + 1;
    }
    //endregion

    //region getters and setters

    //region id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    //endregion

    //region total
    public double getTotal() {
        total = 0.00;
        for (Item item : items) total += item.getItemPriceTotal();
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
    //endregion

    //region funds
    public double getFunds() {
        return funds;
    }

    public void setFunds(double funds) {
        this.funds = funds;
    }
    //endregion

    //region credit
    public double getCredit() {
        double credit = getFunds() - getTotal();
        return credit > 0.00 ? credit : 0.00;
    }
    //endregion

    //region date
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
    //endregion

    //region time
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    //endregion

    //region items
    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }
    //endregion

    //region isLastOrder
    public boolean isLastOrder() {
        return isLastOrder;
    }

    public void setLastOrder(boolean lastOrder) {
        isLastOrder = lastOrder;
    }
    //endregion

    //region isSummarized
    public boolean isSummarized() {
        return isSummarized;
    }

    public void setSummarized(boolean summarized) {
        isSummarized = summarized;
    }
    //endregion

    //endregion

    //region control methods
    public boolean isOrderValid() {
        //show fabOrderCheckout based on orderTotal>0 && amount Paid
        total = getTotal();
        boolean amountPaid = (getFunds() > 0) && (getFunds() >= total);
        boolean orderTotalValid = (total > 0);
        return (amountPaid && orderTotalValid);
    }

    @Override
    public boolean equals(Object obj) {
        return ((Order) obj).getId() == this.getId();
    }
    //endregion

    //region Gui classes
    public static class Timeline implements Serializable {

        private Order lastPlacedOrder;

        public Order getLastPlacedOrder() {
            return lastPlacedOrder;
        }

        private void setLastPlacedOrder(Order lastPlacedOrder) {
            this.lastPlacedOrder = lastPlacedOrder;
        }

        private boolean isDateSetter;

        public boolean isDateSetter() {
            return isDateSetter;
        }

        private void setDateSetter(boolean dateSetter) {
            isDateSetter = dateSetter;
        }

        private ArrayList<Order> childOrders = new ArrayList<>();

        public ArrayList<Order> getChildOrders() {
            return childOrders;
        }

        private void setChildOrders(ArrayList<Order> childOrders) {
            this.childOrders = childOrders;
        }

        private Timeline(Order lastPlacedOrder, ArrayList<Order> childOrders) {
            setLastPlacedOrder(lastPlacedOrder);
            setChildOrders(childOrders);
            setDateSetter(!childOrders.isEmpty());
        }

        @Override
        public boolean equals(Object obj) {
            return (((Order) obj).getId() == lastPlacedOrder.getId());
        }
    }

    public static class OrderTimelineHelper implements Serializable {

        /**
         * todo check if previous order time lines are implemented.
         * For example let say Today's date is Monday, and you have some orders;
         * the returned Timeline will include the last placed order from today,-
         * and its' childOrders.
         * But previous Monday's and other prior Monday's, are not included.
         * This is not a true time line; therefore this will show a weekly time line;
         * and will show new order time lines at the start of very week to follow.
         *
         * @param context android context is need to access the {@link OrderDatabase}
         */
        public static ArrayList<Timeline> get(Context context) {

            ArrayList<Timeline> timelines = new ArrayList<>();

            //OrderDatabase, orders.
            ArrayList<Object> orderObjects = new OrderDatabase(context).getItems();


            //construct from OrderDatabase, order time lines.
            for (Object orderObject : orderObjects) {

                //database order
                Order order = (Order) orderObject;

                //its day format eg {Monday,Tuesday etc..} see {@link DayFormats}, is queried.
                DayFormats orderDayFormat = DayFormats.getDayFormat(order.getDate());


                for (DayFormats dayFormat : DayFormats.values()) {

                    //then verified against dayFormat
                    if (!orderDayFormat.equals(dayFormat)) continue;

                    if (timelines.isEmpty()) {
                        order.setLastOrder(true);
                        timelines.add(new Timeline(order, new ArrayList<Order>()));
                    } else {
                        boolean isChildOrder = false;


                        int index = 0;
                        for (Timeline timeline : timelines) {
                            Date dateOfLastOrder = new Date(timeline.getLastPlacedOrder().getDate());
                            Date dateOfPotentialChildOrder = new Date(order.getDate());
                            isChildOrder = (dateOfPotentialChildOrder.getDate() == dateOfLastOrder.getDate());
                            if (isChildOrder) {
                                break;
                            }
                            index++;
                        }

                        if (isChildOrder) {
                            Timeline timeline = timelines.get(index);
                            order.setLastOrder(false);
                            timeline.getChildOrders().add(order);
                            timelines.remove(index);
                            timelines.add(index, timeline);
                        } else {
                            order.setLastOrder(true);
                            timelines.add(new Timeline(order, new ArrayList<Order>()));
                        }

                    }
                }
            }

            return timelines;
        }


        public static ArrayList<Order> getTodaysOrders(Context context) {

            ArrayList<Order> todaysOrders = new ArrayList<>();

            DayFormats todaysFormat = DayFormats.getTodaysFormat();

            ArrayList<Order.Timeline> timeLines;
            timeLines = Order.OrderTimelineHelper.get(context);

            for (Order.Timeline timeline : timeLines) {
               DayFormats timelineDayFormat = DayFormats.getDayFormat(timeline.getLastPlacedOrder().getTimeStamp());
                if(!timelineDayFormat.equals(todaysFormat))continue;

                todaysOrders.add(timeline.getLastPlacedOrder());
                todaysOrders.addAll(timeline.getChildOrders());
            }


            return todaysOrders;
        }
    }

    public static class TimelineData {
        long date;
        int ordersPlaced;
        int remainingUnits;
        int soldUnits;
        double grandTotal;
        double damageTotal;
        double assertTotal;

        //region getters and setters

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public int getRemainingUnits() {
            return remainingUnits;
        }

        public void setRemainingUnits(int remainingUnits) {
            this.remainingUnits = remainingUnits;
        }

        public int getSoldUnits() {
            return soldUnits;
        }

        public void setSoldUnits(int soldUnits) {
            this.soldUnits = soldUnits;
        }

        public double getGrandTotal() {
            return grandTotal;
        }

        public void setGrandTotal(double grandTotal) {
            this.grandTotal = grandTotal;
        }

        public double getAssertTotal() {
            return assertTotal;
        }

        public void setAssertTotal(double assertTotal) {
            this.assertTotal = assertTotal;
        }

        public int getOrdersPlaced() {
            return ordersPlaced;
        }

        public void setOrdersPlaced(int ordersPlaced) {
            this.ordersPlaced = ordersPlaced;
        }

        public double getDamageTotal() {
            return damageTotal;
        }

        public void setDamageTotal(double damageTotal) {
            this.damageTotal = damageTotal;
        }


        //endregion
    }
//endregion
}
