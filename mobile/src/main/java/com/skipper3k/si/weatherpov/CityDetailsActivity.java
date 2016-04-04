package com.skipper3k.si.weatherpov;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.skipper3k.si.weatherpov.data.WPOVCity;
import com.skipper3k.si.weatherpov.data.WeatherFetcherService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by skipper3k on 04/04/16.
 *
 */
public class CityDetailsActivity extends AppCompatActivity {
    private static final String TAG = CityDetailsActivity.class.getSimpleName();
    DateFormat sdf = SimpleDateFormat.getDateTimeInstance();

    private WeatherFetcherService mWeatherFetcherService;
    boolean mBound = false;

    private WPOVCity city;

    private TextView cityName;
    private TextView cityTemp;
    private TextView cityHumidity;
    private TextView cityDescription;
    private TextView lastUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_city);


        Intent intent = getIntent();
        if (intent != null) {
            city = (WPOVCity)intent.getExtras().getSerializable(WeatherPOVActivity.ADD_CITY_STRING);
        }


        cityName = (TextView)findViewById(R.id.cityName);
        cityTemp = (TextView)findViewById(R.id.cityTemp);
        cityHumidity = (TextView)findViewById(R.id.cityHumidity);
        cityDescription = (TextView)findViewById(R.id.cityDescription);
        lastUpdated = (TextView)findViewById(R.id.lastUpdated);

        cityName.setText(city.name);
        cityTemp.setText((city.temp + "°C"));
        cityHumidity.setText(city.humidity);
        cityDescription.setText(city.description);
        lastUpdated.setText(city.lastUpdated != null ? sdf.format(city.lastUpdated) : "never");

        if (!mBound) {
            Intent intentService = new Intent(this, WeatherFetcherService.class);
            bindService(intentService, mConnection, Context.BIND_AUTO_CREATE);
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
