package com.skipper3k.si.weatherpov;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.skipper3k.si.weatherpov.data.WPOVCity;
import com.skipper3k.si.weatherpov.data.WPOVDatabase;
import com.skipper3k.si.weatherpov.data.WeatherFetcherService;
import com.skipper3k.si.weatherpov.helpers.Config;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_pov);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, mBound ? "Service is connected." : "Service is not connected.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                if (Config.DEBUG && mBound) {
                    // Acts as a test button too!
//                    if (mWeatherFetcherService != null) mWeatherFetcherService.searchForCity("francisc");
                }

                Intent addCityIntent = new Intent(WeatherPOVActivity.this, AddCityActivity.class);
                startActivityForResult(addCityIntent, ADD_CITY_ACTIVITY);
            }
        });


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
        List<WPOVCity> cities = mWeatherFetcherService.favouriteCitiesList();
        for (WPOVCity city : cities) {
            Log.i(TAG, "favourite city: " + city.name);
        }
    }

    private void fetchCities() {
        /**
         fetch cities list in background
         */
        mWeatherFetcherService.fetchCitiesList(new WeatherFetcherService.WeatherFetcherListener() {

            @Override
            public void citiesLoaded(Map<String, WPOVCity> cities) {
                cities.clear();

                if (Config.DEBUG) {
                    Snackbar.make(fab, cities != null ? "Cities successfully loaded!" : "Cities load failed!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

            @Override
            public void fetchedWeather(WPOVCity city) {

            }

            @Override
            public void searchFound(List<WPOVCity> cities) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_CITY_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                // refresh list !
                if (data != null) {

                    WPOVCity citi = (WPOVCity)data.getExtras().getSerializable(ADD_CITY_STRING);
                    Log.i(TAG, "citi: " + citi.name + " fav: " + citi.favoured);

                    /**
                     * todo: undo action
                     */
                    Snackbar.make(fab, "You added " + citi.name, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    fetchFavouriteCities();
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

            fetchCities();
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
