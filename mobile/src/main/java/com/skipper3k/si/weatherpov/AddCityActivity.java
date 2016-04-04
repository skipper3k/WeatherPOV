package com.skipper3k.si.weatherpov;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;

import com.skipper3k.si.weatherpov.data.WPOVDatabase;
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


    private String city;


    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        final AutoCompleteTextView searchField = (AutoCompleteTextView) findViewById(R.id.searchCity);

        adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null,
                new String[] { "name" },
                new int[] { android.R.id.text1 });

        searchField.setAdapter(adapter);

        /**
         * the proper way to repopulate dropdown list
         */
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                if (constraint == null || constraint.equals(""))
                    return adapter.getCursor();

                if (mWeatherFetcherService != null) {
                    /** makes no sense querying by a single letter or two.. too many results */
                    if (constraint.length() > 2) {
                        return mWeatherFetcherService.searchForCity(constraint.toString());
                    }
                }

                return adapter.getCursor();
            }
        });
        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor c) {
                return c.getString(c.getColumnIndexOrThrow(WPOVDatabase.COLUMN_NAME));
            }
        });


        Button add = (Button) findViewById(R.id.addCity);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(WeatherPOVActivity.ADD_CITY_STRING, city);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
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
