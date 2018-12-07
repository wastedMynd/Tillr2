package com.wast3dmynd.tillr.boundary.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.MainActivity;
import com.wast3dmynd.tillr.boundary.adapter.OrderSummaryTimelineViewAdapter;
import com.wast3dmynd.tillr.boundary.interfaces.MainActivityListener;
import com.wast3dmynd.tillr.boundary.view_holder.OrderSummaryTimelineViewHolder;
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.Order;
import com.wast3dmynd.tillr.utils.CurrencyUtility;
import com.wast3dmynd.tillr.utils.DateFormats;
import com.wast3dmynd.tillr.utils.DayFormats;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class OrdersFragment extends Fragment {
    private MainActivityListener listener;

    private TextView timeline_orders_placed,
            timeline_date,
            timeline_stock,
            timeline_sold,
            timeline_total,
            timeline_damage,
            timeline_asserts;
    private ConstraintLayout order_summary;
    private CoordinatorLayout timeline_layout;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    public static Fragment newInstance() {
        return new OrdersFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_view_all_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        order_summary = view.findViewById(R.id.order_summary);
        timeline_layout = view.findViewById(R.id.orders_activity);
        timeline_orders_placed = view.findViewById(R.id.dashboard_time);
        timeline_date = view.findViewById(R.id.dashboard_date);
        timeline_stock = view.findViewById(R.id.items_units);
        timeline_sold = view.findViewById(R.id.timeline_sold);
        timeline_total = view.findViewById(R.id.timeline_total);
        timeline_damage = view.findViewById(R.id.timeline_damage);
        timeline_asserts = view.findViewById(R.id.timeline_asserts);


        //Toolbar toolbar = view.findViewById(R.id.toolbar);
        //((MainActivity) getActivity()).setSupportActionBar(toolbar);
        //((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.title_activity_view_all_orders);
        setHasOptionsMenu(true);

        order_summary.setVisibility(View.GONE);
        final MainActivity thisActivity = (MainActivity) getActivity();
        mSectionsPagerAdapter = new SectionsPagerAdapter(thisActivity.getSupportFragmentManager(),
                new PlaceholderFragment.PlaceholderFragmentListener() {
            @Override
            public void onShowOrderDetailsOptionSelected(Order order) {
               listener.onFragmentChanged(ViewOrderFragment.newInstance(order));
            }

            @Override
            public void onOrderSummaryRequest(Order order, boolean isOrderSummaryRequested) {

                if(!isOrderSummaryRequested)
                {
                    order_summary.setVisibility(View.GONE);
                    return;
                }

                if (!order.isLastOrder()) return;


                ProcessOrderSummaryRequest request = new ProcessOrderSummaryRequest(thisActivity);
                request.execute(order);

                final Snackbar snackbar = Snackbar.make(timeline_layout, R.string.action_order_summary_processing, Snackbar.LENGTH_INDEFINITE);
                try {
                    final Order.TimelineData timelineData = request.get();
                    snackbar.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            snackbar.setText(R.string.action_order_summary_done);

                            //display order timeline date
                            DateFormats dateFormats = DateFormats.Day_Month_Year;
                            String dateStamp = DateFormats.getSimpleDateString(timelineData.getDate(), dateFormats);
                            timeline_date.setText(dateStamp);

                            //display order timeline order's placed
                            timeline_orders_placed.setText(String.valueOf(timelineData.getOrdersPlaced()));

                            //display order timeline stock
                            timeline_stock.setText(String.valueOf(timelineData.getRemainingUnits()));

                            //display order timeline sold
                            timeline_sold.setText(String.valueOf(timelineData.getSoldUnits()));

                            //display order timeline total
                            timeline_total.setText(CurrencyUtility.getCurrencyDisplay(timelineData.getGrandTotal()));

                            //display order timeline damage
                            timeline_damage.setText(CurrencyUtility.getCurrencyDisplay(timelineData.getDamageTotal()));

                            //display order timeline asserts
                            timeline_asserts.setText(CurrencyUtility.getCurrencyDisplay(timelineData.getAssertTotal()));

                            order_summary.setVisibility(View.VISIBLE);
                            snackbar.dismiss();
                        }
                    }, 1000);
                } catch (InterruptedException e) {
                    snackbar.setText(R.string.action_order_summary_interrupted);
                    e.printStackTrace();
                    snackbar.dismiss();
                } catch (ExecutionException e) {
                    snackbar.setText(R.string.action_order_summary_error);
                    e.printStackTrace();
                    snackbar.dismiss();
                }
            }

        });

        // Set up the ViewPager with the sections adapter.
        mViewPager = view.findViewById(R.id.cont);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        mViewPager.setCurrentItem(DayFormats.getTodaysFormat().getDayNumber() - 1, true);
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MainActivityListener))
            throw new ClassCastException("Must implement MainActivityListener");
        this.listener = (MainActivityListener) context;
    }


    private static class ProcessOrderSummaryRequest extends AsyncTask<Order, Void, Order.TimelineData> {

        private ArrayList<Order.Timeline> timeLines;
        private Context context;


        ProcessOrderSummaryRequest(Context context) {
            this.context = context;
        }

        @Override
        protected Order.TimelineData doInBackground(Order... orders) {

            timeLines = Order.OrderTimelineHelper.get(context);

            ArrayList<Item> items = new ArrayList<>();
            ArrayList<Object> itemObjects = new ItemDatabase(context).getItems();
            for(Object itemObject : itemObjects)items.add((Item)itemObject);

            Order.TimelineData timelineData = new Order.TimelineData();
            Order order = orders[0];

            //data
            long date = order.getDate();
            int ordersPlaced = 0;
            int remainingUnits = 0;
            int soldUnits = 0;
            double grandTotal = 0.00;
            double assertTotal = 0.00;

            for (Order.Timeline timeline : timeLines) {
                if (!order.equals(timeline.getLastPlacedOrder())) continue;

                ordersPlaced += timeline.getChildOrders().size() + 1;
                grandTotal += timeline.getLastPlacedOrder().getTotal();

                for (Item item : timeline.getLastPlacedOrder().getItems()) soldUnits += item.getItemUnits();

                for (Order childOrder : timeline.getChildOrders()) {
                    grandTotal += childOrder.getTotal();
                    for (Item item : childOrder.getItems()) soldUnits += item.getItemUnits();
                }
            }

            for(Item item : items){
                remainingUnits += item.getItemUnitRemaining();
                assertTotal += (item.getItemUnitRemaining() * item.getItemCostPerUnit());
            }

            timelineData.setOrdersPlaced(ordersPlaced);
            timelineData.setDate(date);
            timelineData.setRemainingUnits(remainingUnits);
            timelineData.setSoldUnits(soldUnits);
            timelineData.setGrandTotal(grandTotal);
            timelineData.setAssertTotal(assertTotal);

            return timelineData;
        }


    }


    public static class PlaceholderFragment extends Fragment implements
            OrderSummaryTimelineViewAdapter.OrderSummaryTimelineViewAdapterListener {
        private static final String ARG_DAY = "arg_day";
        private static final String ARG_LISTENER = "arg_listener";

        private PlaceholderFragmentListener listener;
        private DayFormats dayFormat;

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(DayFormats dayFormats, PlaceholderFragmentListener placeholderFragmentListener) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putSerializable(ARG_DAY, dayFormats);
            args.putSerializable(ARG_LISTENER, placeholderFragmentListener);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_view_all_orders_summary, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            ArrayList<Order.Timeline> timeLines = Order.OrderTimelineHelper.get(getContext());
            ArrayList<Order> orders = new ArrayList<>();
            for (Order.Timeline timeline : timeLines) {
                DayFormats mDayFormat = DayFormats.getDayFormat(timeline.getLastPlacedOrder().getDate());
                if (!mDayFormat.equals(dayFormat)) continue;
                orders.add(timeline.getLastPlacedOrder());
                orders.addAll(timeline.getChildOrders());
            }

            RecyclerView recyclerView = view.findViewById(R.id.items);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            OrderSummaryTimelineViewAdapter adapter = new OrderSummaryTimelineViewAdapter(this, orders);
            adapter.summarizeDaysOrders(DayFormats.getTodaysFormat(),true);
            recyclerView.setAdapter(adapter);
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            dayFormat = (DayFormats) getArguments().get(ARG_DAY);
            listener = (PlaceholderFragmentListener) getArguments().get(ARG_LISTENER);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            listener = null;
        }

        //region OrderSummaryTimelineViewAdapterListener implements
        @Override
        public void onShowOrderDetailsOptionSelected(Order order) {
            listener.onShowOrderDetailsOptionSelected(order);
        }

        @Override
        public void onOrderSummaryRequest(Order order, boolean isOrderSummaryRequested) {
            listener.onOrderSummaryRequest(order, isOrderSummaryRequested);
        }
        //endregion

        interface PlaceholderFragmentListener extends OrderSummaryTimelineViewHolder.OrderSummaryTimelineListener {
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private PlaceholderFragment.PlaceholderFragmentListener placeholderFragmentListener;

        public SectionsPagerAdapter(FragmentManager fm, PlaceholderFragment.PlaceholderFragmentListener placeholderFragmentListener) {
            super(fm);
            this.placeholderFragmentListener = placeholderFragmentListener;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(DayFormats.values()[position], placeholderFragmentListener);
        }

        @Override
        public int getCount() {
            // Show total pages(depends on order date).
            return DayFormats.values().length;
        }
    }

}
