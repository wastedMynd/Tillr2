package com.wast3dmynd.tillr.boundary.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.MainActivity;
import com.wast3dmynd.tillr.boundary.SettingsActivity;
import com.wast3dmynd.tillr.boundary.adapter.ItemLegendAdapter;
import com.wast3dmynd.tillr.boundary.interfaces.MainActivityListener;
import com.wast3dmynd.tillr.boundary.views.ContentViewHolder;
import com.wast3dmynd.tillr.entity.GraphDataHolder;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.utils.CrossFadeUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ItemUnitsInStockFragment extends Fragment {

    private static String ARGS_GRAPH_DATA = "ARGS_GRAPH_DATA";
    private MainActivityListener mainActivityListener;
    private ItemUnitsInStockFragmentListener itemUnitsInStockFragmentListener;
    private GraphDataHolder graphDataHolder;
    private ContentViewHolder holder;
    private CrossFadeUtils crossFadeUtils;

    private TextView graph_title, graph_y_label, graph_x_label;
    private GraphView graph;

    private RecyclerView recyclerView;
    private ItemLegendAdapter itemLegendAdapter;

    public static Fragment newInstance(GraphDataHolder graphDataHolder) {
        Fragment fragment = new ItemUnitsInStockFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGS_GRAPH_DATA, graphDataHolder);
        fragment.setArguments(bundle);
        return fragment;
    }

    //region life cycle

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MainActivityListener))
            throw new ClassCastException("Must implement MainActivityListener");
        this.mainActivityListener = (MainActivityListener) context;

        Bundle data = getArguments();
        graphDataHolder = (GraphDataHolder) data.getSerializable(ARGS_GRAPH_DATA);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_units_in_stock, container, false);
        holder = new ContentViewHolder(view);
        crossFadeUtils = new CrossFadeUtils(holder.contentRecycler, holder.contentLoader);
        holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.label_stock_per_item);
        setHasOptionsMenu(true);

        graph_title = view.findViewById(R.id.graph_title);
        graph_y_label = view.findViewById(R.id.graph_y_label);
        graph_x_label = view.findViewById(R.id.graph_x_label);
        graph = view.findViewById(R.id.graph);


        String title = graphDataHolder.getTitle().replace(view.getResources().getString(R.string.label_stock_per_item), "");
        graph_title.setText(title);
        graph_y_label.setText(graphDataHolder.getYLabel());
        graph_x_label.setText(graphDataHolder.getXLabel());


        if (graphDataHolder.getDataPoints() != null)
            graphDataHolder.getDataPoints().setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    if (itemUnitsInStockFragmentListener == null) return;
                    int position = (int) dataPoint.getX();
                    Item item = graphDataHolder.getRootItems().get(position);
                    itemUnitsInStockFragmentListener.onItemGraphSelected(item, position);
                }
            });

        if (graphDataHolder.getDataPoints() != null)
            graph.addSeries(graphDataHolder.getDataPoints());

        //set manual y bound to have nice steps
        graph.getViewport().setMinY(graphDataHolder.getMinY());
        graph.getViewport().setMaxY(graphDataHolder.getMaxY());
        graph.getViewport().setYAxisBoundsManual(true);

        //set manual x bound to have nice steps
        graph.getViewport().setMinX(graphDataHolder.getMinX());
        graph.getViewport().setMaxX(graphDataHolder.getMaxX());
        graph.getViewport().setXAxisBoundsManual(true);

        graph.getLegendRenderer().setVisible(false);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getGridLabelRenderer().setHumanRounding(true);


        recyclerView = view.findViewById(R.id.content_recycler);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        itemLegendAdapter = new ItemLegendAdapter(graphDataHolder.getRootItems());
        itemUnitsInStockFragmentListener = itemLegendAdapter;
        recyclerView.setAdapter(itemLegendAdapter);

        if (itemLegendAdapter.getItemCount() == 0)
            holder.contentLoaderInfo.setText(R.string.content_loader_empty);
        else {
            holder.contentLoaderInfo.setText(R.string.content_loader_done);
            crossFadeUtils.crossfade();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_item_units_in_stock, menu);

        MenuItem search_item = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) search_item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //perform the final search
                new QueryItemTask(new WeakReference<>(itemLegendAdapter)).execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //text has changed, apply filtering?
                new QueryItemTask(new WeakReference<>(itemLegendAdapter)).execute(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //implemented 23/12/18 to filter item based on range
        switch (item.getItemId()) {
            case R.id.action_filter_min_items:
                new FilterItemsAsyncTask(getContext(), itemLegendAdapter.getItems(), new WeakReference<>(itemLegendAdapter),item.isChecked())
                        .execute(FilterItemRanging.MINIMUM);
                return true;
            case R.id.action_filter_med_items:

                new FilterItemsAsyncTask(getContext(), itemLegendAdapter.getItems(), new WeakReference<>(itemLegendAdapter),item.isChecked())
                        .execute(FilterItemRanging.MEDIUM);
                return true;
            case R.id.action_filter_max_items:
                new FilterItemsAsyncTask(getContext(), itemLegendAdapter.getItems(), new WeakReference<>(itemLegendAdapter),item.isChecked())
                        .execute(FilterItemRanging.MAXIMUM);
                return true;

            case R.id.action_reload:
                itemLegendAdapter.reloadItems(graphDataHolder.getRootItems());

                if (itemLegendAdapter.getItemCount() == 0)
                    holder.contentLoaderInfo.setText(R.string.content_loader_empty);
                else {
                    holder.contentLoaderInfo.setText(R.string.content_loader_done);
                    crossFadeUtils.crossfade();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //endregion

    public interface ItemUnitsInStockFragmentListener {
        void onItemGraphSelected(Item item, int position);
    }

    private enum FilterItemRanging {
        MINIMUM, MEDIUM, MAXIMUM
    }

    private class FilterItemsAsyncTask extends AsyncTask<FilterItemRanging, Void, ArrayList<Item>> {


        private ArrayList<Item> rootItems;
        private WeakReference<ItemLegendAdapter> adapter;

        private final int ITEM_MAX_VALUE;
        private final int ITEM_MED_VALUE;
        private final int ITEM_MIN_VALUE = 0;
        private boolean filter;

        public FilterItemsAsyncTask(Context context, ArrayList<Item> rootItems, WeakReference<ItemLegendAdapter> adapter,boolean filter) {
            this.rootItems = rootItems;
            this.adapter = adapter;
            this.filter = filter;

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            ITEM_MAX_VALUE = Integer.parseInt(pref.getString(SettingsActivity.GeneralPreferenceFragment.PREF_ITEM_MAXIMUM_VALUE, "100"));
            ITEM_MED_VALUE = (int) (ITEM_MAX_VALUE * 0.5);
        }


        private ArrayList<Item> getItemsWithinRange(FilterItemRanging itemRanging, ArrayList<Item> rootItems) {
            ArrayList<Item> items = new ArrayList<>();

            for (Item item : rootItems) {
                int remainingUnits = item.getItemUnitRemaining();

                boolean isItemHighlighted = false;

                if (itemRanging == FilterItemRanging.MEDIUM)
                    isItemHighlighted = remainingUnits == ITEM_MED_VALUE;
                else if (itemRanging == FilterItemRanging.MAXIMUM)
                    isItemHighlighted = remainingUnits > ITEM_MED_VALUE
                            && remainingUnits >= ITEM_MAX_VALUE;
                else if (itemRanging == FilterItemRanging.MINIMUM)
                    isItemHighlighted = remainingUnits >= ITEM_MIN_VALUE
                            && remainingUnits < ITEM_MED_VALUE;


                item.getGui().setHighlighted(isItemHighlighted);
                items.add(item);
            }

            return items;
        }

        @Override
        protected ArrayList<Item> doInBackground(FilterItemRanging... filterItemRangings) {
            return getItemsWithinRange(filterItemRangings[0], rootItems);
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);
            ItemLegendAdapter itemLegendAdapter = adapter.get();
            itemLegendAdapter.filterItems(items,filter);
        }
    }

    class QueryItemTask extends AsyncTask<String, Void, ArrayList<Item>> {

        private WeakReference<ItemLegendAdapter> adapter;
        private ArrayList<Item> adapterItems;

        public QueryItemTask(WeakReference<ItemLegendAdapter> adapter) {
            this.adapter = adapter;
            adapterItems = adapter.get().getItems();
            crossFadeUtils.processWork();
            holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        }

        @Override
        protected ArrayList<Item> doInBackground(String... strings) {
            String query = strings[0].toLowerCase();
            if (query.isEmpty()) return adapterItems;

            ArrayList<Item> queriedItemResult = new ArrayList<>();

            for (Item item : adapterItems) {
                String itemName = item.getItemName().toLowerCase();
                if (itemName.contains(query))
                    queriedItemResult.add(item);
            }

            return queriedItemResult;
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);
            adapter.get().queryItems(items);

            if (items.isEmpty())
                holder.contentLoaderInfo.setText(R.string.content_loader_empty);
            else {
                holder.contentLoaderInfo.setText(R.string.content_loader_done);
                crossFadeUtils.crossfade();
            }
        }
    }
}
