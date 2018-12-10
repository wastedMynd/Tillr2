package com.wast3dmynd.tillr.boundary.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.utils.DateFormats;
import com.wast3dmynd.tillr.utils.CurrencyUtility;

import java.text.SimpleDateFormat;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    //Views from item_place_oder layout
    private ImageView itemCountDecrement, itemCountIncrementer;
    private ImageView imvSave;
    private TextView itemName, itemUnits, itemCostPerUnit, itemPriceTotal, itemUnitRemaining, itemTimeStamp;
    private LinearLayout selector;
    private CardView cardView;

   private Context context;

    private ItemListViewHolderListener mListener;

    public ItemViewHolder(View view, ItemListViewHolderListener listener) {
        super(view);
        context = view.getContext();
        this.mListener = listener;
        itemCountDecrement = view.findViewById(R.id.imgBtnItemDecrement);
        itemCountIncrementer = view.findViewById(R.id.imgBtnItemIncrement);
        itemName = view.findViewById(R.id.itemName);
        itemUnits = view.findViewById(R.id.itemUnits);
        itemCostPerUnit = view.findViewById(R.id.itemCostPerUnit);
        itemPriceTotal = view.findViewById(R.id.itemPriceTotal);
        itemUnitRemaining = view.findViewById(R.id.itemUnitsRemaining);

        imvSave = view.findViewById(R.id.imvOpt);

        itemTimeStamp = view.findViewById(R.id.itemTimeStamp);
        selector = view.findViewById(R.id.lyrItemStatus);
        cardView = view.findViewById(R.id.itemCardContainer);
    }

    @SuppressLint("ResourceAsColor")
    public void onBind(final Item item) {


        boolean selected = item.getGui().isSelected();
        @ColorRes int color = selected ? R.color.colorAccent : android.R.color.white;
        selector.setBackgroundColor(context.getResources().getColor(color));

        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public boolean onLongClick(View v) {

                item.getGui().setSelected(!item.getGui().isSelected());
                boolean selected = item.getGui().isSelected();
                @ColorRes int color = selected ? R.color.colorAccent : android.R.color.white;
                selector.setBackgroundColor(context.getResources().getColor(color));
                return true;
            }
        });

        //itemName TextView update
        itemName.setText(item.getItemName());

        //itemCostPerUnit TextView update
        StringBuilder itemCostPerUnitB = new StringBuilder(CurrencyUtility.getCurrencyDisplay(item.getItemCostPerUnit()));
        itemCostPerUnitB.append("/unit");
        itemCostPerUnit.setText(itemCostPerUnitB.toString());

        //itemUnits TextView update
        int units = item.getItemUnits();
        String itemUnitCondition = units > 0 ? "Added " : "Removed ";
        int modifiedUnitCount = units < 0 ? units * -1 : units;
        String itemUnitsTxtPostfix = units > 1 ? "s" : "";
        StringBuilder itemUnitsB = new StringBuilder(itemUnitCondition);
        itemUnitsB.append(modifiedUnitCount);
        itemUnitsB.append(" unit");
        itemUnitsB.append(itemUnitsTxtPostfix);
        itemUnits.setText(itemUnitsB.toString());

        //itemUnitRemaining TextView update
        String itemUnitRemainingTxtPostfix = item.getItemUnitRemaining() > 1 ? "s" : "";
        StringBuilder itemsUnitRemainingB = new StringBuilder("Stock: ");
        itemsUnitRemainingB.append(item.getItemUnitRemaining());
        itemsUnitRemainingB.append(" unit");
        itemsUnitRemainingB.append(itemUnitRemainingTxtPostfix);
        itemUnitRemaining.setText(itemsUnitRemainingB.toString());

        //itemUnitPriceTotal TextView update
        itemPriceTotal.setText(CurrencyUtility.getCurrencyDisplay(item.getItemUnitRemaining() * item.getItemCostPerUnit()));

        displayTimeStamp(DateFormats.Day_Month_Year, item, itemTimeStamp);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.imgBtnItemDecrement:
                        //region itemCountDecrementedOnClick
                        //affected attr unit(Test)
                        int units = item.getItemUnits() - 1;

                        //simple safe guard: no itemUnit to return
                        int negCount = (units * -1);
                        boolean countValid = negCount <= item.getItemUnitRemaining();
                        if (!countValid) return;

                        item.setItemUnits(units);

                        //itemUnits TextView update
                        String itemUnitCondition = units > 0 ? "Added " : "Removed ";
                        int modifiedUnitCount = units < 0 ? units * -1 : units;

                        String itemUnitsTxtPostfix = negCount > 1 ? "s" : "";
                        StringBuilder itemUnitsB = new StringBuilder(itemUnitCondition);
                        itemUnitsB.append(modifiedUnitCount);
                        itemUnitsB.append(" Unit");
                        itemUnitsB.append(itemUnitsTxtPostfix);
                        itemUnits.setText(itemUnitsB.toString());

                        //endregion
                        break;
                    case R.id.imgBtnItemIncrement:
                        //region itemCountIncrementerOnClick
                        //affected attr unit(Test)
                        units = item.getItemUnits() + 1;
                        item.setItemUnits(units);
                        //endregion

                        //itemUnits TextView update
                        itemUnitCondition = units > 0 ? "Added " : "Removed ";
                        modifiedUnitCount = units < 0 ? units * -1 : units;
                        itemUnitsTxtPostfix = units > 1 ? "s" : "";
                        itemUnitsB = new StringBuilder(itemUnitCondition);
                        itemUnitsB.append(modifiedUnitCount);
                        itemUnitsB.append(" Unit");
                        itemUnitsB.append(itemUnitsTxtPostfix);
                        itemUnits.setText(itemUnitsB.toString());

                        break;
                    case R.id.imvOpt:
                        PopupMenu popupMenu = new PopupMenu(v.getContext(), v, Gravity.BOTTOM);
                        MenuInflater inflater = popupMenu.getMenuInflater();
                        inflater.inflate(R.menu.actions_item_list_option, popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                boolean menuItemSelected;
                                switch (menuItem.getItemId()) {
                                    case R.id.action_item_list_opt_save:
                                        menuItemSelected = true;

                                        //region update
                                        int units = item.getItemUnits();
                                        //simple safe guard: no itemUnit to return
                                        boolean countValid = ((units * -1) <= item.getItemUnitRemaining());
                                        if (!countValid) return true;

                                        boolean depleted = ((units * -1) < item.getItemUnitRemaining());
                                        boolean replenished = (units > 0);
                                        int remains = item.getItemUnitRemaining();
                                        item.setItemUnitRemaining(depleted && !replenished ? remains - (units * -1) : remains + units);

                                        //reset unit to zero
                                        units = 0;
                                        item.setItemUnits(units);


                                        //itemUnits TextView update
                                        String itemUnitCondition = units > 0 ? "Added " : "Removed ";
                                        int modifiedUnitCount = units < 0 ? units * -1 : units;
                                        String itemUnitsTxtPostfix = units > 1 ? "s" : "";
                                        StringBuilder itemUnitsB = new StringBuilder(itemUnitCondition);
                                        itemUnitsB.append(modifiedUnitCount);
                                        itemUnitsB.append(" Unit");
                                        itemUnitsB.append(itemUnitsTxtPostfix);
                                        itemUnits.setText(itemUnitsB.toString());

                                        //itemUnitRemaining TextView update
                                        String itemUnitRemainingTxtPostfix = item.getItemUnitRemaining() > 1 ? "s" : "";
                                        StringBuilder itemsUnitRemainingB = new StringBuilder("Stock: ");
                                        itemsUnitRemainingB.append(item.getItemUnitRemaining());
                                        itemsUnitRemainingB.append(" unit");
                                        itemsUnitRemainingB.append(itemUnitRemainingTxtPostfix);
                                        itemUnitRemaining.setText(itemsUnitRemainingB.toString());

                                        //itemUnitPriceTotal TextView update
                                        itemPriceTotal.setText(CurrencyUtility.getCurrencyDisplay(item.getItemUnitRemaining() * item.getItemCostPerUnit()));

                                        //timeStamp
                                        item.setItemTimeStamp(System.currentTimeMillis());
                                        displayTimeStamp(DateFormats.DayName_Day_Month, item, itemTimeStamp);
                                        //endregion

                                        break;
                                    case R.id.action_item_list_opt_edit:
                                        menuItemSelected = true;
                                        //region edit
                                        mListener.onItemEdit(item);
                                        //endregion
                                        break;
                                    case R.id.action_item_list_opt_delete:
                                        menuItemSelected = true;
                                        //region delete
                                        //When database is setup delete this item
                                        mListener.onItemRemoved(item);
                                        //endregion
                                        break;
                                    default:
                                        menuItemSelected = false;
                                        break;
                                }
                                return menuItemSelected;
                            }
                        });
                        popupMenu.show();
                        break;
                }
            }


        };

        itemCountDecrement.setOnClickListener(onClickListener);
        itemCountIncrementer.setOnClickListener(onClickListener);
        imvSave.setOnClickListener(onClickListener);

    }

    private void displayTimeStamp(DateFormats timeFormats, Item item, TextView view) {
        String timeStamp = DateFormats.getSimpleDateString(item.getItemTimeStamp(), timeFormats);
        view.setText(timeStamp);
    }

    public interface ItemListViewHolderListener {
        void onItemRemoved(Item item);

        void onItemEdit(Item item);
    }
}
