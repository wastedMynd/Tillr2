package com.wast3dmynd.tillr.boundary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.view_holder.ViewOrderViewHolder;
import com.wast3dmynd.tillr.boundary.view_holder.ViewOrdersViewHolder;
import com.wast3dmynd.tillr.database.OrderDatabase;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.Order;

import java.util.ArrayList;

public class ViewOrderAdapter extends RecyclerView.Adapter<ViewOrderViewHolder> implements ViewOrderViewHolder.ViewOrderViewHolderListener {

    private ArrayList<Item> items;
    private LayoutInflater layoutInflater;

    public ViewOrderAdapter(Context context, Order order) {

        layoutInflater = LayoutInflater.from(context);
        items = order.getItems();
    }

    @NonNull
    @Override
    public ViewOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.layout_view_order_item, parent, false);
        return new ViewOrderViewHolder(this, itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewOrderViewHolder holder, int position) {
        holder.onBind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return getItems().size();
    }

    private Item  getItem(int position) {
        return getItems().get(position);
    }

    private ArrayList<Item> getItems() {
        return items;
    }

    @Override
    public void onViewOrderItemSelected(int itemPosition, Item item) {
        getItems().remove(itemPosition);
        getItems().add(itemPosition,item);
        notifyItemChanged(itemPosition);
    }
}