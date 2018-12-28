package com.wast3dmynd.tillr.boundary.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.AnimRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.MainActivity;
import com.wast3dmynd.tillr.boundary.adapter.EditItemAdapter;
import com.wast3dmynd.tillr.boundary.views.ContentViewHolder;
import com.wast3dmynd.tillr.boundary.views.dialogs.BundleUnitCalculatorDialog;
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.ItemSpecial;
import com.wast3dmynd.tillr.utils.CrossFadeUtils;
import com.wast3dmynd.tillr.utils.CurrencyUtility;
import com.wast3dmynd.tillr.utils.DateFormats;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

    private boolean isItemCancelable = false;
    private ItemSpecial special;

    //region views
    private EditText itemName, itemPricePerUnit, itemCount, itemSpecialPrice,
            itemSpecialStartDate, itemSpecialEndDate, itemSpecialStartTime, itemSpecialEndTime;
    private Switch item_advanced_setting_switch;
    private View item_advanced_setting_container, item_control_details_container;
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

    private void displayItemSpecialPrice() {
        itemSpecialPrice.setText(String.valueOf(special.getSpecialPrice()));
    }

    private void displayItemSpecialStartDate() {
        itemSpecialStartDate.setText(DateFormats.getSimplifiedDateString(special.getSpecialStartDate()));
    }

    private void displayItemSpecialEndDate() {
        itemSpecialEndDate.setText(DateFormats.getSimplifiedDateString(special.getSpecialEndDate()));
    }

    private void displayItemSpecialStartTime() {
        DateFormats dateFormat = DateFormats.Hours_Minutes;
        itemSpecialStartTime.setText(DateFormats.getSimpleDateString(special.getSpecialStartTime(), dateFormat));
    }

    private void displayItemSpecialEndTime() {
        DateFormats dateFormat = DateFormats.Hours_Minutes;
        itemSpecialEndTime.setText(DateFormats.getSimpleDateString(special.getSpecialEndTime(), dateFormat));
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
        initializeSpecial(item);
    }

    private void initializeSpecial(Item item) {
        special = item.getSpecial();
    }

    private void clearFields() {
        itemName.setText("");
        itemPricePerUnit.setText("");
        itemCount.setText("");
        itemSpecialPrice.setText("");
        itemSpecialPrice.setHint(getContext().getResources().getString(R.string.label_item_price_per_unit_specials));
        itemSpecialStartDate.setText("");
        itemSpecialEndDate.setText("");
        itemSpecialStartTime.setText("");
        itemSpecialEndTime.setText("");

        itemName.requestFocus();
    }

    private void invalidateCancelAction(boolean isItemCancelable) {
        try {
            this.isItemCancelable = isItemCancelable;
            getActivity().invalidateOptionsMenu();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void changeToolbarTitle() {
        final String createNewItemTitle = getResources().getString(R.string.title_add_item);
        String editItemTitle = getResources().getString(R.string.title_item_editor);
        String title = (editorOptions.equals(ItemEditorOptions.CREATE_NEW_ITEM)) ? createNewItemTitle : editItemTitle;
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(title);
    }

    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {

        boolean performEditorAction(int actionId, KeyEvent event) {
            if (event == null) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    performItemSave();
                    return true;
                }
                // Capture soft enters in a singleLine EditText that is the last EditText.
                else return actionId != EditorInfo.IME_ACTION_NEXT;
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

        @Override
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            return performEditorAction(actionId, event);
        }
    };

    private void performItemCancel() {
        item.getGui().setSelected(false);
        editItemAdapter.addItem(item);
        item = new Item();
        special = new ItemSpecial();
        clearFields();
        invalidateCancelAction(false);
        saveBtn.setVisibility(item.isValid() ? View.VISIBLE : View.GONE);
        editorOptions = ItemEditorOptions.CREATE_NEW_ITEM;
        changeToolbarTitle();
    }

    private void performItemSave() {
        final MainActivity thisActivity = (MainActivity) getActivity();
        String itemNameStr = itemName.getText().toString();
        double cost = CurrencyUtility.reformatCurrency(itemPricePerUnit.getText().toString());
        final String itemCountStr = (itemCount.getText().toString().replace("units", "")).replace("unit", "").trim();
        int count = Integer.parseInt(itemCountStr);

        boolean createItem = (editorOptions == ItemEditorOptions.CREATE_NEW_ITEM);
        String message;
        if (itemNameStr.isEmpty() && itemName.getText().toString().length() > 20) {

            message = createItem ? "Item was not created, " : "item was not updated, ";
            message += itemName.getText().toString().length() > 20 ? "item\'s name exceeds the 20 character limit!" : "due to its name is not assigned.";
            Toast.makeText(thisActivity, message, Toast.LENGTH_LONG).show();
            return;

        } else if (cost <= 0) {

            message = createItem ? "Item was not created, " : "item was not updated,";
            message += "due to its cost per unit is below standard.";
            Toast.makeText(thisActivity, message, Toast.LENGTH_LONG).show();
            return;
        }

        item.setItemName(itemNameStr);
        item.setItemCostPerUnit(cost);
        item.setItemUnitRemaining(count);
        special.setItemId(item.getId());
        item.setSpecial(special);

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
                        special = new ItemSpecial();
                        clearFields();
                        invalidateCancelAction(false);
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
                        special = new ItemSpecial();
                        clearFields();
                        invalidateCancelAction(false);
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_editor, container, false);

        //region Content View Loader
        holder = new ContentViewHolder(view);
        crossFadeUtils = new CrossFadeUtils(holder.contentRecycler, holder.contentLoader);
        holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        //endregion

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
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

        itemCount.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new BundleUnitCalculatorDialog(getContext(), new BundleUnitCalculatorDialog.DialogListener() {
                    @Override
                    public void onUnitsCalculated(int units) {
                        itemCount.setText(String.valueOf(units));
                        item.setItemUnitRemaining(units);
                    }
                });
                return false;
            }
        });
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

                if (s != null && !s.toString().isEmpty()) {
                    item.setItemCostPerUnit(CurrencyUtility.reformatCurrency(s.toString()));
                    StringBuilder hintBuilder = new StringBuilder(getContext().getResources().getString(R.string.label_item_price_per_unit_specials));
                    hintBuilder.append(" must be less then ");
                    hintBuilder.append(CurrencyUtility.getCurrencyDisplay(item.getItemCostPerUnit()));
                    itemSpecialPrice.setHint(hintBuilder.toString());
                }
                saveBtn.setVisibility(item.isValid() ? View.VISIBLE : View.GONE);
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

        //advanced settings
        item_control_details_container = view.findViewById(R.id.item_control_details_container);
        item_advanced_setting_switch = view.findViewById(R.id.item_advanced_setting_switch);
        item_advanced_setting_container = view.findViewById(R.id.item_advanced_setting_container);
        itemSpecialPrice = view.findViewById(R.id.item_special_price);
        itemSpecialStartDate = view.findViewById(R.id.item_special_start_date);
        itemSpecialEndDate = view.findViewById(R.id.item_special_end_date);
        itemSpecialStartTime = view.findViewById(R.id.item_special_start_time);
        itemSpecialEndTime = view.findViewById(R.id.item_special_end_time);

        item_advanced_setting_container.setVisibility(item_advanced_setting_switch.isChecked() ? View.VISIBLE : View.GONE);
        item_advanced_setting_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            private void showAnimation(@AnimRes int anim, long duration, final View animView, final int visibility) {
                Animation aniSlide = AnimationUtils.loadAnimation(getContext(), anim);
                aniSlide.setDuration(duration);
                aniSlide.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        animView.setVisibility(visibility);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                animView.startAnimation(aniSlide);
            }

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                //change visibility of advanced setting
                if (item_control_details_container.getVisibility() == View.VISIBLE)
                    showAnimation(R.anim.slide_up, 350, item_control_details_container, View.GONE);
                else
                    showAnimation(R.anim.slide_down, 350, item_control_details_container, View.VISIBLE);

                if (item_advanced_setting_container.getVisibility() == View.GONE)
                    showAnimation(R.anim.slide_down, 1000, item_advanced_setting_container, View.VISIBLE);
                else
                    showAnimation(R.anim.slide_up, 1000, item_advanced_setting_container, View.GONE);
            }
        });

        itemSpecialPrice.setFilters(new InputFilter[]{new CurrencyUtility.CurrencyFormatInputFilter()});
        itemSpecialPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s != null && !s.toString().isEmpty())
                    special.setSpecialPrice(CurrencyUtility.reformatCurrency(s.toString()));

                saveBtn.setVisibility(special.isSpecialValid() && item.isValid() ? View.VISIBLE : View.GONE);

            }
        });
        itemSpecialStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View clickedView) {
                showDatePickerDialog(clickedView);
            }

            private void showDatePickerDialog(final View clickedView) {
                final Calendar myCalendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    private String getDateString(Date date) {
                        return DateFormats.getSimplifiedDateString(date);
                    }

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        Date currentDate = new Date(System.currentTimeMillis()),
                                startDate = new Date(myCalendar.getTimeInMillis());

                        if (startDate.before(currentDate)) {
                            StringBuilder msg = new StringBuilder("Special\'s start date is invalid!");
                            msg.append("\nDue to the start time ");
                            msg.append(getDateString(startDate));
                            msg.append(" ;is before today\'s date ");
                            msg.append(getDateString(currentDate));
                            msg.append(".\n");
                            msg.append("Special\'s start date must be equal or greater then today\'s date.");
                            Toast.makeText(getContext(), msg.toString(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        special.setSpecialStartDate(startDate.getTime());
                        displayItemSpecialStartDate();
                        saveBtn.setVisibility(special.isSpecialValid() && special.isSpecialValid() ? View.VISIBLE : View.GONE);
                    }

                };
                new DatePickerDialog(getContext(), date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        itemSpecialEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View clickedView) {
                showDatePickerDialog();
            }

            private void showDatePickerDialog() {
                final Calendar myCalendar = Calendar.getInstance();

                final long startDate = special.getSpecialStartDate();

                boolean isSpecialStartDateValid = startDate > 0;

                if (!isSpecialStartDateValid) {
                    Toast.makeText(getContext(), "Set special start date first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        Date endDate;
                        endDate = new Date(myCalendar.getTimeInMillis());

                        boolean isSpecialDateRangeValid = endDate.after(new Date(startDate)) || endDate.getTime() == new Date(startDate).getTime();
                        if (!isSpecialDateRangeValid) {

                            String startDateString = DateFormats.getSimplifiedDateString(startDate);
                            StringBuilder msg = new StringBuilder("Special end time, is invalid!");
                            msg.append("\nMust be on or after ");
                            msg.append(startDateString);
                            Toast.makeText(getContext(), msg.toString(), Toast.LENGTH_SHORT).show();
                            return;
                        }


                        special.setSpecialEndDate(endDate.getTime());
                        displayItemSpecialEndDate();
                        saveBtn.setVisibility(special.isSpecialValid() ? View.VISIBLE : View.GONE);
                    }

                };
                new DatePickerDialog(getContext(), date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        itemSpecialStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View clickedView) {
                showTimePickerDialog(clickedView);
            }

            private void showTimePickerDialog(final View clickedView) {
                final Calendar myCalendar = Calendar.getInstance();
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);

                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        myCalendar.set(Calendar.MINUTE, minute);

                        special.setSpecialStartTime(myCalendar.getTimeInMillis());
                        displayItemSpecialStartTime();
                        saveBtn.setVisibility(special.isSpecialValid() ? View.VISIBLE : View.GONE);
                    }
                },
                        hour,
                        minute,
                        DateFormat.is24HourFormat(getContext())).show();
            }
        });
        itemSpecialEndTime.setOnClickListener(new View.OnClickListener() {

            private void showTimePickerDialog() {
                final Calendar myCalendar = Calendar.getInstance();
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);

                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    private String getTimeString(long specialTime) {
                        DateFormats dateFormat = DateFormats.Hours_Minutes;
                        return DateFormats.getSimpleDateString(specialTime, dateFormat);
                    }

                    private String getDateString(long specialTime) {
                        DateFormats dateFormat = DateFormats.Day_Month_Year;
                        return DateFormats.getSimpleDateString(specialTime, dateFormat);
                    }

                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        myCalendar.set(Calendar.MINUTE, minute);


                        long specialEndTime = myCalendar.getTimeInMillis();
                        long specialStartDate, specialEndDate, specialStartTime;
                        specialStartDate = item.getSpecial().getSpecialStartDate();
                        specialEndDate = item.getSpecial().getSpecialEndDate();
                        specialStartTime = item.getSpecial().getSpecialStartTime();


                        boolean isSpecialDateRangeValid = specialEndDate >= specialStartDate;
                        boolean isSpecialTimeRangeValid = specialEndTime >= specialStartTime;

                        StringBuilder msg = new StringBuilder("Special\'s end time, is invalid!");
                        if (specialEndTime == 0) {
                            msg.append("\n");
                            msg.append("Special\'s end time is ");
                            msg.append(getTimeString(specialEndTime));
                            Toast.makeText(getContext(), msg.toString(), Toast.LENGTH_SHORT).show();
                            return;
                        } else if (!isSpecialDateRangeValid) {
                            msg.append("\n");
                            msg.append("Due to Special\'s end date is ");
                            msg.append(getDateString(specialEndDate));
                            msg.append(" must be on or after ");
                            msg.append(getDateString(specialStartDate));
                            msg.append(" Special\'s start date.");
                            if (!isSpecialTimeRangeValid) {
                                msg.append("\n");
                                msg.append("And, also the Special\'s end time is ");
                                msg.append(getTimeString(specialEndTime));
                                msg.append(" ;less then Special\'s start time ");
                                msg.append(getTimeString(specialStartTime));
                            }
                            Toast.makeText(getContext(), msg.toString(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        special.setSpecialEndTime(specialEndTime);
                        displayItemSpecialEndTime();
                        saveBtn.setVisibility(special.isSpecialValid() ? View.VISIBLE : View.GONE);

                    }
                },
                        hour,
                        minute,
                        DateFormat.is24HourFormat(getContext())).show();

            }

            @Override
            public void onClick(final View clickedView) {
                if (special.getSpecialStartTime() == 0) {
                    Toast.makeText(getContext(), "Set special start time first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                showTimePickerDialog();
            }
        });

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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    crossFadeUtils.crossfade();
                    if (item == null) return;
                    item.getGui().setSelected(true);
                    editItemAdapter.setItem(item);
                }
            }, getContext().getResources().getInteger(R.integer.loading_duration));
        }

        if (editorOptions.equals(ItemEditorOptions.EDIT_ITEM)) displayItem();
        else clearFields();
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

        MenuItem actionCancel = menu.findItem(R.id.action_cancel);
        actionCancel.setVisible(isItemCancelable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                startActivity(MainActivity.newInstance(getContext()));
                return true;
            case R.id.action_cancel:
                performItemCancel();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
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

        invalidateCancelAction(true);

        //todo this would be a user pref condition
        itemCount.requestFocus();

        initializeSpecial(item);
        displayItemSpecialPrice();
        displayItemSpecialStartDate();
        displayItemSpecialEndDate();
        displayItemSpecialStartTime();
        displayItemSpecialEndTime();

    }

    class QueryItemTask extends AsyncTask<String, Void, ArrayList<Item>> {

        public QueryItemTask() {
            crossFadeUtils.processWork();
            holder.contentLoaderInfo.setText(R.string.content_loader_processing);
        }

        @Override
        protected ArrayList<Item> doInBackground(String... strings) {
            String query = strings[0].toLowerCase();
            ArrayList<Item> databaseItems = new ItemDatabase(getContext()).getAll();
            if (query.isEmpty()) return databaseItems;


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
