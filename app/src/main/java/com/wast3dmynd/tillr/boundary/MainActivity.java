package com.wast3dmynd.tillr.boundary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainActivityListener {

    //view(s)
    NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private ConstraintLayout container;
    private TextView nav_item_list, nav_order_list;


    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region users preferred day mode
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAutoNightMode = pref.getBoolean(SettingsActivity.GeneralPreferenceFragment.PREF_AUTO_THEME_MODE_KEY, false);
        if (isAutoNightMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        else {
            boolean isNightMode = pref.getBoolean(SettingsActivity.GeneralPreferenceFragment.PREF_NIGHT_MODE_KEY, false);
            if (isNightMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        ////endregion


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

        //display dash board fragment
        Fragment fragment = DashBoardFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(container.getId(), fragment);
        transaction.commit();

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
        NavigationDrawerTask navigationDrawerTask = new NavigationDrawerTask(this);
        navigationDrawerTask.execute();
        try {
            NavigationDrawerData navigationDrawerData = navigationDrawerTask.get();
            nav_order_list.setText(String.valueOf(navigationDrawerData.getTodaysOrderCount()));
            nav_item_list.setText(String.valueOf(navigationDrawerData.getItemListSize()));
            nav_order_list.setBackgroundResource(R.drawable.menu_counter_text_background);
            nav_item_list.setBackgroundResource(R.drawable.menu_counter_text_background);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

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

    private static class NavigationDrawerTask extends AsyncTask<Void, Void, NavigationDrawerData> {
        private Context context;

        public NavigationDrawerTask(Context context) {
            this.context = context;
        }

        @Override
        protected NavigationDrawerData doInBackground(Void... voids) {

            NavigationDrawerData navigationDrawerData = new NavigationDrawerData();

            navigationDrawerData.setItemListSize(new ItemDatabase(context).getCount());

            navigationDrawerData.setTodaysOrderCount(Order.OrderTimelineHelper.getTodaysOrders(context).size());

            return navigationDrawerData;
        }
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

                }
            }).show();
        }
    }
    //endregion

    //region Option Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region Navigation Item selected
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.nav_dashboard:
                fragment = DashBoardFragment.newInstance();
                break;
            //Employer
            case R.id.nav_create_item:
                fragment = EditItemFragment.newItemEditorIntent(EditItemFragment.ItemEditorOptions.CREATE_NEW_ITEM, new Item());
                break;
            case R.id.nav_item_list:
                fragment = ItemsFragment.newItemListIntent();
                break;
            case R.id.nav_order_list:
                fragment = OrdersFragment.newInstance();
                break;
            //Employee
            case R.id.nav_order_placement:
                fragment = PlaceOrderFragment.newInstance(this);
                break;
        }

        //change fragment
        onFragmentChanged(fragment);

        //todo when navigation drawer is opened
        //displayNavigationData();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(container.getId(), fragment);
        transaction.commit();
    }
    //endregion

}
