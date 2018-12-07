package com.wast3dmynd.tillr.boundary.view_holder;

import android.graphics.PorterDuff;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.utils.CurrencyUtility;


public class OrderMenuItemViewHolder extends RecyclerView.ViewHolder {

    //Views from item_place_oder layout
    private ImageView itemAdder;
    private TextView itemName, itemCostPerUnit, itemUnitRemaining;
    private LinearLayout lyrItemStatus;
    private CardView cdItem;

    //region constructor view initialize
    public OrderMenuItemViewHolder(View itemView, ItemMenuViewHolderListener itemMenuViewHolderListener) {
        super(itemView);
        this.itemMenuViewHolderListener = itemMenuViewHolderListener;
        initializeViews(itemView);
    }


    private void initializeViews(View view) {
        //region view init
        itemAdder = view.findViewById(R.id.imgBtnItemIncrement);
        itemName = view.findViewById(R.id.itemName);
        itemCostPerUnit = view.findViewById(R.id.itemCostPerUnit);
        itemUnitRemaining = view.findViewById(R.id.itemUnitsRemaining);
        lyrItemStatus = view.findViewById(R.id.lyrItemStatus);
        cdItem = view.findViewById(R.id.cdItem);
        //endregion
    }
    //endregion

    private void displayItemCount(Item item) {
        //itemUnitRemaining TextView update
        String itemUnitRemainingTxtPostfix = item.getItemUnitRemaining() > 1 ? "s" : "";
        StringBuilder itemsUnitRemainingB = new StringBuilder(String.valueOf(item.getItemUnits()));
        itemsUnitRemainingB.append(" of ");
        itemsUnitRemainingB.append(item.getItemUnitRemaining());
        itemsUnitRemainingB.append(" unit");
        itemsUnitRemainingB.append(itemUnitRemainingTxtPostfix);
        itemUnitRemaining.setText(itemsUnitRemainingB.toString());
    }

    private void displayIndicates(Item item) {

        lyrItemStatus.setBackgroundColor(item.getItemUnits() > 0 ? itemAdder.getResources().getColor(R.color.colorAdderAccent) :
                itemAdder.getResources().getColor(android.R.color.white));

        Item.ItemGui.MenuItemMode menuItemMode = item.getGui().getMenuItemMode();

        switch (menuItemMode) {
            case INCREMENT:
                //change button to increment icon
                itemAdder.setImageResource(R.drawable.ic_item_add);
                itemAdder.setColorFilter(itemAdder.getResources().getColor(R.color.colorAdderAccent), PorterDuff.Mode.SRC_ATOP);
                break;

            case DECREMENT:
                //change button to decrement icon
                itemAdder.setImageResource(R.drawable.ic_item_remove);
                itemAdder.setColorFilter(itemAdder.getResources().getColor(R.color.colorRemoverAccent), PorterDuff.Mode.SRC_ATOP);
                break;
        }
    }


    public void bindPlacedOrderItem(final Item item) {

        //validate if layout_menu_item must be enabled
        boolean enabled = item.getItemUnitRemaining() > 0;
        itemAdder.setEnabled(enabled);
        cdItem.setEnabled(enabled);
        itemAdder.setVisibility(enabled ? View.VISIBLE : View.GONE);

        //itemName TextView update
        itemName.setText(item.getItemName());

        //itemCostPerUnit TextView update
        StringBuilder itemCostPerUnitB = new StringBuilder(CurrencyUtility.getCurrencyDisplay(item.getItemCostPerUnit()));
        itemCostPerUnitB.append("/unit");
        itemCostPerUnit.setText(itemCostPerUnitB.toString());

        displayItemCount(item);

        displayIndicates(item);

        //region itemAdder onClick actions

        itemAdder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (item.getGui().getMenuItemMode()) {
                    case INCREMENT:
                        if (item.getItemUnits() < item.getItemUnitRemaining())
                            item.setItemUnits(item.getItemUnits() + 1);
                        else
                        {
                            itemAdder.performLongClick();
                            item.setItemUnits(item.getItemUnits() - 1);
                        }
                        break;
                    case DECREMENT:
                        if (item.getItemUnits() > 0 && item.getItemUnits() < item.getItemUnitRemaining())
                            item.setItemUnits(item.getItemUnits() - 1);
                        else {
                            itemAdder.performLongClick();
                            item.setItemUnits(item.getItemUnits() + 1);
                        }
                        break;
                }

                displayItemCount(item);

                //(Test)
                itemMenuViewHolderListener.onOrderMenuItemChanged(item);
            }
        });

        itemAdder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Item.ItemGui.MenuItemMode menuItemMode = item.getGui().getMenuItemMode() == Item.ItemGui.MenuItemMode.INCREMENT ? Item.ItemGui.MenuItemMode.DECREMENT :
                        Item.ItemGui.MenuItemMode.INCREMENT;

                item.getGui().setMenuItemMode(menuItemMode);

                switch (menuItemMode) {
                    case INCREMENT:
                        //change button to increment icon
                        itemAdder.setImageResource(R.drawable.ic_add_black_24dp);
                        itemAdder.setColorFilter(itemAdder.getResources().getColor(R.color.colorAdderAccent), PorterDuff.Mode.SRC_ATOP);
                        return true;

                    case DECREMENT:
                        //change button to decrement icon
                        itemAdder.setImageResource(R.drawable.ic_remove_black_24dp);
                        itemAdder.setColorFilter(itemAdder.getResources().getColor(R.color.colorRemoverAccent), PorterDuff.Mode.SRC_ATOP);
                        return true;
                }
                return false;
            }
        });
        //endregion

    }




    //region itemOrder to place_order_summary inter-communication via ItemMenuViewHolderListener
    private ItemMenuViewHolderListener itemMenuViewHolderListener;

    public interface ItemMenuViewHolderListener {
        void onOrderMenuItemChanged(Item orderItem);
    }
    //endregion
}
