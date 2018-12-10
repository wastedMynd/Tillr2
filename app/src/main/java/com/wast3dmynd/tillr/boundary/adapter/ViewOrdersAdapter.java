package com.wast3dmynd.tillr.boundary.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.views.ViewOrdersViewHolder;
import com.wast3dmynd.tillr.boundary.views.ViewOrdersViewHolder.OrderSummaryTimelineListener;
import com.wast3dmynd.tillr.entity.Order;
import com.wast3dmynd.tillr.utils.DayFormats;

import java.util.ArrayList;

public class ViewOrdersAdapter extends RecyclerView.Adapter<ViewOrdersViewHolder>
        implements OrderSummaryTimelineListener {

    //params
    private ArrayList<Order> orders;

    private OrderSummaryTimelineViewAdapterListener listener;

    private DayFormats dayFormat;
    private boolean summarizeDaysOrder;

    //constructor
    public ViewOrdersAdapter(OrderSummaryTimelineViewAdapterListener listener, ArrayList<Order> orders) {

        //init params
        this.orders = orders;

        //init listener
        this.listener = listener;

    }

    //RecyclerView.Adapter methods
    @NonNull
    @Override
    public ViewOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //init layout inflater
        //dependency
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        //setup itemView
        View itemView = layoutInflater.inflate(R.layout.item_view_orders_header, parent, false);

        //construct ViewOrdersViewHolder
        return new ViewOrdersViewHolder(itemView, viewType, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewOrdersViewHolder holder, int position) {
        //bind order to ViewOrdersViewHolder
        final Order order = orders.get(position);
        holder.onBind(order);
        if (order.isLastOrder() && position == 0) {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    DayFormats ordersDayFormat = DayFormats.getDayFormat(order.getDate());
                    if (!ordersDayFormat.equals(dayFormat)) return;

                    holder.main_order_summarize.setChecked(summarizeDaysOrder);
                    summarizeDaysOrder = false;
                }
            }, 1000);
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
