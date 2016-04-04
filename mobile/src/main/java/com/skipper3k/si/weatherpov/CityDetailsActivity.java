package com.skipper3k.si.weatherpov;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.skipper3k.si.weatherpov.data.WPOVCity;
import com.skipper3k.si.weatherpov.data.WeatherFetcherService;

/**
 * Created by skipper3k on 04/04/16.
 *
 */
public class CityDetailsActivity extends AppCompatActivity {
    private static final String TAG = CityDetailsActivity.class.getSimpleName();

    private WeatherFetcherService mWeatherFetcherService;
    boolean mBound = false;


    private WPOVCity city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_city);



        if (!mBound) {
            Intent intent = new Intent(this, WeatherFetcherService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private void updateCityWeather() {
        if (mWeatherFetcherService != null) {

        }
    }

    /**
     service connection
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            WeatherFetcherService.WeatherFetcherServiceBinder binder = (WeatherFetcherService.WeatherFetcherServiceBinder) service;
            mWeatherFetcherService = binder.getService();
            mBound = true;

            Log.i(TAG, "service bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
