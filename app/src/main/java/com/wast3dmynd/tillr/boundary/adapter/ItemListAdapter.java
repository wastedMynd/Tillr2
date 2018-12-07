package com.wast3dmynd.tillr.boundary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.view_holder.ItemListViewHolder;
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.entity.Item;

import java.util.ArrayList;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListViewHolder> implements ItemListViewHolder.ItemListViewHolderListener {
    private ArrayList<Item> orderItems;
    private LayoutInflater layoutInflater;
    private ItemListAdapterListener itemListAdapterListener;

    public ItemListAdapter(Context context, ItemListAdapterListener itemListAdapterListener) {
        layoutInflater = LayoutInflater.from(context);
        this.itemListAdapterListener = itemListAdapterListener;

        ItemDatabase database = new ItemDatabase(context);
        this.orderItems = new ArrayList<>();
        ArrayList<Object> objects = database.getItems();
        for (Object item : objects) orderItems.add((Item) item);
    }

    @NonNull
    @Override
    public ItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.layout_item, parent, false);
        return new ItemListViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemListViewHolder holder, int position) {
        holder.onBind(getItem(position));
    }

    private Item getItem(int position) {
        return orderItems.get(position);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    @Override
    public void onItemRemoved(Item item) {
        itemListAdapterListener.onItemDelete(item);
    }

    @Override
    public void onItemEdit(Item item) {
        itemListAdapterListener.onItemEdit(item);
        Log.d("onItemEditAdapter", item.getItemName());
    }

    /**
     * To be call only from the {@link ItemsActivity}
     **/
    public void onItemDelete(Item item) {
        int position = orderItems.indexOf(item);
        orderItems.remove(position);
        notifyItemRemoved(position);
    }

    public interface ItemListAdapterListener {
        void onItemEdit(Item item);

        void onItemDelete(Item item);
    }
}
