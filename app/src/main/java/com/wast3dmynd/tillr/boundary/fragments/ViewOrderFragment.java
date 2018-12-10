package com.wast3dmynd.tillr.boundary.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.MainActivity;
import com.wast3dmynd.tillr.boundary.adapter.ViewOrderAdapter;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.Order;
import com.wast3dmynd.tillr.utils.CurrencyUtility;
import com.wast3dmynd.tillr.utils.DateFormats;

import java.util.ArrayList;

public class ViewOrderFragment extends Fragment {

    private static final String ARG_VIEW_ORDER = "ARG_VIEW_ORDER";

    public static Fragment newInstance(Order order) {
        Fragment fragment = new ViewOrderFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_VIEW_ORDER, order);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_order,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_view_order);

        //get passed order
        Order order = (Order) getArguments().get(ARG_VIEW_ORDER);

        if (order == null) return;


        //OrderNumber
        TextView orderNumber = view.findViewById(R.id.dashboard_date);
        orderNumber.setText(String.valueOf(order.getId()));

        //OrderDate
        DateFormats dateFormats = DateFormats.Day_Month_Year;
        String dateStamp = DateFormats.getSimpleDateString(order.getDate(), dateFormats);
        TextView orderDate = view.findViewById(R.id.order_date);
        orderDate.setText(dateStamp);

        //OrderTime
        DateFormats timeFormats = DateFormats.Hours_Minutes;
        String timeStamp = DateFormats.getSimpleDateString(order.getTimeStamp(), timeFormats);
        TextView orderTime = view.findViewById(R.id.items_units);
        orderTime.setText(timeStamp);

        //OrderUnits
        ArrayList<Item> items = order.getItems();
        TextView orderItems = view.findViewById(R.id.order_items);
        orderItems.setText(String.valueOf(items.size()));
        int units = 0;
        for (Item item : items) units += item.getItemUnits();
        TextView orderUnits = view.findViewById(R.id.order_units);
        orderUnits.setText(String.valueOf(units));

        //OrderTotal
        TextView orderTotal = view.findViewById(R.id.order_total);
        orderTotal.setText(CurrencyUtility.getCurrencyDisplay(order.getTotal()));

        //link Adapter
        RecyclerView recyclerView = view.findViewById(R.id.itemRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        ViewOrderAdapter adapter = new ViewOrderAdapter(getContext(), order);
        recyclerView.setAdapter(adapter);
    }

}
