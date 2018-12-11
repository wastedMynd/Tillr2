package com.wast3dmynd.tillr.boundary.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.MainActivity;
import com.wast3dmynd.tillr.boundary.adapter.DashboardAdapter;
import com.wast3dmynd.tillr.boundary.interfaces.MainActivityListener;
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.database.OrderDatabase;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.Order;
import com.wast3dmynd.tillr.utils.DateFormats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DashBoardFragment extends Fragment {
    private static final int DASH_BOARD_CONTENT_LOADER_ID = 1;
    private MainActivityListener listener;
    private GraphView graph;

    //region views
    private TextView dashboard_date, dashboard_time, dashboard_sold, dashboard_stock, dashboard_orders;
    CoordinatorLayout dashboard_main;
    private ConstraintLayout dashboard_summary;
    //endregion

    public static Fragment newInstance() {
        return new DashBoardFragment();
    }

    //region life cycle
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_fragment_dashboard);

        //region init views
        dashboard_main = view.findViewById(R.id.dashboard_main);
        dashboard_summary = view.findViewById(R.id.dashboard_summary);
        dashboard_date = view.findViewById(R.id.dashboard_date);
        dashboard_time = view.findViewById(R.id.order_date);
        dashboard_sold = view.findViewById(R.id.dashboard_sold);
        dashboard_stock = view.findViewById(R.id.dashboard_stock);
        dashboard_orders = view.findViewById(R.id.dashboard_orders);
        //endregion

        //region process dashboard summary
        dashboard_summary.setVisibility(View.GONE);
        getActivity().getSupportLoaderManager().initLoader(DASH_BOARD_CONTENT_LOADER_ID, null, dashboardDataLoader);
        //endregion

        //region DashboardItems
        DashboardItem dashboardItem_newItem,
                dashboardItem_viewItems,
                dashboardItem_viewOrders,
                dashboardItem_placeOrder;

        //init DashboardItems
        dashboardItem_newItem = new DashboardItem(R.drawable.ic_create_item, R.string.title_add_item);
        dashboardItem_viewItems = new DashboardItem(R.drawable.ic_item_list, R.string.title_item_list);
        dashboardItem_viewOrders = new DashboardItem(R.drawable.ic_order_list, R.string.title_order_list);
        dashboardItem_placeOrder = new DashboardItem(R.drawable.ic_order_placement, R.string.title_activity_place_order);

        ArrayList<DashboardItem> dashboardItems = new ArrayList<>();
        dashboardItems.add(dashboardItem_newItem);
        dashboardItems.add(dashboardItem_viewItems);
        dashboardItems.add(dashboardItem_viewOrders);
        dashboardItems.add(dashboardItem_placeOrder);

        DashboardAdapter adapter = new DashboardAdapter(dashboardItems, listener);
        RecyclerView recyclerView = view.findViewById(R.id.dashboard_item_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
        //endregion

        //stats
        graph = view.findViewById(R.id.graph);
        getActivity().getSupportLoaderManager().initLoader(1989, null, inventoryDataLoaderCallbacks);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MainActivityListener))
            throw new ClassCastException("Must implement MainActivityListener");
        listener = (MainActivityListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
    //endregion

    public static class DashboardItem {
        @DrawableRes
        private int icon;
        @StringRes
        private int title;

        //region getters and setters

        public int getIcon() {
            return icon;
        }

        public void setIcon(@DrawableRes int icon) {
            this.icon = icon;
        }

        public int getTitle() {
            return title;
        }

        public void setTitle(@StringRes int title) {
            this.title = title;
        }
        //endregion


        public DashboardItem(int icon, int title) {
            this.icon = icon;
            this.title = title;
        }
    }

    private static class DashboardData {

        //data
        private long date = System.currentTimeMillis();
        private long time = System.currentTimeMillis();
        private int orders = 0;
        private int sold = 0;
        private int stock = 0;

        //getters and setters
        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public int getSold() {
            return sold;
        }

        public void setSold(int sold) {
            this.sold = sold;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }

        public int getOrders() {
            return orders;
        }

        public void setOrders(int orders) {
            this.orders = orders;
        }

        //endregion
    }

    private static class ProcessDashboardDataAsync extends AsyncTaskLoader<DashboardData> {

        public ProcessDashboardDataAsync(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Nullable
        @Override
        public DashboardData loadInBackground() {
            DashboardData dashboard = new DashboardData();

            ArrayList<Object> orderObjects = new OrderDatabase(getContext()).getItems();
            if (orderObjects.isEmpty()) return null;

            ArrayList<Order> orders = new ArrayList<>(orderObjects.size());
            for (Object object : orderObjects) orders.add((Order) object);

            ArrayList<Object> itemObjects = new ItemDatabase(getContext()).getItems();
            ArrayList<Item> items = new ArrayList<>(itemObjects.size());
            for (Object object : itemObjects) items.add((Item) object);

            ArrayList<Order.Timeline> timeLines = Order.OrderTimelineHelper.get(getContext());
            //display dashboard summary according to order data
            List<Date> dates = new ArrayList<>(orders.size());
            for (Order order : orders) dates.add(new Date(order.getDate()));
            // Get the one closest to Today's date
            Date closest = Collections.min(dates, new Comparator<Date>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                public int compare(Date d1, Date d2) {
                    long now = System.currentTimeMillis();
                    long diff1 = Math.abs(d1.getTime() - now);
                    long diff2 = Math.abs(d2.getTime() - now);
                    return Long.compare(diff1, diff2);
                }
            });
            dashboard.setDate(closest.getTime());
            dashboard.setTime(closest.getTime());

            //data
            int ordersPlaced = 0;
            int remainingUnits = 0;
            int soldUnits = 0;

            for (Order order : orders) {
                for (Order.Timeline timeline : timeLines) {
                    if (!order.equals(timeline.getLastPlacedOrder())) continue;
                    if (order.getTimeStamp() != closest.getTime()) continue;

                    ordersPlaced += timeline.getChildOrders().size() + 1;

                    for (Item item : timeline.getLastPlacedOrder().getItems()) {
                        soldUnits += item.getItemUnits();
                    }

                    for (Order childOrder : timeline.getChildOrders()) {
                        for (Item item : childOrder.getItems()) {
                            soldUnits += item.getItemUnits();
                        }
                    }
                }
            }

            for (Item item : items) remainingUnits += item.getItemUnitRemaining();

            dashboard.setOrders(ordersPlaced);
            dashboard.setStock(remainingUnits);
            dashboard.setSold(soldUnits);
            return dashboard;
        }
    }

    private LoaderManager.LoaderCallbacks dashboardDataLoader = new LoaderManager.LoaderCallbacks<DashboardData>() {

        @Nullable
        @Override
        public Loader onCreateLoader(int id, @Nullable Bundle args) {
            if (getContext() == null) return null;
            return new ProcessDashboardDataAsync(getContext());
        }

        @Override
        public void onLoadFinished(@NonNull Loader loader, DashboardData data) {
            dashboard_summary.setVisibility(View.GONE);
            DashboardData dashboard = data;

            //display dashboard date
            DateFormats dateFormats = DateFormats.Day_Month_Year;
            String dateStamp = DateFormats.getSimpleDateString(dashboard.getDate(), dateFormats);
            dashboard_date.setText(dateStamp);

            //display dashboard time
            DateFormats timeFormats = DateFormats.Hours_Minutes;
            String timeStamp = DateFormats.getSimpleDateString(dashboard.getTime(), timeFormats);
            dashboard_time.setText(timeStamp);

            //display dashboard sold
            dashboard_sold.setText(String.valueOf(dashboard.getSold()));

            //display dashboard stock
            dashboard_stock.setText(String.valueOf(dashboard.getStock()));

            //display dashboard orders
            dashboard_orders.setText(String.valueOf(dashboard.getOrders()));

            dashboard_summary.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoaderReset(@NonNull Loader loader) {

        }
    };


    private static class ProcessInventoryDataAsync extends AsyncTaskLoader<InventoryHolder> {

        public ProcessInventoryDataAsync(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Nullable
        @Override
        public InventoryHolder loadInBackground() {
            InventoryHolder holder = new InventoryHolder();

            //ArrayList<Object> orderObjs = new OrderDatabase(getContext()).getItems();
            //ArrayList<Order> orders = new ArrayList<>(orderObjs.size());
            //for (Object obj : orderObjs) orders.add((Order) obj);

            //ArrayList<Object> itemObjs = new ItemDatabase(getContext()).getItems();
            //ArrayList<Item> items = new ArrayList<>(itemObjs.size());
            //for (Object obj : itemObjs) items.add((Item) obj);

            //holder.setDatabaseItemHolder(items);
            //holder.setDatabaseOrderHolder(orders);

            holder.setTimelineDataHolder(Order.OrderTimelineHelper.get(getContext()));
            return holder;
        }
    }

    private static class InventoryHolder {
        ArrayList<Item> databaseItemHolder;
        ArrayList<Order> databaseOrderHolder;
        ArrayList<Order.Timeline> timelineHolder;

        //region getters and setters

        public ArrayList<Item> getDatabaseItemHolder() {
            return databaseItemHolder;
        }

        public void setDatabaseItemHolder(ArrayList<Item> databaseItemHolder) {
            this.databaseItemHolder = databaseItemHolder;
        }

        public ArrayList<Order> getDatabaseOrderHolder() {
            return databaseOrderHolder;
        }

        public void setDatabaseOrderHolder(ArrayList<Order> databaseOrderHolder) {
            this.databaseOrderHolder = databaseOrderHolder;
        }

        public ArrayList<Order.Timeline> getTimelineDataHolder() {
            return timelineHolder;
        }

        public void setTimelineDataHolder(ArrayList<Order.Timeline> timelineHolder) {
            this.timelineHolder = timelineHolder;
        }


        //endregion
    }

    private LoaderManager.LoaderCallbacks inventoryDataLoaderCallbacks = new LoaderManager.LoaderCallbacks() {
        @NonNull
        @Override
        public Loader onCreateLoader(int id, @Nullable Bundle args) {
            return new ProcessInventoryDataAsync(getContext());
        }

        @Override
        public void onLoadFinished(@NonNull Loader loader, Object data) {


            InventoryHolder inventoryHolder = (InventoryHolder) data;

            //region Sales
            LineGraphSeries<DataPoint> sales = new LineGraphSeries<>();

            final int MAX_DATA_POINTS = inventoryHolder.getTimelineDataHolder().size();
            int maxY = 0;
            long end = inventoryHolder.getTimelineDataHolder().get(0).getLastPlacedOrder().getTimeStamp();
            long start = inventoryHolder.getTimelineDataHolder().get(MAX_DATA_POINTS - 1).getLastPlacedOrder().getTimeStamp();

            //remember timeline contains the last placed order until - the first placed order
            //so we need to cycle backwards
            for (int index = MAX_DATA_POINTS - 1; index > -1; index--) {
                int sold = 0;
                Order.Timeline timeline = inventoryHolder.getTimelineDataHolder().get(index);
                for (Item item : timeline.getLastPlacedOrder().getItems())
                    sold += item.getItemUnits();
                for (Order order : timeline.getChildOrders())
                    for (Item item : order.getItems()) sold += item.getItemUnits();

                long date = timeline.getLastPlacedOrder().getDate();
                maxY = sold > maxY ? sold : maxY;
                sales.appendData(new DataPoint(new Date(date), sold), false, 10000, true);
            }

            String startDate = DateFormats.getSimpleDateString(start, DateFormats.Day_Month_Year);
            String endDate = DateFormats.getSimpleDateString(end, DateFormats.Day_Month_Year);

            //decor
            sales.setTitle("Sales from " + startDate + "-" + endDate);
            sales.setColor(Color.GREEN);
            sales.setDrawDataPoints(true);
            sales.setDataPointsRadius(6);
            sales.setThickness(2);

            //append
            graph.addSeries(sales);

            //set date label formatter
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(graph.getContext()));
            graph.getGridLabelRenderer().setNumHorizontalLabels(2);

            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(maxY);
            graph.getViewport().setYAxisBoundsManual(true);

            //set manual x bound to have nice steps
            graph.getViewport().setMinX(start);
            graph.getViewport().setMaxX(end);
            graph.getViewport().setXAxisBoundsManual(true);

            //as we use dates as labels,the human rounding to nice readable numbers is not necessary.
            graph.getGridLabelRenderer().setHumanRounding(true);
            //endregion

        }

        @Override
        public void onLoaderReset(@NonNull Loader loader) {

        }
    };
}
