package com.wast3dmynd.tillr.boundary.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.MainActivity;
import com.wast3dmynd.tillr.boundary.fragments.ItemUnitsInStockFragment;
import com.wast3dmynd.tillr.boundary.interfaces.MainActivityListener;
import com.wast3dmynd.tillr.boundary.views.GraphViewHolder;
import com.wast3dmynd.tillr.entity.GraphDataHolder;

import java.util.ArrayList;

public class GraphViewAdapter extends RecyclerView.Adapter<GraphViewHolder> {

    private ArrayList<GraphDataHolder> dataHolders;
    private MainActivityListener mainActivityListener;


    public GraphViewAdapter(ArrayList<GraphDataHolder> dataHolders) {
        this.dataHolders = dataHolders;
    }

    @NonNull
    @Override
    public GraphViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_dashboard_graph, parent, false);
        GraphViewHolder graphViewHolder = new GraphViewHolder(view);
        return graphViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GraphViewHolder holder, int position) {
        final GraphDataHolder dataHolder = dataHolders.get(position);
        holder.setGraphViewHolderListener(new GraphViewHolder.GraphViewHolderListener() {
            @Override
            public void onShowItemUnitsDetails() {
                if(mainActivityListener==null)return;
                mainActivityListener.onFragmentChanged(ItemUnitsInStockFragment.newInstance(dataHolder));
            }
        });
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

    public void setMainActivityListener(MainActivityListener mainActivityListener) {
        this.mainActivityListener = mainActivityListener;
    }
}
