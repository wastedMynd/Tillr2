package com.wast3dmynd.tillr.boundary.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import java.util.concurrent.ExecutionException;

public class DashBoardFragment extends Fragment {
    private MainActivityListener listener;

    //region views
    private TextView dashboard_date, dashboard_time, dashboard_sold, dashboard_stock, dashboard_orders;
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
        CoordinatorLayout dashboard_main = view.findViewById(R.id.dashboard_main);
        dashboard_summary = view.findViewById(R.id.dashboard_summary);
        dashboard_date = view.findViewById(R.id.dashboard_date);
        dashboard_time = view.findViewById(R.id.dashboard_time);
        dashboard_sold = view.findViewById(R.id.dashboard_sold);
        dashboard_stock = view.findViewById(R.id.dashboard_stock);
        dashboard_orders = view.findViewById(R.id.dashboard_orders);
        //endregion

        //region process dashboard summary
        dashboard_summary.setVisibility(View.GONE);
        final Snackbar snackbar = Snackbar.make(dashboard_main, R.string.action_dashboard_summary_processing, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //region append data to views
                try {
                    DashboardData dashboard = new ProcessDashboardSummary(getContext()).execute().get();
                    if (dashboard == null) {
                        dashboard_summary.setVisibility(View.GONE);
                        snackbar.setText(R.string.action_dashboard_summary_empty);
                        snackbar.dismiss();
                        return;
                    }

                    dashboard_summary.setVisibility(dashboard == null ? View.GONE : View.VISIBLE);
                    if (dashboard == null) {
                        snackbar.setText(R.string.action_dashboard_summary_interrupted);
                        dashboard_summary.setVisibility(View.GONE);
                        snackbar.dismiss();
                        return;
                    }
                    snackbar.setText(R.string.action_dashboard_summary_done);
                    //region append dashboardData

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
                    //endregion
                    snackbar.dismiss();
                } catch (InterruptedException e) {
                    snackbar.setText(R.string.action_dashboard_summary_interrupted);
                    e.printStackTrace();
                    dashboard_summary.setVisibility(View.GONE);
                    snackbar.dismiss();
                } catch (ExecutionException e) {
                    snackbar.setText(R.string.action_dashboard_summary_error);
                    e.printStackTrace();
                    dashboard_summary.setVisibility(View.GONE);
                    snackbar.dismiss();
                }
                //endregion
            }
        }, 5000);
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

    private static class ProcessDashboardSummary extends AsyncTask<Void, Void, DashboardData> {

        private Context context;

        ProcessDashboardSummary(Context context) {
            this.context = context;
        }

        @Override
        protected DashboardData doInBackground(Void... params) {

            DashboardData dashboard = new DashboardData();

            ArrayList<Object> orderObjects = new OrderDatabase(context).getItems();
            if (orderObjects.isEmpty()) return null;

            ArrayList<Order> orders = new ArrayList<>(orderObjects.size());
            for (Object object : orderObjects) orders.add((Order) object);

            ArrayList<Object> itemObjects = new ItemDatabase(context).getItems();
            ArrayList<Item> items = new ArrayList<>(itemObjects.size());
            for (Object object : itemObjects) items.add((Item) object);

            ArrayList<Order.Timeline> timeLines = Order.OrderTimelineHelper.get(context);
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
}
