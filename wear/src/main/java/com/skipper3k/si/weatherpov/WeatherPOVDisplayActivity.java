package com.skipper3k.si.weatherpov;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class WeatherPOVDisplayActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_pov_display);
        mTextView = (TextView) findViewById(R.id.text);
    }
}
