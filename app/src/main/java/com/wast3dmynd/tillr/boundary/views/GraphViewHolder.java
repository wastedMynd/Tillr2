package com.wast3dmynd.tillr.boundary.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.entity.GraphDataHolder;

public class GraphViewHolder extends RecyclerView.ViewHolder {

    private TextView graph_title, graph_y_label, graph_x_label;
    private GraphView graph;


    public GraphViewHolder(View view) {
        super(view);
        graph_title = view.findViewById(R.id.graph_title);
        graph_y_label = view.findViewById(R.id.graph_y_label);
        graph_x_label = view.findViewById(R.id.graph_x_label);
        graph = view.findViewById(R.id.graph);
    }

    public void onBind(GraphDataHolder dataHolder) {


        graph_title.setText(dataHolder.getTitle());
        graph_y_label.setText(dataHolder.getYLabel());
        graph_x_label.setText(dataHolder.getXLabel());


        graph.addSeries(dataHolder.getDataPoints());

        //set manual y bound to have nice steps
        graph.getViewport().setMinY(dataHolder.getMinY());
        graph.getViewport().setMaxY(dataHolder.getMaxY());
        graph.getViewport().setYAxisBoundsManual(true);

        //set manual x bound to have nice steps
        graph.getViewport().setMinX(dataHolder.getMinX());
        graph.getViewport().setMaxX(dataHolder.getMaxX());
        graph.getViewport().setXAxisBoundsManual(true);

        if (dataHolder.getDataPoints() instanceof BarGraphSeries) {
            graph.getLegendRenderer().setVisible(false);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            graph.getGridLabelRenderer().setHumanRounding(true);
            return;
        }
        //set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(graph.getContext()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(2);
        //as we use dates as labels,the human rounding to nice readable numbers is not necessary.
        graph.getGridLabelRenderer().setHumanRounding(false);

        //endregion
    }
}
