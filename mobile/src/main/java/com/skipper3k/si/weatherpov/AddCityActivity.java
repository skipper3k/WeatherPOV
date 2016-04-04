package com.skipper3k.si.weatherpov;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.skipper3k.si.weatherpov.data.WeatherFetcherService;

/**
 * Created by skipper3k on 04/04/16.
 *
 * The 3. activity where one can search cities and add them to the first screen.
 *
 */
public class AddCityActivity extends AppCompatActivity {
    private static final String TAG = AddCityActivity.class.getSimpleName();

    private WeatherFetcherService mWeatherFetcherService;
    boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        EditText searchField = (EditText) findViewById(R.id.searchCity);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(TAG, "text changed: " + s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG, "text changed: " + s);
            }
        });


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
