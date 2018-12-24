package com.wast3dmynd.tillr.boundary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.fragments.ItemUnitsInStockFragment;
import com.wast3dmynd.tillr.boundary.views.ItemLegendViewHolder;
import com.wast3dmynd.tillr.entity.Item;

import java.util.ArrayList;

public class ItemLegendAdapter extends RecyclerView.Adapter<ItemLegendViewHolder> implements ItemUnitsInStockFragment.ItemUnitsInStockFragmentListener {

    private ArrayList<Item> rootItems;
    private ArrayList<Item> items;

    public ItemLegendAdapter(ArrayList<Item> rootItems) {
        this.rootItems = rootItems;
        items = new ArrayList<>();
        items.addAll(rootItems);
    }

    @NonNull
    @Override
    public ItemLegendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_lagend, parent, false);
        ItemLegendViewHolder viewHolder = new ItemLegendViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemLegendViewHolder holder, int position) {
        holder.onBind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private Item getItem(int position) {
        return items.get(position);
    }

    //implements ItemUnitsInStockFragment.ItemUnitsInStockFragmentListener
    @Override
    public void onItemGraphSelected(Item item, int position) {
        item.getGui().setSelected(true);

        if (!items.contains(item)) {
            if (rootItems.contains(item)) {

                rootItems.remove(position);
                rootItems.add(position, item);

                items = new ArrayList<>();
                items.addAll(rootItems);

                notifyDataSetChanged();
            }
        } else {

            items.remove(position);
            items.add(position, item);
            notifyItemChanged(position);
        }
    }


    public void filterItems(ArrayList<Item> highlightedItems, boolean filtered) {

        //highlight filtered items
        items.clear();
        if (!filtered)
            items.addAll(highlightedItems);
        else
            for (Item item : highlightedItems)
                if (item.getGui().isHighlighted()) items.add(item);

        notifyDataSetChanged();
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void queryItems(ArrayList<Item> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void reloadItems(ArrayList<Item> rootItems) {
        this.items.clear();
        this.items.addAll(rootItems);

        if(items.isEmpty())items.addAll(this.rootItems);
        notifyDataSetChanged();
    }
}
