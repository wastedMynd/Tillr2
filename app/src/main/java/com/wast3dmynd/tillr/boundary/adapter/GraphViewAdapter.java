package com.wast3dmynd.tillr.boundary.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.views.GraphViewHolder;
import com.wast3dmynd.tillr.entity.GraphDataHolder;

import java.util.ArrayList;

public class GraphViewAdapter extends RecyclerView.Adapter<GraphViewHolder> {

    private ArrayList<GraphDataHolder> dataHolders;

    public GraphViewAdapter(ArrayList<GraphDataHolder> dataHolders) {
        this.dataHolders = dataHolders;
    }

    @NonNull
    @Override
    public GraphViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_dashboard_graph, parent, false);
        return new GraphViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GraphViewHolder holder, int position) {
        GraphDataHolder dataHolder = dataHolders.get(position);
        holder.onBind(dataHolder);
    }

    @Override
    public int getItemCount() {
        return dataHolders.size();
    }

    public void setDataHolders(ArrayList<GraphDataHolder> dataHolders) {
        this.dataHolders = dataHolders;
        notifyDataSetChanged();
    }
}
