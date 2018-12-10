package com.wast3dmynd.tillr.boundary.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.views.PlaceOrderViewHolder;
import com.wast3dmynd.tillr.entity.Item;

import java.util.ArrayList;

public class PlaceOrderAdapter extends RecyclerView.Adapter<PlaceOrderViewHolder> {

    private ArrayList<Item> orderItems, searchOrderItemsPlaceholder;
    private PlaceOrderViewHolder.ItemMenuViewHolderListener orderListener;
    private LayoutInflater layoutInflater;


    public PlaceOrderAdapter(Context context, ArrayList<Item> orderItems, PlaceOrderViewHolder.ItemMenuViewHolderListener itemMenuViewHolderListener) {
        layoutInflater = LayoutInflater.from(context);

        //add Items
        this.orderItems = new ArrayList<>();
        this.searchOrderItemsPlaceholder = new ArrayList<>();

        for (Item item : orderItems) {
            //if item units that are remain are equal 0 don't show it.
            if (item.getItemUnitRemaining() > 0) {
                this.orderItems.add(item);
                this.searchOrderItemsPlaceholder.add(item);
            }
        }

        this.orderListener =  itemMenuViewHolderListener;
    }

    @NonNull
    @Override
    public PlaceOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_place_order, parent, false);
        return new PlaceOrderViewHolder(itemView, orderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceOrderViewHolder holder, int position) {
        holder.bindPlacedOrderItem(getOrderItem(position));
    }

    @Override
    public int getItemCount() {
        return getOrderItems().size();
    }

    private Item getOrderItem(int position) {
        return getOrderItems().get(position);
    }

    private ArrayList<Item> getOrderItems() {
        return orderItems;
    }

    public void update(Item orderItem) {

        int indexOfItem = getOrderItems().indexOf(orderItem);
        getOrderItems().remove(indexOfItem);
        getOrderItems().add(indexOfItem, orderItem);

        int indexOfSearchOrderItem = searchOrderItemsPlaceholder.indexOf(orderItem);
        searchOrderItemsPlaceholder.remove(indexOfSearchOrderItem);
        searchOrderItemsPlaceholder.add(indexOfSearchOrderItem,orderItem);

        notifyItemChanged(indexOfItem);
    }

    public void searchForItem(String searchItem) {

        orderItems.clear();
        if (!searchItem.isEmpty()) {
            for (Item item : searchOrderItemsPlaceholder) {
                String itemName = item.getItemName().toLowerCase();
                if (itemName.contains(searchItem.toLowerCase())) {
                    orderItems.add(item);
                }
            }

        } else for (Item item : searchOrderItemsPlaceholder) orderItems.add(item);


        notifyDataSetChanged();
    }


}