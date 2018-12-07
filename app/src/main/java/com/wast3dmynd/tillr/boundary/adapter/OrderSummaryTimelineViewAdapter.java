package com.wast3dmynd.tillr.boundary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.view_holder.OrderSummaryTimelineViewHolder;
import com.wast3dmynd.tillr.boundary.view_holder.OrderSummaryTimelineViewHolder.OrderSummaryTimelineListener;
import com.wast3dmynd.tillr.entity.Order;
import com.wast3dmynd.tillr.utils.DayFormats;

import java.util.ArrayList;
import java.util.logging.Handler;

public class OrderSummaryTimelineViewAdapter extends RecyclerView.Adapter<OrderSummaryTimelineViewHolder>
        implements OrderSummaryTimelineListener {

    //params
    private ArrayList<Order> orders;

    private OrderSummaryTimelineViewAdapterListener listener;

    private DayFormats dayFormat;
    private boolean summarizeDaysOrder;

    //constructor
    public OrderSummaryTimelineViewAdapter(OrderSummaryTimelineViewAdapterListener listener, ArrayList<Order> orders) {

        //init params
        this.orders = orders;

        //init listener
        this.listener = listener;
    }

    //RecyclerView.Adapter methods
    @NonNull
    @Override
    public OrderSummaryTimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //init layout inflater
        //dependency
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        //setup itemView
        View itemView = layoutInflater.inflate(R.layout.layout_order_summary_timeline_view, parent, false);

        //construct OrderSummaryTimelineViewHolder
        return new OrderSummaryTimelineViewHolder(itemView, viewType, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderSummaryTimelineViewHolder holder, int position) {
        //bind order to OrderSummaryTimelineViewHolder
        final Order order = orders.get(position);
        holder.onBind(order);
        if (orders.get(position).isLastOrder() && position == 0) {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    DayFormats ordersDayFormat =DayFormats.getDayFormat(order.getDate());
                    if(!ordersDayFormat.equals(dayFormat))return;

                    holder.main_order_summarize.setChecked(summarizeDaysOrder);
                    summarizeDaysOrder = false;
                }
            },1500);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    //OrderSummaryTimelineListener implements
    @Override
    public void onShowOrderDetailsOptionSelected(Order order) {
        listener.onShowOrderDetailsOptionSelected(order);
    }

    @Override
    public void onOrderSummaryRequest(Order order, boolean isOrderSummaryRequested) {
        listener.onOrderSummaryRequest(order, isOrderSummaryRequested);
    }

    public void summarizeDaysOrders(DayFormats dayFormat, boolean summarizeDaysOrder) {
        this.dayFormat = dayFormat;
        this.summarizeDaysOrder = summarizeDaysOrder;
        notifyItemChanged(0);
    }

    //interface construct
    public interface OrderSummaryTimelineViewAdapterListener extends OrderSummaryTimelineListener {
    }
}
