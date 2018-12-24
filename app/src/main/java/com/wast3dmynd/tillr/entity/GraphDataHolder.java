package com.wast3dmynd.tillr.entity;

import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.wast3dmynd.tillr.boundary.views.GraphViewHolder;

import java.io.Serializable;
import java.util.ArrayList;

public class GraphDataHolder implements Serializable {

    private String title;
    private String yLabel;
    private String xLabel;
    private BaseSeries<DataPoint> dataPoints;
    private ArrayList<BaseSeries<DataPoint>> colectionOfDataPoints;
    private long minX;
    private long maxX;
    private long minY;
    private long maxY;
    private ArrayList<Item> rootItems;


        //region getters and setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYLabel() {
        return yLabel;
    }

    public void setYLabel(String yLabel) {
        this.yLabel = yLabel;
    }

    public String getXLabel() {
        return xLabel;
    }

    public void setXLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public BaseSeries<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(BaseSeries<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public long getMinX() {
        return minX;
    }

    public void setMinX(long minX) {
        this.minX = minX;
    }

    public long getMaxX() {
        return maxX;
    }

    public void setMaxX(long maxX) {
        this.maxX = maxX;
    }

    public long getMinY() {
        return minY;
    }

    public void setMinY(long minY) {
        this.minY = minY;
    }

    public long getMaxY() {
        return maxY;
    }

    public void setMaxY(long maxY) {
        this.maxY = maxY;
    }

    public ArrayList<Item> getRootItems() {
        return rootItems;
    }

    public void setRootItems(ArrayList<Item> rootItems) {
        this.rootItems = rootItems;
    }

    public ArrayList<BaseSeries<DataPoint>> getColectionOfDataPoints() {
        return colectionOfDataPoints;
    }

    public void setColectionOfDataPoints(ArrayList<BaseSeries<DataPoint>> colectionOfDataPoints) {
        this.colectionOfDataPoints = colectionOfDataPoints;
    }

    //endregion

}
