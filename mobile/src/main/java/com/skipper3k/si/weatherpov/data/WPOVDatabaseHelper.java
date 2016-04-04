package com.skipper3k.si.weatherpov.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.skipper3k.si.weatherpov.helpers.Config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by skipper3k on 03/04/16.
 *
 */
public class WPOVDatabaseHelper {
    private static final String TAG = WPOVDatabaseHelper.class.getSimpleName();

    /**
     * updates an existig city or adds a cutom one to the database
     *
     * @param city city object
     * @param updateWeather if the weather data changes set to true
     */
    public void saveCity(WPOVCity city, boolean updateWeather) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final String sql = " INSERT OR REPLACE INTO " + WPOVDatabase.TABLE_CITY + "("
                + WPOVDatabase.COLUMN_OWM_ID + ", "
                + WPOVDatabase.COLUMN_NAME + ", "
                + WPOVDatabase.COLUMN_TEMP + ", "
                + WPOVDatabase.COLUMN_HUMIDITY + ", "
                + WPOVDatabase.COLUMN_DESCRIPTION + ", "
                + WPOVDatabase.COLUMN_LAST_UPDATED + ", "
                + WPOVDatabase.COLUMN_FAVORED
                + " ) VALUES (?1,?2, ?3, ?4, ?5, ?6, ?7)";

        SQLiteStatement stmt = connection.compileStatement(sql);

        stmt.bindString(1, city.id);
        stmt.bindString(2, city.name.trim());
        stmt.bindLong(3, city.temp);
        stmt.bindLong(4, city.humidity);
        stmt.bindString(5, city.description != null ? city.description : "");
        if (updateWeather) {
            stmt.bindString(6, sdf.format(new Date()));
        } else {
            stmt.bindString(6, city.lastUpdated != null ? sdf.format(city.lastUpdated) : "");
        }
        stmt.bindLong(7, city.favoured ? 1 : 0);
        stmt.execute();
    }

    public List<WPOVCity> favouredCitiesList() {
        List<WPOVCity> cities = new ArrayList<WPOVCity>();

        String selection = WPOVDatabase.COLUMN_FAVORED + " > 0";

        Cursor cursor = connection.query(WPOVDatabase.TABLE_CITY,
                allColumns, selection, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            WPOVCity city = cursorToCity(cursor);
            cities.add(city);
            cursor.moveToNext();
        }
        cursor.close();

        return cities;
    }

    private String[] allColumns = {
            "id as _id",
            WPOVDatabase.COLUMN_OWM_ID,
            WPOVDatabase.COLUMN_NAME,
            WPOVDatabase.COLUMN_TEMP,
            WPOVDatabase.COLUMN_HUMIDITY,
            WPOVDatabase.COLUMN_DESCRIPTION,
            WPOVDatabase.COLUMN_LAST_UPDATED,
            WPOVDatabase.COLUMN_FAVORED
            };

    private SQLiteDatabase connection;

    public WPOVDatabaseHelper(Context context) {
        WPOVDatabase database = new WPOVDatabase(context);
        connection = database.getWritableDatabase();

        connection.enableWriteAheadLogging();
    }

    public void closeConnections() {
        Log.e(TAG, "closing DB connection!");
        connection.close();
    }


    /**
     * This is a bulk save of the cities...
     * We do this only once in this example. Otherwise we check for already existing data and update.
     *
     *
     * @param context context of the app
     * @param cities list of cities
     */
    public boolean saveCities(Context context, Map<String, WPOVCity> cities) {
        if (Config.DEBUG) Log.i(TAG, "Saving to database.");
        long startTime = System.nanoTime();

        boolean success = true;

        final String sql = " INSERT OR REPLACE INTO " + WPOVDatabase.TABLE_CITY + "("
                + WPOVDatabase.COLUMN_OWM_ID + ", "             // 1
                + WPOVDatabase.COLUMN_NAME              // 2
                + " ) VALUES (?1,?2)";
        SQLiteStatement stmt = connection.compileStatement(sql);

        connection.beginTransaction();

        try {
            for (WPOVCity city : cities.values()) {
                stmt.bindString(1, city.id);
                stmt.bindString(2, city.name.trim());
                stmt.execute();
            }

            connection.setTransactionSuccessful();
        } catch (SQLiteException e) {
            success = false;
        }  finally {
            Log.e(TAG, "endTransaction");
            connection.endTransaction();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        if (Config.DEBUG) Log.i(TAG, "Saving cities to database took: " + duration/1000000 + " milis.");
        if (Config.DEBUG) Log.i(TAG, "Search for london: " + searchForCity("lond"));

        return success;
    }

    /**
     *
     * @return number of cities in the database
     */
    public long getCitiesCount() {
        return DatabaseUtils.queryNumEntries(connection, WPOVDatabase.TABLE_CITY);
    }

    /**
     * Search for city
     * @param city city string
     * @return
     */
    public Cursor searchForCity(String city) {
        Log.e(TAG, "search for city: " + city);
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(WPOVDatabase.TABLE_CITY);

        String selection = "lower(" + WPOVDatabase.COLUMN_NAME + ") LIKE ?";
        String[] selectionArgs = new String[] { "%" + city.toLowerCase() + "%"};;

        /**
         * limit to 20 for now
         */
        Cursor cursor = builder.query(connection,
                allColumns, selection, selectionArgs, null, null, null, "20");

        if (cursor == null) {
            Log.e(TAG, "nothing found");
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            Log.e(TAG, "nothing found");
            return null;
        }

        return cursor;
    }

    public Map<String, WPOVCity> getCitiesFromDB(Context context) {
        Map<String, WPOVCity> cities = new HashMap<String, WPOVCity>();

        Cursor cursor = connection.query(WPOVDatabase.TABLE_CITY,
                allColumns, null, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            WPOVCity city = cursorToCity(cursor);
            cities.put(city.name, city);
            cursor.moveToNext();
        }
        cursor.close();

        return cities;
    }

    public static WPOVCity cursorToCity(Cursor cursor) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        WPOVCity city = new WPOVCity();

        city.id = cursor.getString(1);
        city.name = cursor.getString(2);
        city.temp = cursor.getInt(3);
        city.humidity = cursor.getInt(4);
        city.description = cursor.getString(5);

        try {
            if (cursor.getString(6) != null) {
                city.lastUpdated = sdf.parse(cursor.getString(6));
            }
        } catch (ParseException e) {

        }

        city.favoured = cursor.getInt(7) != 0;

        return city;
    }

    private static ContentValues cityToValues(WPOVCity city) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        ContentValues values = new ContentValues();
        values.put(WPOVDatabase.COLUMN_OWM_ID, city.id);
        values.put(WPOVDatabase.COLUMN_NAME, city.name);
        if (city.lastUpdated != null) {
            values.put(WPOVDatabase.COLUMN_LAST_UPDATED, sdf.format(city.lastUpdated));
        } else {
            values.put(WPOVDatabase.COLUMN_LAST_UPDATED, "");
        }
        values.put(WPOVDatabase.COLUMN_TEMP, city.temp);
        values.put(WPOVDatabase.COLUMN_HUMIDITY, city.humidity);
        values.put(WPOVDatabase.COLUMN_DESCRIPTION, city.description);

        return values;
    }
}