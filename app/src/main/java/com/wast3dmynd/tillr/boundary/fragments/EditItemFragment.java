package com.wast3dmynd.tillr.boundary.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.MainActivity;
import com.wast3dmynd.tillr.boundary.adapter.EditItemAdapter;
import com.wast3dmynd.tillr.boundary.views.ContentViewHolder;
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.utils.CrossFadeUtils;
import com.wast3dmynd.tillr.utils.CurrencyUtility;

import java.util.ArrayList;

public class EditItemFragment extends Fragment implements EditItemAdapter.EditItemAdapterListener {

    //EditActivity intent dependencies
    private static final String INTENT_ITEM_EDITOR_OPTION = "INTENT_ITEM_EDITOR_OPTION";
    private static final String INTENT_ITEM_EDITOR_ITEM_INSTANCE = "INTENT_ITEM_EDITOR_ITEM_INSTANCE";

    private Item item;
    private ItemEditorOptions editorOptions;

    public enum ItemEditorOptions {
        CREATE_NEW_ITEM,
        EDIT_ITEM
    }

    //region views
    private EditText itemName, itemPricePerUnit, itemCount;
    private FloatingActionButton saveBtn;
    private ContentViewHolder holder;
    private CrossFadeUtils crossFadeUtils;
    private EditItemAdapter editItemAdapter;
    //endregion

    //region Item Display
    private void displayItem() {

        displayItemName();
        displayItemPricePerUnit();
        displayItemUnits();
    }

    private void displayItemName() {
        itemName.setText(item.getItemName());
    }

    private void displayItemPricePerUnit() {
        itemPricePerUnit.setText(String.valueOf(item.getItemCostPerUnit()));
    }

    private void displayItemUnits() {
        itemCount.setText(String.valueOf(item.getItemUnitRemaining()));
    }
    //endregion

    //region Item Controller

    public static Fragment newItemEditorIntent(ItemEditorOptions itemEditorOptions, Item item) {

        Fragment fragment = new EditItemFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_ITEM_EDITOR_OPTION, itemEditorOptions);
        bundle.putSerializable(INTENT_ITEM_EDITOR_ITEM_INSTANCE, item);
        fragment.setArguments(bundle);

        return fragment;
    }


    private void getIntentArgs() {
        editorOptions = (ItemEditorOptions) getArguments().get(INTENT_ITEM_EDITOR_OPTION);
        item = (editorOptions == ItemEditorOptions.EDIT_ITEM) ? (Item) getArguments().get(INTENT_ITEM_EDITOR_ITEM_INSTANCE) : new Item();
    }


    private void clearFields() {
        itemName.setText("");
        itemPricePerUnit.setText("");
        itemCount.setText("");
        itemName.requestFocus();
    }

    private void changeToolbarTitle() {
        final String createNewItemTitle = getResources().getString(R.string.title_add_item);
        String editItemTitle = getResources().getString(R.string.title_item_editor);
        String title = (editorOptions.equals(ItemEditorOptions.CREATE_NEW_ITEM)) ? createNewItemTitle : editItemTitle;
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(title);
    }


    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            if (event == null) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    performItemSave();
                    return true;
                }
                // Capture soft enters in a singleLine EditText that is the last EditText.
                else if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    return false;
                }
                // Capture soft enters in other singleLine EditTexts
                else return true;  // Let system handle all other null KeyEvents
            } else if (actionId == EditorInfo.IME_NULL) {
                // Capture most soft enters in multi-line EditTexts and all hard enters.
                // They supply a zero actionId and a valid KeyEvent rather than
                // a non-zero actionId and a null event like the previous cases.
                if (event.getAction() == KeyEvent.ACTION_DOWN) ;
                    // We capture the event when key is first pressed.
                else return true;   // We consume the event when the key is released.
            } else return true;
            // We let the system handle it when the listener
            // is triggered by something that wasn't an enter.


            // Code from this point on will execute whenever the user
            // presses enter in an attached view, regardless of position,
            // keyboard, or singleLine status.
            return true;   // Consume the event
        }
    };

    private void performItemSave() {
        final MainActivity thisActivity = (MainActivity) getActivity();
        String itemNameStr = itemName.getText().toString();
        double cost = CurrencyUtility.reformatCurrency(itemPricePerUnit.getText().toString());
        final String itemCountStr = (itemCount.getText().toString().replace("units", "")).replace("unit", "").trim();
        int count = Integer.parseInt(itemCountStr);

        boolean createItem = (editorOptions == ItemEditorOptions.CREATE_NEW_ITEM);
        String message;
        if (itemNameStr.isEmpty() || itemCountStr.length() > 20) {

            message = createItem ? "Item was not created, " : "item was not updated, ";
            message += itemCountStr.length() > 20 ? "item\'s name exceeds the 20 character limit!" : "due to its name is not assigned.";
            Toast.makeText(thisActivity, message, Toast.LENGTH_LONG).show();
            return;
        } else if (cost <= 0) {

            message = createItem ? "Item was not created, " : "item was not updated,";
            message += "due to its cost per unit is below standard.";
            Toast.makeText(thisActivity, message, Toast.LENGTH_LONG).show();
            return;
        } else if (count <= 0) {

            message = createItem ? "Item was not created, " : "item was not updated,";
            message += "due to its unit are below zero or equal to zero.";
            Toast.makeText(thisActivity, message, Toast.LENGTH_LONG).show();
            return;
        }

        item.setItemName(itemNameStr);
        item.setItemCostPerUnit(cost);
        item.setItemUnitRemaining(count);

        boolean updated = new ItemDatabase(thisActivity).updateItem(item);
        message = createItem ? (updated ? "Item is saved" : "Item not save!") : (updated ? "Item is updated" : "Item was not updated!");
        Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.title_add_item)
                .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        item.getGui().setSelected(false);
                        editItemAdapter.addItem(item);
                        item = new Item();
                        clearFields();
                        saveBtn.setVisibility(item.isValid() ? View.VISIBLE : View.GONE);
                        editorOptions = ItemEditorOptions.CREATE_NEW_ITEM;
                        changeToolbarTitle();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        item.getGui().setSelected(false);
                        editItemAdapter.addItem(item);
                        item = new Item();
                        clearFields();
                        saveBtn.setVisibility(item.isValid() ? View.VISIBLE : View.GONE);
                        editorOptions = ItemEditorOptions.CREATE_NEW_ITEM;
                        changeToolbarTitle();
                    }
                });
        // Create the AlertDialog object and show it
        builder.create().show();
    }

    //endregion

    //region fragment life cycle
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_editor, container, false);

        //region Content View Loader
        holder = new ContentViewHolder(view);
        crossFadeUtils = new CrossFadeUtils(holder.contentRecycler, holder.contentLoader);
        holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        //endregion

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() == null && !getArguments().containsKey(INTENT_ITEM_EDITOR_OPTION))
            return;

        getIntentArgs();
        changeToolbarTitle();
        setHasOptionsMenu(true);

        saveBtn = view.findViewById(R.id.item_editor_save);
        itemName = view.findViewById(R.id.item_editor_item_name);
        itemPricePerUnit = view.findViewById(R.id.item_editor_item_price_per_unit);
        itemCount = view.findViewById(R.id.item_editor_item_count);

        if (editorOptions.equals(ItemEditorOptions.EDIT_ITEM)) displayItem();
        else clearFields();


        itemName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String itemNameStr = s.toString();
                item.setItemName(itemNameStr);
                saveBtn.setVisibility(item.isValid() ? View.VISIBLE : View.GONE);
            }
        });
        itemName.setOnEditorActionListener(onEditorActionListener);

        itemPricePerUnit.setFilters(new InputFilter[]{new CurrencyUtility.CurrencyFormatInputFilter()});
        itemPricePerUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    if (!s.toString().isEmpty())
                        item.setItemCostPerUnit(CurrencyUtility.reformatCurrency(s.toString()));
                } finally {
                    saveBtn.setVisibility(item.isValid() ? View.VISIBLE : View.GONE);
                }
            }
        });
        itemPricePerUnit.setOnEditorActionListener(onEditorActionListener);

        itemCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    String itemCountStr = (s.toString().replace("units", "")).replace("unit", "").trim();
                    if (!itemCountStr.isEmpty())
                        item.setItemUnitRemaining(Integer.valueOf(itemCountStr));
                } finally {
                    saveBtn.setVisibility(item.isValid() ? View.VISIBLE : View.GONE);
                }
            }
        });
        itemCount.setOnEditorActionListener(onEditorActionListener);

        saveBtn.setVisibility(item.isValid() ? View.VISIBLE : View.GONE);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performItemSave();
            }
        });


        RecyclerView content_recycler = view.findViewById(R.id.content_recycler);
        content_recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        editItemAdapter = new EditItemAdapter(new ItemDatabase(getContext()).getAll(), this);
        content_recycler.setAdapter(editItemAdapter);
        if (editItemAdapter.getItemCount() == 0)
            holder.contentLoaderInfo.setText(R.string.content_loader_empty);
        else {
            holder.contentLoaderInfo.setText(R.string.content_loader_done);
            crossFadeUtils.crossfade();

            if(item==null)return;
            item.getGui().setSelected(true);
            editItemAdapter.setItem(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.item_editor_menu, menu);
        MenuItem search_item = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) search_item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //perform the final search
                new QueryItemTask().execute(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //text has changed, apply filtering?
                new QueryItemTask().execute(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean result;
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                result = true;
                //region
                startActivity(MainActivity.newInstance(getContext()));
                //endregion
                break;

            default:
                result = super.onOptionsItemSelected(menuItem);
                break;
        }
        return result;
    }
    //endregion

    //implements EditItemAdapter.EditItemAdapterListener
    @Override
    public void onItemSelected(Item item) {
        this.item = item;
        editorOptions = ItemEditorOptions.EDIT_ITEM;
        changeToolbarTitle();
        itemName.setText(item.getItemName());
        itemPricePerUnit.setText("");
        itemCount.setText("");
        displayItem();
        //todo this would be a user pref condition
        itemCount.requestFocus();
    }

    class QueryItemTask extends AsyncTask<String, Void, ArrayList<Item>> {

        public QueryItemTask() {
            crossFadeUtils.processWork();
            holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        }

        @Override
        protected ArrayList<Item> doInBackground(String... strings) {
            String query = strings[0].toLowerCase();
            ArrayList<Item>databaseItems = new ItemDatabase(getContext()).getAll();
            if (query.isEmpty())return databaseItems;


            ArrayList<Item> queriedItemResult = new ArrayList<>();

            for (Item item : databaseItems) {
                String itemName = item.getItemName().toLowerCase();
                if (itemName.contains(query))
                    queriedItemResult.add(item);
            }

            return queriedItemResult;
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);
            editItemAdapter.setItems(items);

            if (items.isEmpty())
                holder.contentLoaderInfo.setText(R.string.content_loader_empty);
            else {
                holder.contentLoaderInfo.setText(R.string.content_loader_done);
                crossFadeUtils.crossfade();
            }
        }
    }
}
