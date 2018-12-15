package com.wast3dmynd.tillr.boundary.views;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.utils.CurrencyUtility;

public class EditItemViewHolder extends RecyclerView.ViewHolder {

    private CardView itemContainer;
    private LinearLayout lyrItemStatus;
    private TextView itemName, itemCostPerUnit, itemUnitsRemaining;
    private ImageView itemDeleteOpt;

    public EditItemViewHolder(View itemView, EditItemViewHolderListener editItemViewHolderListener) {
        super(itemView);
        itemContainer = itemView.findViewById(R.id.cdItem);
        lyrItemStatus = itemView.findViewById(R.id.lyrItemStatus);
        itemName = itemView.findViewById(R.id.itemName);
        itemCostPerUnit = itemView.findViewById(R.id.itemCostPerUnit);
        itemUnitsRemaining = itemView.findViewById(R.id.itemUnitsRemaining);
        itemDeleteOpt = itemView.findViewById(R.id.itemDeleteOpt);
        this.editItemViewHolderListener = editItemViewHolderListener;
    }

    private void displayItemName(Item item) {
        itemName.setText(item.getItemName());
    }

    private void displayItemCostPerUnit(Item item) {
        //itemCostPerUnit TextView update
        StringBuilder itemCostPerUnitB = new StringBuilder(CurrencyUtility.getCurrencyDisplay(item.getItemCostPerUnit()));
        itemCostPerUnitB.append("/unit");
        itemCostPerUnit.setText(itemCostPerUnitB.toString());
    }

    public void onBind(final Item item) {

        //itemName TextView update
        displayItemName(item);

        displayItemCostPerUnit(item);

        displayItemCount(item);

        displayIndicates(item);

        itemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.getGui().setHighlighted(!item.getGui().isHighlighted());

                lyrItemStatus.setBackgroundColor(item.getGui().isHighlighted() ? lyrItemStatus.getResources().getColor(R.color.colorAdderAccent) :
                        item.getGui().isSelected() ? lyrItemStatus.getResources().getColor(R.color.colorAccent) : lyrItemStatus.getResources().getColor(android.R.color.transparent));

                editItemViewHolderListener.onItemHighlighted(item);
            }
        });


        itemContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                item.getGui().setSelected(!item.getGui().isSelected());
                lyrItemStatus.setBackgroundColor(item.getGui().isSelected() ? lyrItemStatus.getResources().getColor(R.color.colorAccent) :
                        lyrItemStatus.getResources().getColor(android.R.color.transparent));
                editItemViewHolderListener.onItemSelected(item);
                return true;
            }
        });

        itemDeleteOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Want to delete " + item.getItemName() + " ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                editItemViewHolderListener.onItemDeleted(item);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null);
                // Create the AlertDialog object and show it
                builder.create().show();
            }
        });

        //region itemAdder onClick actions
    }

    private void displayItemCount(Item item) {
        //itemUnitRemaining TextView update
        String itemUnitRemainingTxtPostfix = item.getItemUnitRemaining() > 1 ? "s" : "";
        StringBuilder itemsUnitRemainingB = new StringBuilder();
        itemsUnitRemainingB.append(item.getItemUnitRemaining());
        itemsUnitRemainingB.append(" unit");
        itemsUnitRemainingB.append(itemUnitRemainingTxtPostfix);
        itemUnitsRemaining.setText(itemsUnitRemainingB.toString());
    }

    private void displayIndicates(Item item) {

        lyrItemStatus.setBackgroundColor(item.getGui().isSelected() ? lyrItemStatus.getResources().getColor(R.color.colorAccent) :
                lyrItemStatus.getResources().getColor(android.R.color.transparent));

        lyrItemStatus.setBackgroundColor(item.getGui().isHighlighted() ? lyrItemStatus.getResources().getColor(R.color.colorAdderAccent) :
                item.getGui().isSelected() ? lyrItemStatus.getResources().getColor(R.color.colorAccent) : lyrItemStatus.getResources().getColor(android.R.color.transparent));
    }


    //region itemOrder to place_order_summary inter-communication via ItemMenuViewHolderListener
    private EditItemViewHolderListener editItemViewHolderListener;

    public interface EditItemViewHolderListener {
        void onItemSelected(Item item);

        void onItemHighlighted(Item item);

        void onItemDeleted(Item item);
    }
    //endregion
}
