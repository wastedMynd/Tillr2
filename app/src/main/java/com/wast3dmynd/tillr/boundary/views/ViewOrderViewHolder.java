package com.wast3dmynd.tillr.boundary.views;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.utils.CurrencyUtility;

public class ViewOrderViewHolder extends RecyclerView.ViewHolder {

    //region Views on item_view_order
    private CardView cardView;
    private LinearLayout itemStatus;
    private TextView itemName,
            itemCostPerUnit,
            itemUnits,
            itemTotal;

    private ViewOrderViewHolderListener viewOrderViewHolderListener;
    //endregion

    //region Initialize views on item_view_order
    public ViewOrderViewHolder(ViewOrderViewHolderListener viewOrderViewHolderListener, View itemView) {
        super(itemView);
        initViews(viewOrderViewHolderListener, itemView);
    }

    private void initViews(ViewOrderViewHolderListener viewOrderViewHolderListener, View v) {

        this.viewOrderViewHolderListener = viewOrderViewHolderListener;
        cardView = v.findViewById(R.id.item);
        itemStatus = v.findViewById(R.id.itemStatus);
        itemName = v.findViewById(R.id.itemName);
        itemCostPerUnit = v.findViewById(R.id.itemCostPerUnit);
        itemUnits = v.findViewById(R.id.itemUnitsRemaining);
        itemTotal = v.findViewById(R.id.itemTotal);
    }
    //endregion

    //region Bind Order data  to views on item_view_ordervoid
     public void onBind(final Item item) {


        int selectedColor = itemName.getResources().getColor(R.color.colorAccent);
        int unselectedColor = itemName.getResources().getColor(android.R.color.transparent);
        itemStatus.setBackgroundColor(item.getGui().isSelected() ? selectedColor : unselectedColor);

        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                item.getGui().setSelected(!item.getGui().isSelected());
                viewOrderViewHolderListener.onViewOrderItemSelected(getAdapterPosition(), item);
                return true;
            }
        });


        itemName.setText(item.getItemName());

        String itemCostPerUnitStr = CurrencyUtility.getCurrencyDisplay(item.getItemCostPerUnit()) + "/unit";
        itemCostPerUnit.setText(itemCostPerUnitStr);

        String unitsStr = String.format("%s unit%s", item.getItemUnits(), item.getItemUnits() > 1 ? "s" : "");

        itemUnits.setText(unitsStr);
        itemTotal.setText(CurrencyUtility.getCurrencyDisplay(item.getItemPriceTotal()));
    }
    //endregion

    public interface ViewOrderViewHolderListener {
        void onViewOrderItemSelected(int itemPosition, Item item);
    }
}
