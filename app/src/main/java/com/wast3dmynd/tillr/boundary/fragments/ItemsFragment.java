package com.wast3dmynd.tillr.boundary.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.MainActivity;
import com.wast3dmynd.tillr.boundary.adapter.ItemAdapter;
import com.wast3dmynd.tillr.boundary.interfaces.MainActivityListener;
import com.wast3dmynd.tillr.boundary.views.ContentViewHolder;
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.utils.CrossFadeUtils;
import com.wast3dmynd.tillr.utils.CurrencyUtility;
import com.wast3dmynd.tillr.utils.DateFormats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ItemsFragment extends Fragment implements ItemAdapter.ItemListAdapterListener {

    private ItemAdapter itemAdapter;
    private TextView items_last_update_date, items_count, items_units, items_asserts;

    private MainActivityListener listener;

    private ContentViewHolder holder;
    private CrossFadeUtils crossFadeUtils;

    @NonNull
    public static Fragment newItemListIntent() {
        return new ItemsFragment();
    }

    //region fragment life cycle
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_items, container, false);
        //region Content View Loader
        holder = new ContentViewHolder(view);
        crossFadeUtils = new CrossFadeUtils(holder.contentRecycler, holder.contentLoader);
        holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        //endregion
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_item_list);
        setHasOptionsMenu(true);

        items_last_update_date = view.findViewById(R.id.dashboard_date);
        items_count = view.findViewById(R.id.order_date);
        items_units = view.findViewById(R.id.items_units);
        items_asserts = view.findViewById(R.id.items_asserts);

        //region Items Summary
        List<Item> items = new ArrayList<>();
        List<Date> dates = new ArrayList<>();

        ArrayList<Object> objects = new ItemDatabase(getContext()).getItems();
        //provide list with data
        for (Object object : objects) {
            Item item = (Item) object;
            items.add(item);
            dates.add(new Date(item.getItemTimeStamp()));
        }

        //region Get last Item update date
        // Get the one closest to Today's date
        Date closest = Collections.min(dates, new Comparator<Date>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public int compare(Date d1, Date d2) {
                long now = System.currentTimeMillis();
                long diff1 = Math.abs(d1.getTime() - now);
                long diff2 = Math.abs(d2.getTime() - now);
                return Long.compare(diff1, diff2);
            }
        });
        //format it for display
        DateFormats dateFormats = DateFormats.Day_Month_Year;
        String dateStamp = DateFormats.getSimpleDateString(closest, dateFormats);
        items_last_update_date.setText(dateStamp);
        //endregion

        //items present
        items_count.setText(String.valueOf(items.size()));

        //units present
        int units = 0;
        for (Item item : items) units += item.getItemUnitRemaining();
        items_units.setText(String.valueOf(units));

        //asserts on hold
        double asserts = 0.00;
        for (Item item : items)
            asserts += (item.getItemUnitRemaining() * item.getItemCostPerUnit());
        items_asserts.setText(CurrencyUtility.getCurrencyDisplay(asserts));
        //endregion

        //region link views to fragment_place_order
        RecyclerView itemListRecycler = view.findViewById(R.id.content_recycler);
        itemListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        FloatingActionButton fabInsertItem = view.findViewById(R.id.fabAddItem);
        //endregion

        //region placeOrderItemAdapter init
        itemAdapter = new ItemAdapter(getContext(), this);
        itemListRecycler.setAdapter(itemAdapter);

        if (itemAdapter.getItemCount() == 0)
            holder.contentLoaderInfo.setText(R.string.content_loader_empty);
        else {
            holder.contentLoaderInfo.setText(R.string.content_loader_done);
            crossFadeUtils.crossfade();
        }
        //endregion

        //region View EventHandler config
        View.OnClickListener fabOrderCheckoutOnClickListener;
        fabOrderCheckoutOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFragmentChanged(EditItemFragment.newItemEditorIntent(EditItemFragment.ItemEditorOptions.CREATE_NEW_ITEM, new Item()));
            }
        };
        fabInsertItem.setOnClickListener(fabOrderCheckoutOnClickListener);
        //endregion
    }

    //region implements ItemAdapter.ItemListAdapterListener
    @Override
    public void onItemEdit(final Item item) {
        String itemEdit = "Edit " + item.getItemName() + " ?";
        Snackbar.make(getView().findViewById(R.id.viewItems), itemEdit, Snackbar.LENGTH_LONG)
                .setAction("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onFragmentChanged(EditItemFragment.newItemEditorIntent(EditItemFragment.ItemEditorOptions.EDIT_ITEM, item));
                    }
                })
                .show();
    }

    @Override
    public void onItemDelete(final Item item) {

        StringBuilder stringBuilder = new StringBuilder("Delete Item: ");
        stringBuilder.append(item.getItemName());
        stringBuilder.append(" ?");

        Snackbar.make(getView().findViewById(R.id.viewItems), stringBuilder.toString(), Snackbar.LENGTH_LONG)
                .setAction("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean removed = new ItemDatabase(v.getContext()).removeItem(item);
                        if (removed) itemAdapter.onItemDelete(item);
                        String message = removed ? "Item is removed" : "Item not removed!";
                        Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
    //endregion


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_items, menu);

        MenuItem search_item = menu.findItem(R.id.search_item);

        SearchView searchView = (SearchView) search_item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //perform the final search
                new SearchForItemAsyncTask(getContext()).execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                new SearchForItemAsyncTask(getContext()).execute(newText);
                return true;
            }
        });
    }

    private class SearchForItemAsyncTask extends AsyncTask<String, Void, ArrayList<Item>> {

        private Context context;

        public SearchForItemAsyncTask(Context context) {
            this.context = context;
            crossFadeUtils.processWork();
            holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        }

        @Override
        protected ArrayList<Item> doInBackground(String... strings) {

            //init Search params
            ArrayList<Item> searchItems = new ArrayList<>();
            String searchQuery = strings[0].toLowerCase();


            //get Items for database
            ArrayList<Object> itemObjs = new ItemDatabase(context).getItems();
            ArrayList<Item> items = new ArrayList<>(itemObjs.size());
            for (Object obj : itemObjs) items.add((Item) obj);

            //No searchQuery provided
            if (searchQuery.isEmpty()) return items;

            //get all Item matching the searchQuery
            for (Item item : items)
                if (item.getItemName().toLowerCase().contains(searchQuery))
                    searchItems.add(item);

            return searchItems;
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);

            itemAdapter.setItems(items);

            if (items.isEmpty())
                holder.contentLoaderInfo.setText(R.string.content_loader_empty);
            else {
                holder.contentLoaderInfo.setText(R.string.content_loader_done);
                crossFadeUtils.crossfade();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    //endregion


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MainActivityListener))
            throw new ClassCastException("Must implement ItemsFragmentListener");
        listener = (MainActivityListener) context;
    }


}
