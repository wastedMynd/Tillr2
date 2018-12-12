package com.wast3dmynd.tillr.boundary.adapter;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.views.EditItemViewHolder;
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.entity.Item;

import java.util.ArrayList;

public class EditItemAdapter extends RecyclerView.Adapter<EditItemViewHolder> {

    private EditItemAdapterListener editItemAdapterListener;
    private ArrayList<Item> items, searchOrderItemsPlaceholder;


    public EditItemAdapter( ArrayList<Item> orderItems, EditItemAdapterListener editItemAdapterListener) {

        //add Items
        this.items = new ArrayList<>();
        this.searchOrderItemsPlaceholder = new ArrayList<>();

        for (Item item : orderItems) {
            //if item units that are remain are equal 0 don't show it.
            if (item.getItemUnitRemaining() > 0) {
                this.items.add(item);
                this.searchOrderItemsPlaceholder.add(item);
            }
        }

        this.editItemAdapterListener = editItemAdapterListener;
    }

    @NonNull
    @Override
    public EditItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_editor, parent, false);
        return new EditItemViewHolder(itemView, new EditItemViewHolder.EditItemViewHolderListener() {

            ItemDatabase database = new ItemDatabase(parent.getContext());

            private void updateItems(Item item) {
                int indexOfItem = items.indexOf(item);
                items.remove(indexOfItem);
                items.add(indexOfItem, item);
                notifyItemChanged(indexOfItem);
            }

            @Override
            public void onItemSelected(Item item) {
                updateItems(item);
                editItemAdapterListener.onItemSelected(item);
            }

            @Override
            public void onItemHighlighted(Item item) {
                updateItems(item);
            }

            @Override
            public void onItemDeleted(Item item) {
                int indexOfItem = items.indexOf(item);
                items.remove(indexOfItem);
                notifyItemChanged(indexOfItem);

                database.removeItem(item);
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull EditItemViewHolder holder, int position) {
        holder.onBind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return getItems().size();
    }

    private Item getItem(int position) {
        return getItems().get(position);
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItem(Item item) {
        if(item==null)return;
        int indexOfItem = getItems().indexOf(item);
        if(indexOfItem < 0)return;
        items.remove(indexOfItem);
        items.add(indexOfItem,item);
        notifyItemChanged(indexOfItem);
    }

    public void addItem(Item item) {
        if(!items.contains(item)) {
            items.add(item);
            notifyDataSetChanged();
        }else {
            int indexOfItem = items.indexOf(item);
            items.remove(indexOfItem);
            items.add(indexOfItem,item);
            notifyItemChanged(indexOfItem);
        }
    }

    public interface EditItemAdapterListener{
        void onItemSelected(Item item);
    }
}