package com.wast3dmynd.tillr.boundary.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.MainActivity;
import com.wast3dmynd.tillr.boundary.SettingsActivity;
import com.wast3dmynd.tillr.boundary.adapter.PlaceOrderAdapter;
import com.wast3dmynd.tillr.boundary.interfaces.MainActivityListener;
import com.wast3dmynd.tillr.boundary.views.ContentViewHolder;
import com.wast3dmynd.tillr.boundary.views.PlaceOrderViewHolder;
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.database.OrderDatabase;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.Order;
import com.wast3dmynd.tillr.utils.ClearableEditText;
import com.wast3dmynd.tillr.utils.CrossFadeUtils;
import com.wast3dmynd.tillr.utils.CurrencyUtility;
import com.wast3dmynd.tillr.utils.DateFormats;

import java.util.ArrayList;

public class PlaceOrderFragment extends Fragment implements PlaceOrderViewHolder.ItemMenuViewHolderListener {

    private static String ARG_ORDER = "ARG_ORDER";

    //region views
    private TextView txtOrderNumber;
    private TextView txtOrderItems;
    private TextView txtOrderDate;
    private TextView txtOrderTime;
    private TextView txtOrderTotal;
    private TextView txtOrderFunds;
    private TextView txtOrderCredit;
    private TextView txtOrderUnits;
    private FloatingActionButton fab;

    private ContentViewHolder holder;
    private CrossFadeUtils crossFadeUtils;
    //endregion

    //region data
    private Order order;

    public Order getOrder() {
        return order;
    }

    private void setOrder(Order order) {
        this.order = order;
    }

    private PlaceOrderAdapter placeOrderAdapter;

    private MainActivityListener listener;

    private static ArrayList<Item> masterItems;
    //endregion

    //get instance of this fragment
    public static Fragment newInstance(Context context) {

        Order order = new Order();
        order.setId(Order.getNewID(context));
        order.setDate(System.currentTimeMillis());
        order.setTimeStamp(System.currentTimeMillis());

        Fragment fragment = new PlaceOrderFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ORDER, order);
        fragment.setArguments(bundle);
        return fragment;
    }

    //region get passed instance data
    private void processArgs() {
        if (!getArguments().containsKey(ARG_ORDER)) return;
        setOrder((Order) getArguments().get(ARG_ORDER));
    }
    //endregion

    //region display floating action button icon
    private void displayFabIcon() {

        boolean orderHasBeenPaidFor = getOrder().getFunds() > 0 && getOrder().getTotal() > 0 && getOrder().getFunds() >= getOrder().getTotal();
        @DrawableRes int fabIcon = !orderHasBeenPaidFor ? R.drawable.ic_attach_money_black_24dp : R.drawable.ic_save_black_24dp;

        fab.setImageDrawable(ContextCompat.getDrawable(getContext(), fabIcon));
    }

    private void displayFunds() {
        txtOrderFunds.setText(CurrencyUtility.getCurrencyDisplay(order.getFunds()));
    }

    private void displayCredit() {
        txtOrderCredit.setText(CurrencyUtility.getCurrencyDisplay(order.getCredit()));
    }

    private void displayTotal() {
        txtOrderTotal.setText(CurrencyUtility.getCurrencyDisplay(getOrder().getTotal()));
    }

    private void displayOrderSummary() {
        String orderNumber = String.valueOf(getOrder().getId());
        txtOrderNumber.setText(orderNumber);

        //txtOrderDate
        DateFormats dateFormats = DateFormats.Day_Month_Year;
        String dateStamp = DateFormats.getSimpleDateString(getOrder().getDate(), dateFormats);
        txtOrderDate.setText(dateStamp);

        //txtOrderTime
        DateFormats timeFormats = DateFormats.Hours_Minutes;
        String timeStamp = DateFormats.getSimpleDateString(getOrder().getTimeStamp(), timeFormats);
        txtOrderTime.setText(timeStamp);

        //unit count
        ArrayList<Item> items = getOrder().getItems();
        txtOrderItems.setText(String.valueOf(items.size()));
        int units = 0;
        for (Item item : items) units += item.getItemUnits();
        txtOrderUnits.setText(String.valueOf(units));

        //paid
        displayFunds();

        //Total
        displayTotal();

        //credit
        displayCredit();

        //fab change
        displayFabIcon();
    }
    //endregion

    //region fragment life cycle
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_order, container, false);
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

        //Toolbar toolbar = view.findViewById(R.id.toolbar);
        //((MainActivity) getActivity()).setSupportActionBar(toolbar);
        //((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_place_order);
        setHasOptionsMenu(true);


        //region linking views to fragment_place_order
        txtOrderNumber = view.findViewById(R.id.order_number);
        txtOrderItems = view.findViewById(R.id.order_items);
        txtOrderUnits = view.findViewById(R.id.order_units);
        txtOrderDate = view.findViewById(R.id.order_date);
        txtOrderTime = view.findViewById(R.id.order_time);

        txtOrderTotal = view.findViewById(R.id.order_total);
        txtOrderFunds = view.findViewById(R.id.order_paid);
        txtOrderCredit = view.findViewById(R.id.order_credit);
        RecyclerView recyclerView = view.findViewById(R.id.content_recycler);
        fab = view.findViewById(R.id.fab);

        processArgs();

        displayOrderSummary();

        //region floating Action Button
        final MainActivity thisActivity = (MainActivity) getActivity();
        fab.setOnClickListener(new View.OnClickListener() {

            private void displayResult() {
                String msg = getOrder().isOrderValid() ? "This order has been paid." : "This order is not paid!";
                Toast.makeText(thisActivity, msg, Toast.LENGTH_LONG).show();
                displayFabIcon();
            }

            @Override
            public void onClick(View v) {
                if (!getOrder().isOrderValid()) {

                    if (getOrder().getItems().isEmpty()) {
                        Toast.makeText(thisActivity, "Add an Item...", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //region payment dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext())
                            .setTitle("Payment Option")
                            .setCancelable(false);

                    //layout payment view constructed
                    LayoutInflater inflater = LayoutInflater.from(v.getContext());
                    View view = inflater.inflate(R.layout.dialog_payment, null, false);
                    builder.setView(view);

                    //update views
                    TextView totalTv = view.findViewById(R.id.total);
                    final TextView creditTv = view.findViewById(R.id.credit);

                    StringBuilder totalBuilder = new StringBuilder("Total: ");
                    totalBuilder.append(CurrencyUtility.getCurrencyDisplay(getOrder().getTotal()));
                    totalTv.setText(totalBuilder.toString());

                    StringBuilder creditBuilder = new StringBuilder("Credit: ");
                    creditBuilder.append(CurrencyUtility.getCurrencyDisplay(getOrder().getCredit()));
                    creditTv.setText(creditBuilder.toString());

                    //payment edit text
                    ClearableEditText txtPayment = view.findViewById(R.id.payment);
                    txtPayment.addTextChangedListener(new TextWatcher() {

                        private void refreshCredit() {
                            StringBuilder creditBuilder = new StringBuilder("Credit: ");
                            creditBuilder.append(CurrencyUtility.getCurrencyDisplay(getOrder().getCredit()));
                            creditTv.setText(creditBuilder.toString());
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                            if (s.length() == 0) return;
                            getOrder().setFunds(CurrencyUtility.reformatCurrency(s.toString()));
                            displayFunds();
                            refreshCredit();
                            displayCredit();
                        }
                    });


                    // Create the AlertDialog object and show it
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            displayFabIcon();
                        }
                    });

                    txtPayment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                            if (event == null) {
                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                    displayResult();
                                    dialog.dismiss();
                                }
                                // Capture soft enters in a singleLine EditText that is the last EditText.
                                else if (actionId == EditorInfo.IME_ACTION_NEXT) ;
                                    // Capture soft enters in other singleLine EditTexts
                                else return false;  // Let system handle all other null KeyEvents
                            } else if (actionId == EditorInfo.IME_NULL) {
                                // Capture most soft enters in multi-line EditTexts and all hard enters.
                                // They supply a zero actionId and a valid KeyEvent rather than
                                // a non-zero actionId and a null event like the previous cases.
                                if (event.getAction() == KeyEvent.ACTION_DOWN) ;
                                    // We capture the event when key is first pressed.
                                else
                                    return true;   // We consume the event when the key is released.
                            } else return false;
                            // We let the system handle it when the listener
                            // is triggered by something that wasn't an enter.


                            // Code from this point on will execute whenever the user
                            // presses enter in an attached view, regardless of position,
                            // keyboard, or singleLine status.
                            return true;   // Consume the event
                        }
                    });

                    //endregion

                } else {
                    //region on save order
                    new SaveOrderTask().execute(getOrder());
                    //endregion
                }
            }
        });
        //endregion

        recyclerView.setLayoutManager(new LinearLayoutManager(thisActivity, LinearLayoutManager.VERTICAL, false));
        placeOrderAdapter = new PlaceOrderAdapter(getContext(), new ItemDatabase(getContext()).getAll(), this);

        recyclerView.setAdapter(placeOrderAdapter);

        if (placeOrderAdapter.getItemCount() == 0)
            holder.contentLoaderInfo.setText(R.string.content_loader_empty);
        else {
            holder.contentLoaderInfo.setText(R.string.content_loader_done);
            crossFadeUtils.crossfade();
            masterItems = new ArrayList<>();
            masterItems.addAll(placeOrderAdapter.getItems());
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_order_placement, menu);
        MenuItem search_item = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) search_item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //perform the final search
                new QueryItemTask(placeOrderAdapter.getItems()).execute(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //text has changed, apply filtering?
                new QueryItemTask(placeOrderAdapter.getItems()).execute(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                getActivity().startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;

            case R.id.action_filter_placed_order:
                new ItemFilterTask(placeOrderAdapter.getItems()).execute();
                return true;

            case R.id.action_reload:
                new ReloadTask().execute();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOrderMenuItemChanged(Item orderItem) {

        boolean isItemOrdered = orderItem.getItemUnits() > 0;

        if (isItemOrdered) {

            if (getOrder().getItems().contains(orderItem)) {
                int indexOfItem = getOrder().getItems().indexOf(orderItem);
                //update item
                getOrder().getItems().remove(indexOfItem);
                getOrder().getItems().add(indexOfItem, orderItem);
            } else //add item
                getOrder().getItems().add(orderItem);

        } else  //remove orderItem from this order, if it's not Ordered
            getOrder().getItems().remove(orderItem);


        displayOrderSummary();

        placeOrderAdapter.update(orderItem);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MainActivityListener))
            throw new ClassCastException("Must Implement MainActivityListener");
        listener = (MainActivityListener) context;
    }

    //endregion

    //region AsyncTasks
    class QueryItemTask extends AsyncTask<String, Void, ArrayList<Item>> {

        private ArrayList<Item> adapterItems;

        public QueryItemTask(ArrayList<Item> defaultItems) {
            adapterItems = defaultItems;
            crossFadeUtils.processWork();
            holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        }

        @Override
        protected ArrayList<Item> doInBackground(String... strings) {
            String query = strings[0].toLowerCase();
            if (query.isEmpty()) return masterItems;

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
            placeOrderAdapter.setItems(items);

            if (items.isEmpty())
                holder.contentLoaderInfo.setText(R.string.content_loader_empty);
            else {
                holder.contentLoaderInfo.setText(R.string.content_loader_done);
                crossFadeUtils.crossfade();
            }
        }
    }

    class ItemFilterTask extends AsyncTask<Void, Void, ArrayList<Item>> {

        private ArrayList<Item> items;

        public ItemFilterTask(ArrayList<Item> items) {
            this.items = items;
            crossFadeUtils.processWork();
            holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        }

        @Override
        protected ArrayList<Item> doInBackground(Void... args) {

            ArrayList<Item> filteredItems = new ArrayList();
            for (Item item : items)
                if (item.getGui().isSelected())
                    filteredItems.add(item);

            return filteredItems;
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);

            if (items.isEmpty())
                holder.contentLoaderInfo.setText(R.string.content_loader_empty);
            else {
                placeOrderAdapter.setItems(items);
                holder.contentLoaderInfo.setText(R.string.content_loader_done);
                crossFadeUtils.crossfade();
            }
        }
    }

    class ReloadTask extends AsyncTask<Void, Void, ArrayList<Item>> {

        public ReloadTask() {
            crossFadeUtils.processWork();
            holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        }

        @Override
        protected ArrayList<Item> doInBackground(Void... args) {
            return new ItemDatabase(getContext()).getAll();
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);

            if (items.isEmpty())
                holder.contentLoaderInfo.setText(R.string.content_loader_empty);
            else {
                holder.contentLoaderInfo.setText(R.string.content_loader_done);
                placeOrderAdapter.setItems(items);
                crossFadeUtils.crossfade();
            }
        }
    }

    class SaveOrderTask extends AsyncTask<Order, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Order... orders) {
            return new OrderDatabase(getContext()).addItem(orders[0]);
        }

        @Override
        protected void onPostExecute(Boolean isOrderProcessed) {
            super.onPostExecute(isOrderProcessed);

            String successMsg = "Your Order was Successfully processed..";
            String failureMsg = "Your Order was Denied!";
            String message = (getOrder().isOrderValid() && isOrderProcessed) ? successMsg : failureMsg;
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            if (!isOrderProcessed) return;

            ItemDatabase itemDatabase = new ItemDatabase(getContext());
            ArrayList<Item> itemsDB = itemDatabase.getAll();

            //update remaining items
            for (Item item : itemsDB) {
                for (Item itemInOrder : getOrder().getItems()) {
                    if (item.equals(itemInOrder))
                        item.setItemUnitRemaining(item.getItemUnitRemaining() - itemInOrder.getItemUnits());
                }
                itemDatabase.updateItem(item);
            }

            fab.setVisibility(View.GONE);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.prompt_place_another_order)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            listener.onFragmentChanged(PlaceOrderFragment.newInstance(getContext()));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(MainActivity.newInstance(getContext()));
                        }
                    });
            // Create the AlertDialog object and show it
            builder.create().show();
        }
    }
    //endregion

}
