package com.wast3dmynd.tillr.boundary.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.utils.CurrencyUtility;

public class EditItemFragment extends Fragment {


    //EditActivity intent dependencies
    private static final String INTENT_ITEM_EDITOR_OPTION = "INTENT_ITEM_EDITOR_OPTION";
    private static final String INTENT_ITEM_EDITOR_ITEM_INSTANCE = "INTENT_ITEM_EDITOR_ITEM_INSTANCE";

    private Item item;
    private ItemEditorOptions editorOptions;

    public enum ItemEditorOptions {
        CREATE_NEW_ITEM,
        EDIT_ITEM
    }

    //views
    private EditText itemName, itemPricePerUnit, itemCount;
    private FloatingActionButton saveBtn;

    public static Fragment newItemEditorIntent(ItemEditorOptions itemEditorOptions, Item item) {

        Fragment fragment = new EditItemFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_ITEM_EDITOR_OPTION, itemEditorOptions);
        bundle.putSerializable(INTENT_ITEM_EDITOR_ITEM_INSTANCE, item);
        fragment.setArguments(bundle);

        return fragment;
    }

    private void displayItemUnits() {
        itemCount.setText(String.valueOf(item.getItemUnitRemaining()));
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

    private void displayItem() {
        String itemNameStr = item.getItemName();
        itemName.setText(itemNameStr);

        String itemPricePerUnitStr = CurrencyUtility.getCurrencyDisplay(item.getItemCostPerUnit());
        itemPricePerUnit.setText(itemPricePerUnitStr);

        displayItemUnits();
    }

    private void changeToolbarTitle() {
        final String createNewItemTitle = getResources().getString(R.string.title_add_item);
        String editItemTitle = getResources().getString(R.string.title_item_editor);
        String title = (editorOptions.equals(ItemEditorOptions.CREATE_NEW_ITEM)) ? createNewItemTitle : editItemTitle;
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(title);
    }


    //region fragment life cycle
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_item_editor, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() == null && !getArguments().containsKey(INTENT_ITEM_EDITOR_OPTION))
            return;

        getIntentArgs();

        changeToolbarTitle();

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
    }
    //endregion

    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            if (event==null) {
                if (actionId==EditorInfo.IME_ACTION_DONE)
                {
                    performItemSave();
                    return true;
                }
                // Capture soft enters in a singleLine EditText that is the last EditText.
                else if (actionId==EditorInfo.IME_ACTION_NEXT){
                    return false;
                }
                // Capture soft enters in other singleLine EditTexts
                else return true;  // Let system handle all other null KeyEvents
            }
            else if (actionId==EditorInfo.IME_NULL) {
                // Capture most soft enters in multi-line EditTexts and all hard enters.
                // They supply a zero actionId and a valid KeyEvent rather than
                // a non-zero actionId and a null event like the previous cases.
                if (event.getAction()==KeyEvent.ACTION_DOWN);
                    // We capture the event when key is first pressed.
                else  return true;   // We consume the event when the key is released.
            }
            else  return true;
            // We let the system handle it when the listener
            // is triggered by something that wasn't an enter.


            // Code from this point on will execute whenever the user
            // presses enter in an attached view, regardless of position,
            // keyboard, or singleLine status.
            return true;   // Consume the event
        }
    };

    private void performItemSave()
    {
        final  MainActivity thisActivity = (MainActivity) getActivity();
        String itemNameStr = itemName.getText().toString();
        double cost = CurrencyUtility.reformatCurrency(itemPricePerUnit.getText().toString());
        String itemCountStr = (itemCount.getText().toString().replace("units", "")).replace("unit", "").trim();
        int count = Integer.parseInt(itemCountStr);

        boolean createItem = (editorOptions == ItemEditorOptions.CREATE_NEW_ITEM);
        String message;
        if (itemNameStr.isEmpty() || itemCountStr.length() > 20) {

            message = createItem ? "Item was not created, " : "item was not updated, ";
            message += itemCountStr.length() > 20 ? "item\'s name exceeds the 20 character limit!" :"due to its name is not assigned.";
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

        Snackbar snackbar = Snackbar.make(getView().findViewById(R.id.container), R.string.title_add_item, Snackbar.LENGTH_LONG);
        snackbar.setAction("Yes!", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = new Item();
                clearFields();
                saveBtn.setVisibility(item.isValid() ? View.VISIBLE : View.GONE);
                editorOptions = ItemEditorOptions.CREATE_NEW_ITEM;
                changeToolbarTitle();
            }
        });

        snackbar.addCallback(new Snackbar.Callback() {

            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
                // when snackbar is showing
            }

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                if (event != DISMISS_EVENT_ACTION) {
                    //will be true if user not click on Action button (for example: manual dismiss, dismiss by swipe
                    startActivity(MainActivity.newInstance(thisActivity));
                }
            }
        });
        snackbar.show();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.item_editor_menu, menu);
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

}
