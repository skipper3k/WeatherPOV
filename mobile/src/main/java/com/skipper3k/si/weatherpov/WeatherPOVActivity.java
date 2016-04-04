package com.skipper3k.si.weatherpov;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.skipper3k.si.weatherpov.data.WPOVCity;
import com.skipper3k.si.weatherpov.data.WPOVDatabase;
import com.skipper3k.si.weatherpov.data.WeatherFetcherService;
import com.skipper3k.si.weatherpov.helpers.CitiesRecyclerViewAdapter;
import com.skipper3k.si.weatherpov.helpers.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Main Android activity
 *
 * TODO:
 *  - Recycleview list + adapter
 *  - Toggle listview/no cities message
 *  - Connect to service to access data
 *
 *
 */
public class WeatherPOVActivity extends AppCompatActivity {
    private static final String TAG = WeatherPOVActivity.class.getSimpleName();

    /**
     *  This is the main fetcher service. This is where we get the data from.
     */
    private WeatherFetcherService mWeatherFetcherService;
    boolean mBound = false;

    private int ADD_CITY_ACTIVITY = 123;

    public static String ADD_CITY_STRING = "add.city";

    private FloatingActionButton fab;

    private View rootView;

    /**
     * view for empty list
     */
    private View mNoList;
    /**
     * Recyclerview for displaying and editing the list of cities
     */
    private RecyclerView mCitiesList;

    private CitiesRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipeRefreshLayout refreshLayout;

    private List<WPOVCity> citiesList;

    private Map<Integer, WPOVCity> removeCitiesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_pov);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rootView = findViewById(R.id.root_view);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addCityIntent = new Intent(WeatherPOVActivity.this, AddCityActivity.class);
                startActivityForResult(addCityIntent, ADD_CITY_ACTIVITY);
            }
        });

        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        refreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // force reload from web
                fetchFavouriteCities(true);
            }
        });

        mNoList = findViewById(R.id.no_cities);
        mCitiesList = (RecyclerView) findViewById(R.id.cities_list);


        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mCitiesList.setLayoutManager(mLayoutManager);

        /**
         * init with an empty list and populate after we get the data...
         */
        mAdapter = new CitiesRecyclerViewAdapter(new ArrayList<WPOVCity>(), this);
        mCitiesList.setAdapter(mAdapter);


        removeCitiesList = new HashMap<>();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int position = viewHolder.getAdapterPosition();
                WPOVCity city = citiesList.get(position);
                city.favoured = false;
                citiesList.remove(position);

                mAdapter.notifyItemRemoved(position);

                if (citiesList.size() < 1) {
                    toggleNoCities(true);
                }

                /** save city for remove */
                final WPOVCity removeCity = city;
                removeCitiesList.put(position, city);



                Snackbar.make(rootView, getString(R.string.remove_from_favs, city.name), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (position > citiesList.size()) {
                                    citiesList.add(removeCitiesList.get(position));
                                    mAdapter.notifyItemInserted(citiesList.size());
                                } else {
                                    citiesList.add(position, removeCitiesList.get(position));
                                    mAdapter.notifyItemInserted(position);
                                }
                                // undo for the last element was not working
                                toggleNoCities(false);
                                removeCitiesList.remove(position);
                            }
                        })
                    /**
                     * city is actually dismissed when snackbar hides
                     */
                        .setCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (removeCitiesList.containsKey(position)) {
                                    mWeatherFetcherService.saveCity(removeCitiesList.get(position));
                                    removeCitiesList.remove(position);
                                }

                                super.onDismissed(snackbar, event);
                            }

                            @Override
                            public void onShown(Snackbar snackbar) {
                                super.onShown(snackbar);
                            }
                        }).show();

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mCitiesList);





        mCitiesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }
            }
        );

        toggleNoCities(true);

        if (!mBound) {
            Intent intent = new Intent(this, WeatherFetcherService.class);
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * get all saved cities from db and add them to the recyclerviewe adapter
     */
    private void fetchFavouriteCities() {
        fetchFavouriteCities(false);
    }
    private void fetchFavouriteCities(boolean online) {
        citiesList = mWeatherFetcherService.favouriteCitiesList();

        if (Config.DEBUG) {
            for (WPOVCity city : citiesList) {
                Log.i(TAG, "citi : " + city.id + ", " + city.name + ", description: " + city.description + ", temp: " + city.temp + ", humidity: " + city.humidity
                        + ", last updated: " + city.lastUpdated);
            }
        }

        if (citiesList.size() > 0) {
            mAdapter.setData(citiesList);
            mAdapter.notifyDataSetChanged();
        }

        updateFavouriteCitiesWeather(online);
    }
    private void updateFavouriteCitiesWeather(boolean online) {
        if (citiesList.size() > 0) {
            // set state to refreshing
            toggleNoCities(false);

            boolean timedUpdate = false;
            // todo: check last update and update only if necessary

            if (online || timedUpdate) {
                refreshLayout.setRefreshing(true);
                mWeatherFetcherService.fetchCitiesWeather(citiesList, new WeatherFetcherService.WeatherFetcherListener() {
                    @Override
                    public void citiesLoaded(Map<String, WPOVCity> cities) {

                    }

                    @Override
                    public void weatherUpdated(List<WPOVCity> cities) {
                        refreshLayout.setRefreshing(false);
                        // refetch data from db
                        fetchFavouriteCities();
                    }

                    @Override
                    public void errorUpdating() {
                        refreshLayout.setRefreshing(false);
                        Snackbar.make(rootView, getString(R.string.error_fetch), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
            }
        }
    }

    /**
     *
     * @param nocities true if we have no favourites, false if we do not
     */
    private void toggleNoCities(boolean nocities) {
        if (nocities) {
            mNoList.setVisibility(View.VISIBLE);
            refreshLayout.setVisibility(View.GONE);
        } else {
            mNoList.setVisibility(View.GONE);
            refreshLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     fetch cities list in background
     */
    private void fetchCities() {
        mWeatherFetcherService.fetchCitiesList(new WeatherFetcherService.WeatherFetcherListener() {
            @Override
            public void citiesLoaded(Map<String, WPOVCity> cities) {
                Snackbar.make(fab, "Cities successfully loaded!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                // plainly reload favs
                fetchFavouriteCities();
            }

            @Override
            public void weatherUpdated(List<WPOVCity> cities) {

            }

            @Override
            public void errorUpdating() {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_CITY_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                // refresh list !
                if (data != null) {

                    WPOVCity citi = (WPOVCity) data.getExtras().getSerializable(ADD_CITY_STRING);
                    Log.i(TAG, "citi: " + citi.name + " fav: " + citi.favoured);

                    Snackbar.make(fab, getString(R.string.favourite_added, citi.name), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    fetchFavouriteCities(true);
                }
            }
        }
    }

    /**
        service connection
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            WeatherFetcherService.WeatherFetcherServiceBinder binder = (WeatherFetcherService.WeatherFetcherServiceBinder) service;
            mWeatherFetcherService = binder.getService();
            mBound = true;

            if (Config.LOAD_CITIES) fetchCities();
            fetchFavouriteCities();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather_pov, menu);
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
}
