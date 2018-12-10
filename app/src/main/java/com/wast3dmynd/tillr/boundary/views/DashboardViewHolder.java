package com.wast3dmynd.tillr.boundary.views;

import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.fragments.DashBoardFragment;
import com.wast3dmynd.tillr.boundary.fragments.EditItemFragment;
import com.wast3dmynd.tillr.boundary.fragments.ItemsFragment;
import com.wast3dmynd.tillr.boundary.fragments.OrdersFragment;
import com.wast3dmynd.tillr.boundary.fragments.PlaceOrderFragment;
import com.wast3dmynd.tillr.boundary.interfaces.MainActivityListener;
import com.wast3dmynd.tillr.entity.Item;

public class DashboardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private MainActivityListener listener;

    private ImageView dashboard_item_icon;
    private TextView dashboard_item_title;

    public DashboardViewHolder(View itemView, MainActivityListener listener) {
        super(itemView);
        this.listener = listener;

        CardView dashboard_item;
        dashboard_item = itemView.findViewById(R.id.dashboard_item);
        dashboard_item.setOnClickListener(this);

        dashboard_item_icon = itemView.findViewById(R.id.dashboard_item_icon);
        dashboard_item_title= itemView.findViewById(R.id.dashboard_item_title);
    }

    public void onBind(DashBoardFragment.DashboardItem dashboardItem)
    {
        dashboard_item_icon.setImageResource(dashboardItem.getIcon());
        dashboard_item_title.setText(dashboardItem.getTitle());
    }

    @Override
    public void onClick(View v) {
        Fragment fragment;
        switch (getAdapterPosition()) {
            case 0:
                fragment = EditItemFragment.newItemEditorIntent(EditItemFragment.ItemEditorOptions.CREATE_NEW_ITEM, new Item());
                break;
            case 1:
                fragment = ItemsFragment.newItemListIntent();
                break;
            case 2:
                fragment = OrdersFragment.newInstance();
                break;
            case 3:
                fragment = PlaceOrderFragment.newInstance(v.getContext());
                break;
            default:
                fragment = DashBoardFragment.newInstance();
                break;
        }
        listener.onFragmentChanged(fragment);
    }
}
