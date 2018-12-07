package com.wast3dmynd.tillr.boundary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.view_holder.ViewOrdersViewHolder;
import com.wast3dmynd.tillr.database.OrderDatabase;
import com.wast3dmynd.tillr.entity.Order;

import java.util.ArrayList;

public class ViewOrdersAdapter extends RecyclerView.Adapter<ViewOrdersViewHolder> implements ViewOrdersViewHolder.ViewOrdersViewHolderPopupMenuListener {

    private ArrayList<Order> orders;
    private LayoutInflater layoutInflater;
    private ViewOrdersAdapterViewHolderPopupMenuOptListener viewOrdersAdapterViewHolderPopupMenuOptListener;
    private static final int INVALID_ORDER_RESTORATION_POSITION = -1;
    private int orderRestorationPosition = INVALID_ORDER_RESTORATION_POSITION;

    public ViewOrdersAdapter(Context context) {

        layoutInflater = LayoutInflater.from(context);

        //get all orders from the OrderDatabase
        ArrayList<Object> ordersObjects = new OrderDatabase(context).getItems();
        this.orders = new ArrayList<>(ordersObjects.size());
        for (Object orderObject : ordersObjects) orders.add((Order) orderObject);

        if (!(context instanceof ViewOrdersAdapterViewHolderPopupMenuOptListener))
            throw new ClassCastException("Context must implement ViewOrdersAdapterViewHolderPopupMenuOptListener");
        this.viewOrdersAdapterViewHolderPopupMenuOptListener = (ViewOrdersAdapterViewHolderPopupMenuOptListener) context;
    }

    @NonNull
    @Override
    public ViewOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.layout_view_orders_item_display, parent, false);
        return new ViewOrdersViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewOrdersViewHolder holder, int position) {
        holder.onBind(getOrder(position));
    }

    @Override
    public int getItemCount() {
        return getOrders().size();
    }

    private Order getOrder(int position) {
        return getOrders().get(position);
    }

    private ArrayList<Order> getOrders() {
        return orders;
    }

    //region ViewOrdersViewHolderPopupMenuListener implements
    @Override
    public void onViewOrdersPopupMenuOptView(Order order) {
        viewOrdersAdapterViewHolderPopupMenuOptListener.onViewOrdersPopupMenuOptActionView(order);
    }

    @Override
    public void onViewOrdersPopupMenuOptShare(Order order) {
        viewOrdersAdapterViewHolderPopupMenuOptListener.onViewOrdersPopupMenuOptActionShare(order);
    }

    @Override
    public void onViewOrdersPopupMenuOptDelete(Order order) {
        orderRestorationPosition = getOrders().indexOf(order);
        viewOrdersAdapterViewHolderPopupMenuOptListener.onViewOrdersPopupMenuOptActionDelete(order);
    }

    /**
     * To be called on the {@link com.wast3dmynd.tillr.boundary.ViewOrdersActivity}
     */
    public void onViewOrdersDelete() {
        getOrders().remove(orderRestorationPosition);
        notifyItemRemoved(orderRestorationPosition);
        orderRestorationPosition = INVALID_ORDER_RESTORATION_POSITION;
    }

    /**
     * To be called on the {@link com.wast3dmynd.tillr.boundary.ViewOrdersActivity}
     */
    public void onViewOrdersRestored(Order order) {
        if (orderRestorationPosition == INVALID_ORDER_RESTORATION_POSITION) return;
        getOrders().add(orderRestorationPosition, order);
        notifyItemInserted(orderRestorationPosition);
    }
    //endregion


    public interface ViewOrdersAdapterViewHolderPopupMenuOptListener {
        void onViewOrdersPopupMenuOptActionView(Order order);

        void onViewOrdersPopupMenuOptActionShare(Order order);

        void onViewOrdersPopupMenuOptActionDelete(Order order);
    }
}