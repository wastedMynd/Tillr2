package com.wast3dmynd.tillr.boundary.view_holder;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.Order;
import com.wast3dmynd.tillr.utils.DateFormats;
import com.wast3dmynd.tillr.utils.CurrencyUtility;

import java.util.ArrayList;

public class ViewOrdersViewHolder extends RecyclerView.ViewHolder {

    //region Views on layout_view_orders_item_display.xml
    private TextView viewOrdersItemOrderNumber,
            viewOrdersItemOrderDate,
            viewOrdersItemOrderTimeStamp,
            viewOrdersItemOrderTotal,
            viewOrdersItemOrderUnits;

    private ImageView viewOrdersItemOrderOption;
    //endregion

    //ViewOrdersViewHolderPopupMenuListener
    private ViewOrdersViewHolderPopupMenuListener viewOrdersViewHolderPopupMenuListener;

    //region Initialize views on layout_view_orders_item_display.xml
    public ViewOrdersViewHolder(View itemView,ViewOrdersViewHolderPopupMenuListener viewOrdersViewHolderPopupMenuListener) {
        super(itemView);
        initViews(itemView);
        this.viewOrdersViewHolderPopupMenuListener = viewOrdersViewHolderPopupMenuListener;
    }

    private void initViews(View v) {
        viewOrdersItemOrderNumber = v.findViewById(R.id.view_orders_item_order_number);
        viewOrdersItemOrderDate = v.findViewById(R.id.view_orders_item_order_date);
        viewOrdersItemOrderTimeStamp = v.findViewById(R.id.view_orders_item_order_timestamp);
        viewOrdersItemOrderTotal = v.findViewById(R.id.view_orders_item_order_total);
        viewOrdersItemOrderUnits = v.findViewById(R.id.view_orders_item_order_units);
        viewOrdersItemOrderOption = v.findViewById(R.id.view_orders_item_order_option);
    }
    //endregion

    //region Bind Order data  to views on layout_view_orders_item_display.xml
    public void onBind(final Order order) {

        //display order number
        viewOrdersItemOrderNumber.setText(String.valueOf(order.getId()));

        //display order date
        DateFormats dateFormats = DateFormats.Day_Month_Year;
        String dateStamp = DateFormats.getSimpleDateString(order.getDate(), dateFormats);
        viewOrdersItemOrderDate.setText(dateStamp);

        //display order time stamp
        DateFormats timeFormats = DateFormats.Hours_Minutes;
        String timeStamp = DateFormats.getSimpleDateString(order.getTimeStamp(), timeFormats);
        viewOrdersItemOrderTimeStamp.setText(timeStamp);

        //display order total
        viewOrdersItemOrderTotal.setText(CurrencyUtility.getCurrencyDisplay(order.getTotal()));

        //display order units
        ArrayList<Item> items = order.getItems();
        int units = 0;
        for (Item item : items) units += item.getItemUnits();
        viewOrdersItemOrderUnits.setText(String.valueOf(units));

        //display order option
        viewOrdersItemOrderOption.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v, Gravity.BOTTOM);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.actions_view_orders_option, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        boolean menuItemSelected;
                        switch (menuItem.getItemId()) {
                            case R.id.action_view_orders_opt_view_order:
                                menuItemSelected = true;
                                viewOrdersViewHolderPopupMenuListener.onViewOrdersPopupMenuOptView(order);
                                break;
                            case R.id.action_view_orders_opt_share_order:
                                menuItemSelected = true;
                                viewOrdersViewHolderPopupMenuListener.onViewOrdersPopupMenuOptShare(order);
                                break;
                            case R.id.action_view_orders_opt_delete_order:
                                menuItemSelected = true;
                                viewOrdersViewHolderPopupMenuListener.onViewOrdersPopupMenuOptDelete(order);
                                break;
                            default:
                                menuItemSelected = false;
                                break;
                        }
                        return menuItemSelected;
                    }
                });
                popupMenu.show();
            }
        });
    }
    //endregion

    public interface ViewOrdersViewHolderPopupMenuListener{
        void onViewOrdersPopupMenuOptView(Order order);
        void onViewOrdersPopupMenuOptShare(Order order);
        void onViewOrdersPopupMenuOptDelete(Order order);
    }
}
