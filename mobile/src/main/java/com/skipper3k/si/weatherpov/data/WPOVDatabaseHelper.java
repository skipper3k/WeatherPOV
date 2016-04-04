package com.skipper3k.si.weatherpov.data;

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
import java.util.List;
import java.util.Map;

/**
 * Created by skipper3k on 03/04/16.
 *
 *  Database helper class. Here we manipulate with data.
 */
public class WPOVDatabaseHelper {
    private static final String TAG = WPOVDatabaseHelper.class.getSimpleName();

    public WPOVDatabaseHelper(Context context) {
        WPOVDatabase database = new WPOVDatabase(context);
        connection = database.getWritableDatabase();
        connection.enableWriteAheadLogging();
    }

    private SQLiteDatabase connection;

    private String[] allColumns = {
            "id as _id",
            WPOVDatabase.COLUMN_ID,
            WPOVDatabase.COLUMN_NAME,
            WPOVDatabase.COLUMN_TEMP,
            WPOVDatabase.COLUMN_HUMIDITY,
            WPOVDatabase.COLUMN_DESCRIPTION,
            WPOVDatabase.COLUMN_LAST_UPDATED,
            WPOVDatabase.COLUMN_FAVORED
    };

    /**
     * updates an existig city or adds a cutom one to the database
     * We use this update call for every city update (favored, unfavoured, climate data change ...)
     *
     * @param city city object
     * @param updateWeather if the weather data changes set to true
     */
    public void saveCity(WPOVCity city, boolean updateWeather) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final String sqlStatement = "INSERT OR REPLACE INTO " + WPOVDatabase.TABLE_CITY + "("
                + WPOVDatabase.COLUMN_ID + ", "
                + WPOVDatabase.COLUMN_NAME + ", "
                + WPOVDatabase.COLUMN_TEMP + ", "
                + WPOVDatabase.COLUMN_HUMIDITY + ", "
                + WPOVDatabase.COLUMN_DESCRIPTION + ", "
                + WPOVDatabase.COLUMN_LAST_UPDATED + ", "
                + WPOVDatabase.COLUMN_FAVORED
                + ") VALUES (?1,?2, ?3, ?4, ?5, ?6, ?7)";

        SQLiteStatement stmt = connection.compileStatement(sqlStatement);

        stmt.bindLong(1, city.id);
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

        try {
            stmt.execute();
        } catch (Exception e) {
            Log.e(TAG, "cannot update city", e);
        }


        stmt.close();
    }

    /**
     * Construct a list of favoured cities to display in recycle viewer.
     * Could also use just the cursor...
     *
     * @return Custom array list with favoured cities and weather data
     */
    public List<WPOVCity> favouredCitiesList() {
        List<WPOVCity> cities = new ArrayList<>();

        String selection = WPOVDatabase.COLUMN_FAVORED + " = 1";

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
                + WPOVDatabase.COLUMN_ID + ", "             // 1
                + WPOVDatabase.COLUMN_NAME              // 2
                + " ) VALUES (?1,?2)";
        SQLiteStatement stmt = connection.compileStatement(sql);

        connection.beginTransaction();

        try {
            for (WPOVCity city : cities.values()) {
                stmt.bindLong(1, city.id);
                stmt.bindString(2, city.name.trim());
                stmt.execute();
            }

            connection.setTransactionSuccessful();
        } catch (SQLiteException e) {
            success = false;
        }  finally {
            stmt.close();
            connection.endTransaction();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        if (Config.DEBUG) Log.i(TAG, "Saving cities to database took: " + duration/1000000 + " milis.");

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
        String[] selectionArgs = new String[] { city.toLowerCase() + "%"};

        /**
         * limit to 30 for now
         */
        Cursor cursor = builder.query(connection,
                allColumns, selection, selectionArgs, null, null, "name asc", "30");

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

    public static WPOVCity cursorToCity(Cursor cursor) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        WPOVCity city = new WPOVCity();

        city.id = cursor.getInt(1);
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
}