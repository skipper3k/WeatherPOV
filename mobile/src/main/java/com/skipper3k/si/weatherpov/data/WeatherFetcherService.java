package com.skipper3k.si.weatherpov.data;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by skipper3k on 03/04/16.
 *
 * This is a service which polls the data from openweathermap apis and stores them locally.
 *
 *
 * API RULES:
 *  - Do not send requests more then 1 time per 10 minutes from one device/one API key.
 *      Normally the weather is not changing so frequently.
 *      (this is just an advice, actual limit is 60 cals per minute)
 *  - Use the name of the server as api.openweathermap.org.
 *  - Call API by city ID instead of city name, city coordinates or zip code.
 *      In this case you get precise respond exactly for your city.
 *  - Free account has limitation of capacity and data availability.
 *      If you do not get respond from server do not try to repeat your request immediately, but only after 10 min.
 *      Also we recommend to store your previous request data.
 *
 */
public class WeatherFetcherService extends Service {
    private static final String TAG = WeatherFetcherService.class.getSimpleName();

    /** service binder */
    IBinder mBinder = new WeatherFetcherServiceBinder();

    /**
     * per recomendation from the openweathermap api
     */
    private static final int API_TIMEOUT_MIN = 10;
    private static final String API_BASE_URL = "http://api.openweathermap.org";


    /**
     * I would consider this a hack. But was the best way I could find to get all the cities in the OpenWeatherMap database.
     *
     * No API call for cities?
     *  - http://stackoverflow.com/questions/16560350/how-to-populate-a-jquery-mobile-list-view-of-cities-from-open-weather-map-api
     *
     *
     */
    private static final String cityListURL = "http://openweathermap.org/help/city_list.txt";

    /**
     * TODO: this could be obfuscated
     */
    private static final String API_KEY = "79bfc35c0e64f6e2364384b6b2f5a1f3";


    public interface RequestStringListener {
        void requestFinished(JSONObject json);
        void requestFinished(JSONArray json);
        void requestFinishedString(String html);
        void requestFailed();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Starting service");

        return super.onStartCommand(intent, flags, startId);
    }


    /**
     *
     * @param cid
     */
    public void fetchWeatherForCityId(String cid) {

    }

    /**
     * we get the list of cities every now and then and save it locally for further use
     */
    public void fetchCitiesList() {
        RequestHtmlAsString task = new RequestHtmlAsString(new RequestStringListener() {
            @Override
            public void requestFinished(JSONObject json) {

            }

            @Override
            public void requestFinished(JSONArray json) {

            }

            @Override
            public void requestFinishedString(String html) {
                Log.i(TAG, "Got a list of cities! " + html);
            }

            @Override
            public void requestFailed() {
                Log.e(TAG, "Could not fetch a list of cities. Retry in 1 minute.");
            }
        });
        task.execute(cityListURL);
    }

    private static class RequestHtmlAsString extends AsyncTask<String, Void, String> {
        private RequestStringListener mListener;
        public RequestHtmlAsString(RequestStringListener l) {
            mListener = l;
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            HttpURLConnection connection = getHttpConnectionFromUrl(urlString);

            if (connection == null)
                return null;

            InputStream is = getInputStreamFromHttp(connection);
            try {
                if (is != null) {
                    //get response to String
                    StringBuilder builder = new StringBuilder();
                    String response = "";
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                            builder.append("\n");
                        }

                        response = builder.toString();

                        is.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Exception getting stream", e);
                    }

                    //TODO: handle response from server
                    return response;
                }
                return null;
            } finally {
                connection.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(s == null || "".equals(s)) {
                //TODO: implement fail case
                if (mListener != null) mListener.requestFailed();
                Log.e(TAG, "Request failed!");
                return;
            }

            // fixme: just try for the type ...
            try {
                if (mListener != null) mListener.requestFinished(new JSONObject(s));
            } catch (JSONException e) {
                try {
                    if (mListener != null) mListener.requestFinished(new JSONArray(s));
                } catch (JSONException e2) {
                    if (mListener != null) mListener.requestFinishedString(s);
                }
            }
        }
    }


    public class WeatherFetcherServiceBinder extends Binder {
        public WeatherFetcherService getService() {
            return WeatherFetcherService.this;
        }
    }

    public String sayHi() {
        return "Hello world!";
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private static HttpURLConnection getHttpConnectionFromUrl(String urlString){
        HttpURLConnection connection;
        URL url;
        try {
            url = new URL(urlString);
            connection = (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return connection;
    }

    private static InputStream getInputStreamFromHttp(HttpURLConnection connection){
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept-Charset", "UTF8");
            connection.setConnectTimeout(20000);

            connection.connect();

            return connection.getInputStream();
        } catch (MalformedURLException e) {
            //url name
            e.printStackTrace();
        } catch (IOException e) {
            //open connection
            e.printStackTrace();
        }
        return null;
    }
}
