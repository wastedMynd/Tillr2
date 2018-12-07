package com.wast3dmynd.tillr.boundary.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.adapter.ItemListAdapter;
import com.wast3dmynd.tillr.boundary.interfaces.MainActivityListener;
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.utils.CurrencyUtility;
import com.wast3dmynd.tillr.utils.DateFormats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ItemsFragment extends Fragment implements ItemListAdapter.ItemListAdapterListener {

    private ItemListAdapter itemListAdapter;
    private TextView items_last_update_date, items_count, items_units, items_asserts;

    private MainActivityListener listener;

    @NonNull
    public static Fragment newItemListIntent() {
        return new ItemsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_item_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        items_last_update_date = view.findViewById(R.id.dashboard_date);
        items_count = view.findViewById(R.id.dashboard_time);
        items_units = view.findViewById(R.id.items_units);
        items_asserts = view.findViewById(R.id.items_asserts);

        //region Items Summary
        List<Item> items = new ArrayList<>();
        List<Date> dates = new ArrayList<>();

        ArrayList<Object> objects = new ItemDatabase(getContext()).getItems();
        //provide list with data
        for (Object object : objects) {
            Item item = (Item) object;
            items.add(item);
            dates.add(new Date(item.getItemTimeStamp()));
        }

        //region Get last Item update date
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
        //format it for display
        DateFormats dateFormats = DateFormats.Day_Month_Year;
        String dateStamp = DateFormats.getSimpleDateString(closest, dateFormats);
        items_last_update_date.setText(dateStamp);
        //endregion

        //items present
        items_count.setText(String.valueOf(items.size()));

        //units present
        int units = 0;
        for (Item item : items) units += item.getItemUnitRemaining();
        items_units.setText(String.valueOf(units));

        //asserts on hold
        double asserts = 0.00;
        for (Item item : items)
            asserts += (item.getItemUnitRemaining() * item.getItemCostPerUnit());
        items_asserts.setText(CurrencyUtility.getCurrencyDisplay(asserts));
        //endregion

        //region link views to activity_place_order
        RecyclerView itemListRecycler = view.findViewById(R.id.rcyclAddItems);
        itemListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        FloatingActionButton fabInsertItem = view.findViewById(R.id.fabAddItem);
        //endregion

        //region placeOrderItemAdapter init
        itemListAdapter = new ItemListAdapter(getContext(), this);
        itemListRecycler.setAdapter(itemListAdapter);
        //endregion

        //region View EventHandler config
        View.OnClickListener fabOrderCheckoutOnClickListener;
        fabOrderCheckoutOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFragmentChanged(EditItemFragment.newItemEditorIntent(EditItemFragment.ItemEditorOptions.CREATE_NEW_ITEM, new Item()));
            }
        };
        fabInsertItem.setOnClickListener(fabOrderCheckoutOnClickListener);
        //endregion
    }

    //region implements ItemListAdapter.ItemListAdapterListener
    @Override
    public void onItemEdit(final Item item) {
        String itemEdit = "Edit " + item.getItemName() + " ?";
        Snackbar.make(getView().findViewById(R.id.viewItems), itemEdit, Snackbar.LENGTH_LONG)
                .setAction("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onFragmentChanged(EditItemFragment.newItemEditorIntent(EditItemFragment.ItemEditorOptions.EDIT_ITEM, item));
                    }
                })
                .show();
    }

    @Override
    public void onItemDelete(final Item item) {

        StringBuilder stringBuilder = new StringBuilder("Delete Item: ");
        stringBuilder.append(item.getItemName());
        stringBuilder.append(" ?");

        Snackbar.make(getView().findViewById(R.id.viewItems), stringBuilder.toString(), Snackbar.LENGTH_LONG)
                .setAction("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean removed = new ItemDatabase(v.getContext()).removeItem(item);
                        if (removed) itemListAdapter.onItemDelete(item);
                        String message = removed ? "Item is removed" : "Item not removed!";
                        Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
    //endregion


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MainActivityListener))
            throw new ClassCastException("Must implement ItemsFragmentListener");
        listener = (MainActivityListener) context;
    }


}
