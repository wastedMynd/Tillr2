package com.wast3dmynd.tillr.boundary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.wast3dmynd.tillr.R;
import com.wast3dmynd.tillr.boundary.fragments.DashBoardFragment;
import com.wast3dmynd.tillr.boundary.fragments.EditItemFragment;
import com.wast3dmynd.tillr.boundary.fragments.ItemsFragment;
import com.wast3dmynd.tillr.boundary.fragments.OrdersFragment;
import com.wast3dmynd.tillr.boundary.fragments.PlaceOrderFragment;
import com.wast3dmynd.tillr.boundary.interfaces.MainActivityListener;
import com.wast3dmynd.tillr.database.ItemDatabase;
import com.wast3dmynd.tillr.entity.Item;
import com.wast3dmynd.tillr.entity.Order;
import com.wast3dmynd.tillr.utils.CurrencyUtility;
import com.wast3dmynd.tillr.utils.DateFormats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainActivityListener {

    private static final boolean DEBUGGING = true;
    private static final int MAIN_ACTIVITY_CONTENT_LOADER_ID = 0;
    //view(s)
    NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private ConstraintLayout container;
    private TextView nav_item_list, nav_order_list;

    Fragment selectedFragment = null;

    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region users preferred day mode
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAutoNightMode = pref.getBoolean(SettingsActivity.GeneralPreferenceFragment.PREF_AUTO_THEME_MODE_KEY, false);
        if (isAutoNightMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        else {
            boolean isNightMode = pref.getBoolean(SettingsActivity.GeneralPreferenceFragment.PREF_NIGHT_MODE_KEY, false);
            if (isNightMode)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        //endregion

        setContentView(R.layout.activity_main);


        //init tool bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        //ini navigation view
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //init drawer
        nav_item_list = (TextView) navigationView.getMenu().findItem(R.id.nav_item_list).getActionView();
        nav_order_list = (TextView) navigationView.getMenu().findItem(R.id.nav_order_list).getActionView();
        //This method will initialize the count value

        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                displayNavigationData();
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawer.addDrawerListener(toggle);

        container = findViewById(R.id.container);

        if (DEBUGGING) {
            new Thread(new Runnable() {

                class FileData {
                    String name;
                    String data;
                }


                @Override
                public void run() {
                    try {
                        //Find the directory for the SD Card using the API
                        //*Don't* hardcode "/sdcard"
                        File sdcard = Environment.getExternalStorageDirectory();

                        StringBuilder pathBuilder = new StringBuilder(sdcard.getPath());
                        pathBuilder.append("/Til Point");
                        String mainDir = pathBuilder.toString();

                        File myFile = new File(mainDir);
                        if (!myFile.exists()) myFile.mkdir();

                        ArrayList<FileData> fileDataHolder = new ArrayList<>();
                        FileData userItemFile = new FileData();
                        userItemFile.name = "My_Items.txt";
                        FileData devItemFile = new FileData();
                        devItemFile.name = "App_Items.txt";

                        ArrayList<Item> items = new ItemDatabase(getApplicationContext()).getAll();

                        if (!items.isEmpty()) {
                            for (int index = 0; index < items.size(); index++) {
                                Item item = items.get(index);
                                if (index > 0) {
                                    devItemFile.data += "~";
                                    userItemFile.data += "\n\n";
                                }
                                devItemFile.data += item.toJson();

                                StringBuilder userItemStr = new StringBuilder("Item's Name: ");
                                userItemStr.append(item.getItemName());
                                userItemStr.append("\nRemaining Units: ");
                                userItemStr.append(String.valueOf(item.getItemUnitRemaining()));
                                userItemStr.append("\nPrice: ");
                                userItemStr.append(CurrencyUtility.getCurrencyDisplay(item.getItemCostPerUnit()));
                                userItemStr.append("\nTotal: ");
                                userItemStr.append(CurrencyUtility.getCurrencyDisplay(item.getItemUnitRemaining() * item.getItemCostPerUnit()));
                                userItemStr.append("\nLast Modify Date: ");
                                userItemStr.append(DateFormats.getSimpleDateString(item.getItemTimeStamp(), DateFormats.DayName_Day_Month_Year));

                                userItemFile.data += userItemStr.toString();
                            }

                            fileDataHolder.add(devItemFile);
                            fileDataHolder.add(userItemFile);

                            for (FileData fileData : fileDataHolder) {
                                myFile = new File(mainDir + "/" + fileData.name);
                                myFile.createNewFile();
                                FileOutputStream fOut = new FileOutputStream(myFile);
                                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                myOutWriter.append(fileData.data);
                                myOutWriter.close();
                                fOut.close();
                            }
                        } else {
                            //Get the text file
                            File file = new File(mainDir, "App_Items.txt");
                            if (file.exists()) {
                                //Read text from file
                                StringBuilder text = new StringBuilder();
                                try {
                                    BufferedReader br = new BufferedReader(new FileReader(file));
                                    String line;
                                    while ((line = br.readLine()) != null) text.append(line);
                                    br.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                String[] itemsStr = text.toString().split("~");
                                ItemDatabase itemDatabase = new ItemDatabase(getApplicationContext());
                                for (String itemStr : itemsStr) {
                                    //Parse itemStr from JSON to Item object
                                    String json = itemStr.replace("null", "").trim();
                                    Item item = Item.fromJson(json);
                                    item.setItemTimeStamp(System.currentTimeMillis());
                                    itemDatabase.addItem(item);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }).start();
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }


    private void displayNavigationData() {
        nav_order_list.setBackgroundColor(getResources().getColor(android.R.color.white));
        nav_item_list.setBackgroundColor(getResources().getColor(android.R.color.white));
        getSupportLoaderManager().initLoader(MAIN_ACTIVITY_CONTENT_LOADER_ID, null, loaderCallbacks);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            StringBuilder msg = new StringBuilder("Close ");
            msg.append(getResources().getString(R.string.app_name));
            msg.append("?");
            Snackbar.make(container, msg, Snackbar.LENGTH_LONG).setAction("Yes", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            }).show();
        }
    }
    //endregion

    //region Option Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    //endregion

    //region Navigation Item selected
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_dashboard:
                selectedFragment = DashBoardFragment.newInstance();
                break;
            //Employer
            case R.id.nav_create_item:
                selectedFragment = EditItemFragment.newItemEditorIntent(EditItemFragment.ItemEditorOptions.CREATE_NEW_ITEM, new Item());
                break;
            case R.id.nav_item_list:
                selectedFragment = ItemsFragment.newItemListIntent();
                break;
            case R.id.nav_order_list:
                selectedFragment = OrdersFragment.newInstance();
                break;
            //Employee
            case R.id.nav_order_placement:
                selectedFragment = PlaceOrderFragment.newInstance(this);
                break;
            case R.id.nav_settings:
                selectedFragment = null;
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        //todo when navigation drawer is opened
        displayNavigationData();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        //region change selectedFragment
        final Snackbar snackbar;
        snackbar = Snackbar.make(container, R.string.selected_fragment_onchange_wait, Snackbar.LENGTH_INDEFINITE);
        //we need this, to give drawer some time to allow it to close properly
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onFragmentChanged(selectedFragment);
                snackbar.dismiss();
            }
        }, 1000);
        //endregion

        return true;
    }
    //endregion

    //region New instance of the activity
    public static Intent newInstance(Context context) {
        return new Intent(context, MainActivity.class);
    }
    //endregion

    //region implements ItemsFragmentListener
    @Override
    public void onFragmentChanged(Fragment fragment) {
        if (fragment == null) return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(container.getId(), fragment);
        transaction.commit();
    }
    //endregion


    private static class NavigationDrawerData {
        int todaysOrderCount;
        int itemListSize;

        //region getters and setters

        public int getTodaysOrderCount() {
            return todaysOrderCount;
        }

        public void setTodaysOrderCount(int todaysOrderCount) {
            this.todaysOrderCount = todaysOrderCount;
        }

        public int getItemListSize() {
            return itemListSize;
        }

        public void setItemListSize(int itemListSize) {
            this.itemListSize = itemListSize;
        }


        //endregion
    }

    private static class NavigationDrawerTask extends AsyncTaskLoader<NavigationDrawerData> {

        public NavigationDrawerTask(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Nullable
        @Override
        public NavigationDrawerData loadInBackground() {

            NavigationDrawerData navigationDrawerData = new NavigationDrawerData();

            navigationDrawerData.setItemListSize(new ItemDatabase(getContext()).getCount());

            navigationDrawerData.setTodaysOrderCount(Order.OrderTimelineHelper.getTodaysOrders(getContext()).size());

            return navigationDrawerData;
        }
    }

    private LoaderManager.LoaderCallbacks loaderCallbacks = new android.support.v4.app.LoaderManager.LoaderCallbacks() {
        @NonNull
        @Override
        public android.support.v4.content.Loader onCreateLoader(int id, @Nullable Bundle args) {
            return new NavigationDrawerTask(getApplicationContext());
        }

        @Override
        public void onLoadFinished(@NonNull android.support.v4.content.Loader loader, Object data) {
            NavigationDrawerData navigationDrawerData = (NavigationDrawerData) data;
            nav_order_list.setText(String.valueOf(navigationDrawerData.getTodaysOrderCount()));
            nav_item_list.setText(String.valueOf(navigationDrawerData.getItemListSize()));
            nav_order_list.setBackgroundResource(R.drawable.nav_indicate_circle_background);
            nav_item_list.setBackgroundResource(R.drawable.nav_indicate_circle_background);
        }

        @Override
        public void onLoaderReset(@NonNull android.support.v4.content.Loader loader) {

        }
    };

}
