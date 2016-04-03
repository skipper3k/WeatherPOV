package com.skipper3k.si.weatherpov.data;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.skipper3k.si.weatherpov.helpers.Config;

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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public Map<String, String> getCities() {
        return cities;
    }

    private Map<String, String> cities;


    private boolean FETCHING_CITIES;



    public interface RequestStringListener {
        void requestFinished(JSONObject json);
        void requestFinished(JSONArray json);
        void requestFinishedString(String html);
        void requestFailed();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Starting service");

        /**
         * Load cities from db or the web ...
         */
        if (cities == null) {
            fetchCitiesList();
        }

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

        if (FETCHING_CITIES) return;

        FETCHING_CITIES = true;
        RequestHtmlAsString task = new RequestHtmlAsString(new RequestStringListener() {
            @Override
            public void requestFinished(JSONObject json) {

            }

            @Override
            public void requestFinished(JSONArray json) {

            }

            @Override
            public void requestFinishedString(String html) {
                parseCitiesList(html);
            }

            @Override
            public void requestFailed() {
                Log.e(TAG, "Could not fetch a list of cities. Retry in 1 minute.");
            }
        });
        task.execute(cityListURL);
    }

    /**
     * Parse the text file of all the cities. This is not bulletproof ...
     *
     * We only parse the city name and id because it is relevant to this app.
     * We could make a data structure and save the coordinates as well...
     *
     * fixme: slow, add to async processing, because this runs on the main thread and is UI blocking! and find a regex ninja! ...
     *
     * @param citiesString list of cities fetched via cityListURL
     */
    private void parseCitiesList(String citiesString) {
        if (cities == null) cities = new HashMap<String, String>();

        long startTime = System.nanoTime();

        String citiesSplit[] = citiesString.split("\\r?\\n");

        Log.i(TAG, "parsing cities! " + citiesSplit.length);

        Pattern pId = Pattern.compile("^[0-9]*");
        Pattern pCity = Pattern.compile("^[[a-z] [A-Z]-]*");


        for (int i = 1; i < citiesSplit.length; i++) {
            String line = citiesSplit[i];

            Matcher matcher = pId.matcher(line);
            String id = null;

            while (matcher.find()) {
                line = line.substring(matcher.end(), line.length()).trim();
                id = matcher.group();
            }

            Matcher citiMatch = pCity.matcher(line);
            String city = null;

            while (citiMatch.find()) {
                city = citiMatch.group();
                if (i%500 == 0) Log.e(TAG, "found city: " + city + " in : " + line);
            }

            if (id != null && city != null) {
                cities.put(city, id);
            }
        }


        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        if (Config.DEBUG) Log.i(TAG, "parsing cities took: " + duration/1000000 + " milis.");

        saveCitiesList(cities);
    }

    /**
     * TODO: add database!
     *
     * @param citiesList list of cities with ids
     */
    private void saveCitiesList(Map<String, String> citiesList) {

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
