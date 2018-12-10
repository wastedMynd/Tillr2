package com.wast3dmynd.tillr.boundary.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.fragments.DashBoardFragment;
import com.wast3dmynd.tillr.boundary.interfaces.MainActivityListener;
import com.wast3dmynd.tillr.boundary.views.DashboardViewHolder;

import java.util.ArrayList;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardViewHolder> {

    private ArrayList<DashBoardFragment.DashboardItem> dashboardItems;
    private MainActivityListener listener;

    public DashboardAdapter(ArrayList<DashBoardFragment.DashboardItem> dashboardItems, MainActivityListener listener) {
        this.dashboardItems = dashboardItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DashboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_dashboard, parent, false);
        return new DashboardViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardViewHolder holder, int position) {
        holder.onBind(get(position));
    }

    @Override
    public int getItemCount() {
        return dashboardItems.size();
    }

    private DashBoardFragment.DashboardItem get(int position) {
        return dashboardItems.get(position);
    }
}
