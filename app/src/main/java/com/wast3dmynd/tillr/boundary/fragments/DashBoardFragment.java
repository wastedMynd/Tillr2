package com.wast3dmynd.tillr.boundary.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.MainActivity;
import com.wast3dmynd.tillr.boundary.adapter.GraphViewAdapter;
import com.wast3dmynd.tillr.boundary.interfaces.MainActivityListener;
import com.wast3dmynd.tillr.boundary.views.ContentViewHolder;
import com.wast3dmynd.tillr.database.InventoryDatabase;
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.database.OrderDatabase;
import com.wast3dmynd.tillr.entity.GraphDataHolder;
import com.wast3dmynd.tillr.entity.InventoryData;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.Order;
import com.wast3dmynd.tillr.utils.ColorGenerator;
import com.wast3dmynd.tillr.utils.CrossFadeUtils;
import com.wast3dmynd.tillr.utils.DateFormats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DashBoardFragment extends Fragment {
    private static final int DASH_BOARD_CONTENT_LOADER_ID = 1;
    private MainActivityListener listener;

    //region views
    private TextView dashboard_date, dashboard_time, dashboard_sold, dashboard_stock, dashboard_orders;
    CoordinatorLayout dashboard_main;
    private ConstraintLayout dashboard_summary;

    private GraphViewAdapter graphViewAdapter = null;


    //<include layout="@layout/layout_content_recycler"/>
    private CrossFadeUtils crossFadeUtils;
    private ContentViewHolder holder;
    private RecyclerView recyclerView;

    //endregion

    public static Fragment newInstance() {
        return new DashBoardFragment();
    }

    //region life cycle
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        holder = new ContentViewHolder(view);
        crossFadeUtils = new CrossFadeUtils(holder.contentRecycler, holder.contentLoader);
        holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        return view;
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

        recyclerView = view.findViewById(R.id.content_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(graphViewAdapter);
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

    //region control logic
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

    }
    //endregion

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

            if (data == null) {
                Toast.makeText(getContext(), R.string.action_redirect_fragment_content_missing, Toast.LENGTH_LONG).show();
                listener.onFragmentChanged(PlaceOrderFragment.newInstance(getContext()));
                return;
            }

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

            //stats
            new ProcessInventoryDataAsync(getContext()).execute();
        }

        @Override
        public void onLoaderReset(@NonNull Loader loader) {

        }
    };

    private class ProcessInventoryDataAsync extends AsyncTask<Void, Void, Inventory> {

        private Context context;

        public ProcessInventoryDataAsync(@NonNull Context context) {
            this.context = context;
        }


        @Override
        protected Inventory doInBackground(Void... voids) {
            Inventory holder = new Inventory();
            holder.setDatabaseItemHolder(new ItemDatabase(getContext()).getAll());
            holder.setDatabaseOrderHolder(new OrderDatabase(getContext()).getAll());
            holder.setTimelineDataHolder(Order.OrderTimelineHelper.get(context));
            holder.setInventoryDataHolder(new InventoryDatabase(context).getAll());
            return holder;
        }

        @Override
        protected void onPostExecute(Inventory inventory) {
            super.onPostExecute(inventory);
            new DashboardGraphDataAsyncTask().execute(inventory);
        }
    }

    private static class Inventory {
        ArrayList<Item> databaseItemHolder;
        ArrayList<Order> databaseOrderHolder;
        ArrayList<Order.Timeline> timelineHolder;
        ArrayList<InventoryData> inventoryDataHolder;

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

        public ArrayList<Order.Timeline> getTimelineHolder() {
            return timelineHolder;
        }

        public void setTimelineHolder(ArrayList<Order.Timeline> timelineHolder) {
            this.timelineHolder = timelineHolder;
        }

        public ArrayList<InventoryData> getInventoryDataHolder() {
            return inventoryDataHolder;
        }

        public void setInventoryDataHolder(ArrayList<InventoryData> inventoryDataHolder) {
            this.inventoryDataHolder = inventoryDataHolder;
        }


        //endregion
    }

    private class DashboardGraphDataAsyncTask extends AsyncTask<Inventory, Void, ArrayList<GraphDataHolder>> {

        private Inventory inventory;

        private Random random;

        public DashboardGraphDataAsyncTask() {
            this.random = new Random(1);
        }

        private GraphDataHolder getSales() {

            //region Sales
            LineGraphSeries<DataPoint> sales = new LineGraphSeries<>();

            final int MAX_DATA_POINTS = inventory.getTimelineDataHolder().size();
            int maxY = 0;
            long end = System.currentTimeMillis();
            long start = inventory.getTimelineDataHolder().get(MAX_DATA_POINTS - 1).getLastPlacedOrder().getTimeStamp();

            //remember timeline contains the last placed order until - the first placed order
            //so we need to cycle backwards
            for (int index = MAX_DATA_POINTS - 1; index > -1; index--) {
                int sold = 0;
                Order.Timeline timeline = inventory.getTimelineDataHolder().get(index);
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
            StringBuilder salesTitleBuilder = new StringBuilder(getContext().getResources().getString(R.string.label_sales));
            salesTitleBuilder.append("\n");
            salesTitleBuilder.append(startDate);
            salesTitleBuilder.append(" - ");
            salesTitleBuilder.append(endDate);

            sales.setTitle(salesTitleBuilder.toString());
            sales.setAnimated(true);
            sales.setColor(Color.GREEN);
            sales.setDrawDataPoints(true);
            sales.setDataPointsRadius(6);
            sales.setThickness(2);

            sales.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {

                    String date = DateFormats.getSimpleDateString((long) dataPoint.getX(), DateFormats.DayName_Day_Month_Year);
                    StringBuilder msg = new StringBuilder(date);
                    msg.append("\nSold: ");
                    msg.append(String.valueOf((int) dataPoint.getY()));
                    msg.append(" unit");
                    msg.append(dataPoint.getY() > 1 ? "s" : "");
                    Toast.makeText(getContext(), msg.toString(), Toast.LENGTH_LONG).show();
                }
            });


            GraphDataHolder salesDataHolder = new GraphDataHolder();
            salesDataHolder.setTitle(salesTitleBuilder.toString());
            salesDataHolder.setYLabel(getContext().getResources().getString(R.string.label_units));
            salesDataHolder.setXLabel(getContext().getResources().getString(R.string.label_date));
            salesDataHolder.setMinX(start);
            salesDataHolder.setMaxX(end);
            salesDataHolder.setMinY(0);
            salesDataHolder.setMaxY(maxY + 2);
            salesDataHolder.setDataPoints(sales);

            return salesDataHolder;
        }

        private GraphDataHolder getStockPerItem() {

            //region StockPerItem

            final ArrayList<Item> items = new ItemDatabase(getContext()).getAll();

            GraphDataHolder graphDataHolder = new GraphDataHolder();

            int maxX = 0, maxY = 0;

            int index = 0;

            int maxDataPoints = items.size();


            //sort items according to date
            Collections.sort(items, new Comparator<Item>() {
                @Override
                public int compare(Item item1, Item item2) {

                    Date date1 = new Date(item1.getItemTimeStamp());
                    Date date2 = new Date(item2.getItemTimeStamp());

                    return date1.compareTo(date2);
                }
            });

            graphDataHolder.setColectionOfDataPoints(new ArrayList<BaseSeries<DataPoint>>());

            BarGraphSeries<DataPoint> dataPointBarGraphSeries = new BarGraphSeries<>();

            final ArrayList<Integer> colors = new ArrayList<>();
            final ArrayList<Item> temp = new ArrayList<>();
            for (final Item i : items) {
                i.getGui().setColor(ColorGenerator.generateColor());
                temp.add(i);
                colors.add(i.getGui().getColor());
            }

            items.clear();
            items.addAll(temp);

            for (final Item item : items) {


                //region dataPointBarGraphSeries append DataPoints

                int remainingItems = item.getItemUnitRemaining();

                maxY = remainingItems > maxY ? remainingItems : maxY;

                remainingItems = remainingItems > 0 ? remainingItems : -5;

                DataPoint dataPoint = new DataPoint(index, remainingItems);

                dataPointBarGraphSeries.appendData(dataPoint, true, maxDataPoints);

                index++;

                maxX++;

                //endregion

                //region dataPointBarGraphSeries onTapListener
                dataPointBarGraphSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series, DataPointInterface dataPoint) {

                        Item item = items.get((int) dataPoint.getX());

                        long timestamp = item.getItemTimeStamp();
                        DateFormats dateFormats = DateFormats.DayName_Day_Month_Year;
                        String date = DateFormats.getSimpleDateString(timestamp, dateFormats);

                        int soldUnits = item.getItemUnits();
                        int remainingUnits = item.getItemUnitRemaining();

                        StringBuilder msg = new StringBuilder(date);
                        msg.append("\n");
                        msg.append(item.getItemName());

                        msg.append("\nRemains: ");
                        msg.append(String.valueOf(remainingUnits));
                        msg.append(" unit");
                        msg.append(remainingUnits > 1 ? "s" : "");

                        msg.append("\nSold: ");
                        msg.append(String.valueOf(soldUnits));
                        msg.append(" unit");
                        msg.append(soldUnits > 1 ? "s" : "");

                        Toast.makeText(getContext(), msg.toString(), Toast.LENGTH_LONG).show();
                    }
                });
                //endregion

                //region dataPointBarGraphSeries styling
                dataPointBarGraphSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                    @Override
                    public int get(DataPoint data) {
                        return colors.get((int) (data.getX()));
                    }
                });

                //dataPointBarGraphSeries.setColor(color);
                dataPointBarGraphSeries.setDrawValuesOnTop(true);
                dataPointBarGraphSeries.setTitle(item.getItemName());
                //int colorAccent = getContext().getResources().getColor(R.color.colorAccent);
                //dataPointBarGraphSeries.setValuesOnTopColor(colorAccent);
                //dataPointBarGraphSeries.setAnimated(true);
                dataPointBarGraphSeries.setSpacing(0);
                dataPointBarGraphSeries.setDataWidth(0.25d);

                //this seems to cause a null pointer exception at at com.jjoe64.graphview.series.BarGraphSeries.draw(BarGraphSeries.java:304)
                //dataPointBarGraphSeries.setAnimated(true);

                //endregion
                graphDataHolder.setDataPoints(dataPointBarGraphSeries);
            }

            graphDataHolder.setMaxX(maxX);
            graphDataHolder.setMaxY(maxY + 20);
            graphDataHolder.setMinX(0);
            graphDataHolder.setMinY(-10);


            long lastOrderTimestamp = items.get(items.size() - 1).getItemTimeStamp();
            long startTimestamp = items.get(0).getItemTimeStamp();

            DateFormats dateFormats = DateFormats.Day_Month_Year;
            String startDate = DateFormats.getSimpleDateString(startTimestamp, dateFormats);
            String endDate = DateFormats.getSimpleDateString(lastOrderTimestamp, dateFormats);

            StringBuilder salesTitleBuilder = new StringBuilder(getContext().getResources().getString(R.string.label_stock_per_item));
            salesTitleBuilder.append("\n");
            salesTitleBuilder.append(startDate);
            salesTitleBuilder.append(" - ");
            salesTitleBuilder.append(endDate);
            //endregion

            graphDataHolder.setTitle(salesTitleBuilder.toString());
            graphDataHolder.setYLabel(getContext().getResources().getString(R.string.label_units));
            graphDataHolder.setXLabel(getContext().getResources().getString(R.string.label_items));
            graphDataHolder.setRootItems(items);

            return graphDataHolder;
        }

        private GraphDataHolder getModifiedStock() {
            //region Sales
            LineGraphSeries<DataPoint> stock = new LineGraphSeries<>();

            final int MAX_DATA_POINTS = inventory.getInventoryDataHolder().size();
            long minY = 0;
            long maxY = 0;
            final long maxX = inventory.getInventoryDataHolder().get(MAX_DATA_POINTS - 1).getTimestamp();
            final long minX = inventory.getInventoryDataHolder().get(0).getTimestamp();

            long prevDate = System.currentTimeMillis();
            for (InventoryData inventoryData : inventory.getInventoryDataHolder()) {

                long stockUnitCount = inventoryData.getStockUnitCount();

                stockUnitCount = stockUnitCount > 0 ? stockUnitCount : -3;

                long date = inventoryData.getTimestamp();

                maxY = stockUnitCount > maxY ? stockUnitCount : maxY;

                try {
                    stock.appendData(new DataPoint(new Date(date), stockUnitCount), true, MAX_DATA_POINTS, true);
                    prevDate = date;
                } catch (Exception e) {
                    e.printStackTrace();
                    stock.appendData(new DataPoint(new Date(prevDate), stockUnitCount), true, MAX_DATA_POINTS, true);
                }
            }


            String startDate = DateFormats.getSimpleDateString(minX, DateFormats.Day_Month_Year);
            String endDate = DateFormats.getSimpleDateString(maxX, DateFormats.Day_Month_Year);

            //region stock data
            StringBuilder stockTitleBuilder = new StringBuilder(getContext().getResources().getString(R.string.label_modified_stock));
            stockTitleBuilder.append("\n");
            stockTitleBuilder.append(startDate);
            stockTitleBuilder.append(" - ");
            stockTitleBuilder.append(endDate);
            stock.setTitle(stockTitleBuilder.toString());
            stock.setColor(Color.RED);
            stock.setDrawDataPoints(true);
            stock.setDataPointsRadius(7);
            stock.setThickness(3);
            stock.setAnimated(true);
            stock.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {

                    String date = DateFormats.getSimpleDateString((long) dataPoint.getX(), DateFormats.DayName_Day_Month_Year);
                    StringBuilder msg = new StringBuilder(date);
                    msg.append("\nModified Stock: ");
                    msg.append(String.valueOf((int) dataPoint.getY()));
                    msg.append(" unit");
                    msg.append(dataPoint.getY() > 1 ? "s" : "");
                    Toast.makeText(getContext(), msg.toString(), Toast.LENGTH_LONG).show();
                }
            });

            GraphDataHolder stockDataHolder = new GraphDataHolder();
            stockDataHolder.setTitle(stockTitleBuilder.toString());
            stockDataHolder.setYLabel(getContext().getResources().getString(R.string.label_units));
            stockDataHolder.setXLabel(getContext().getResources().getString(R.string.label_date));
            stockDataHolder.setMinX(minX);
            stockDataHolder.setMaxX(maxX);
            stockDataHolder.setMinY(-5);
            stockDataHolder.setMaxY(maxY + 5);
            stockDataHolder.setDataPoints(stock);
            //endregion
            return stockDataHolder;
        }

        @Override
        protected ArrayList<GraphDataHolder> doInBackground(Inventory... inventories) {

            ArrayList<GraphDataHolder> dataHolders = new ArrayList<>();
            if (inventories[0].getInventoryDataHolder().isEmpty()) return dataHolders;
            inventory = inventories[0];

            dataHolders.add(getStockPerItem());
            dataHolders.add(getSales());
            dataHolders.add(getModifiedStock());

            return dataHolders;
        }

        @Override
        protected void onPostExecute(ArrayList<GraphDataHolder> graphDataHolders) {
            super.onPostExecute(graphDataHolders);

            if (graphViewAdapter == null) {
                graphViewAdapter = new GraphViewAdapter(graphDataHolders);
                graphViewAdapter.setMainActivityListener((MainActivity) getActivity());
                recyclerView.setAdapter(graphViewAdapter);
            } else
                graphViewAdapter.setDataHolders(graphDataHolders);

            if (graphViewAdapter.getItemCount() == 0)
                holder.contentLoaderInfo.setText(R.string.content_loader_empty);
            else {
                holder.contentLoaderInfo.setText(R.string.content_loader_done);
                crossFadeUtils.crossfade();
            }
        }
    }

    //endregion

}
