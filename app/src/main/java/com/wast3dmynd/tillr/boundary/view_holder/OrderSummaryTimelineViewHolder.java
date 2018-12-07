package com.wast3dmynd.tillr.boundary.view_holder;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.Order;
import com.wast3dmynd.tillr.utils.CurrencyUtility;
import com.wast3dmynd.tillr.utils.DateFormats;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderSummaryTimelineViewHolder extends RecyclerView.ViewHolder {

    //region views
    private TextView main_order_date, order_number,
            order_total,
            order_time,
            order_units;
    public Switch main_order_summarize;
    private ImageView order_option;
    //endregion

    //region interface
    private OrderSummaryTimelineListener orderSummaryTimelineListener;
    //endregion

    //region constructor
    public OrderSummaryTimelineViewHolder(View itemView, int viewType, OrderSummaryTimelineListener orderSummaryTimelineListener) {

        //region init params
        super(itemView);

        this.orderSummaryTimelineListener = orderSummaryTimelineListener;
        //endregion

        //region initViews
        //mTimelineView = itemView.findViewById(R.id.time_marker);
        //mTimelineView.initLine(viewType);

        main_order_date = itemView.findViewById(R.id.main_order_date);

        main_order_summarize = itemView.findViewById(R.id.main_order_summarize);

        order_number = itemView.findViewById(R.id.view_orders_item_order_number);

        order_time = itemView.findViewById(R.id.view_orders_item_order_time);

        order_units = itemView.findViewById(R.id.view_orders_item_order_units);

        order_total = itemView.findViewById(R.id.view_orders_item_order_total);

        order_option = itemView.findViewById(R.id.order_option);
        //endregion
    }
    //endregion

    //region bind data
    public void onBind(final Order order) {

        //region display main_order_date
        main_order_date.setVisibility(order.isLastOrder() ? View.VISIBLE : View.GONE);
        if (order.isLastOrder()) {
            DateFormats dateFormats = DateFormats.Day_Month_Year;
            String dateString = DateFormats.getSimpleDateString(order.getDate(), dateFormats);
            main_order_date.setText(dateString);
        } else main_order_date.setText("");
        //endregion

        //region display main_order_summarize
        main_order_summarize.setVisibility(order.isLastOrder() ? View.VISIBLE : View.GONE);
        main_order_summarize.setChecked((order.isLastOrder() && order.isSummarized()));
        main_order_summarize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                orderSummaryTimelineListener.onOrderSummaryRequest(order, isChecked);
            }
        });
        //endregion

        //region display order number
        order_number.setText(String.valueOf(order.getId()));
        //endregion

        //region display order's time stamp
        DateFormats timeFormats = DateFormats.Hours_Minutes;
        String timeStamp = DateFormats.getSimpleDateString(order.getTimeStamp(), timeFormats);
        order_time.setText(timeStamp);
        //endregion

        //region display order's units
        ArrayList<Item> items = order.getItems();
        int units = 0;
        for (Item item : items) units += item.getItemUnits();
        order_units.setText(String.valueOf(units));
        //endregion

        //region display order's total
        order_total.setText(CurrencyUtility.getCurrencyDisplay(order.getTotal()));
        //endregion

        //region set order_option onClickListener
        order_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderSummaryTimelineListener.onShowOrderDetailsOptionSelected(order);
            }
        });
        //endregion
    }
    //endregion

    //region interface construct
    public interface OrderSummaryTimelineListener extends Serializable{
        void onShowOrderDetailsOptionSelected(Order order);

        void onOrderSummaryRequest(Order order, boolean isOrderSummaryRequested);
    }
    //endregion

}
