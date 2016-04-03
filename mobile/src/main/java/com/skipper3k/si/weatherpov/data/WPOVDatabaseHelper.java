package com.skipper3k.si.weatherpov.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.skipper3k.si.weatherpov.helpers.Config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by skipper3k on 03/04/16.
 *
 */
public class WPOVDatabaseHelper {
    private static final String TAG = WPOVDatabaseHelper.class.getSimpleName();

    public void saveCity(WPOVDatabase db, WPOVCity city) {

    }

    public void updateCity(WPOVDatabase db, WPOVCity city) {

    }

    private String[] allColumns = {
            WPOVDatabase.COLUMN_OWM_ID,
            WPOVDatabase.COLUMN_NAME,
            WPOVDatabase.COLUMN_TEMP,
            WPOVDatabase.COLUMN_HUMIDITY,
            WPOVDatabase.COLUMN_DESCRIPTION,
            WPOVDatabase.COLUMN_LAST_UPDATED
            };

    /**
     * This is a bulk save of the cities...
     * We do this only once in this example. Otherwise we check for already existing data and update.
     *
     *
     * @param context context of the app
     * @param cities list of cities
     */
    public void saveCities(Context context, Map<String, WPOVCity> cities) {
        if (Config.DEBUG) Log.i(TAG, "Saving to database.");
        long startTime = System.nanoTime();

        WPOVDatabase database = new WPOVDatabase(context);
        SQLiteDatabase db = database.getWritableDatabase();

        db.beginTransaction();

        final String sql = " INSERT OR REPLACE INTO " + WPOVDatabase.TABLE_CITY + "("
                + " owmid,"             // 1
                + " name"              // 2
                + " ) VALUES (?1,?2)";
        SQLiteStatement stmt = db.compileStatement(sql);

        db.beginTransaction();
        try {
            try {
                for (WPOVCity city : cities.values()) {
                    stmt.bindString(1, city.id);
                    stmt.bindString(2, city.name);
                    stmt.execute();
                }

                db.setTransactionSuccessful();

            } catch (SQLiteException e) {


            }
        } finally {
            db.endTransaction();
            stmt.close();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        if (Config.DEBUG) Log.i(TAG, "Sqving cities to database took: " + duration/1000000 + " milis.");
        if (Config.DEBUG) Log.i(TAG, "Saved cities to database.");

        db.close();
    }

    /**
     *      *
     * @param context
     * @return number of cities in the database
     */
    public long getCitiesCount(Context context) {
        WPOVDatabase database = new WPOVDatabase(context);
        SQLiteDatabase db = database.getReadableDatabase();

        long cnt  = DatabaseUtils.queryNumEntries(db, WPOVDatabase.TABLE_CITY);
        db.close();
        return cnt;
    }

    public WPOVCity getCityFromDB(Context context, String id) {
        WPOVDatabase database = new WPOVDatabase(context);
        SQLiteDatabase db = database.getReadableDatabase();

        Cursor cursor = db.query(WPOVDatabase.TABLE_CITY,
                allColumns, null, null, null, null, null, null);

        cursor.moveToFirst();
        return cursorToCity(cursor);
    }


    public Map<String, WPOVCity> getCitiesFromDB(Context context) {
        WPOVDatabase database = new WPOVDatabase(context);
        SQLiteDatabase db = database.getReadableDatabase();

        Map<String, WPOVCity> cities = new HashMap<String, WPOVCity>();

        Cursor cursor = db.query(WPOVDatabase.TABLE_CITY,
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

    private WPOVCity cursorToCity(Cursor cursor) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        WPOVCity city = new WPOVCity();

        city.id = cursor.getString(0);
        city.name = cursor.getString(1);
        city.temp = cursor.getInt(2);
        city.humidity = cursor.getInt(3);
        city.description = cursor.getString(4);

        try {
            city.lastUpdated = sdf.parse(cursor.getString(5));
        } catch (ParseException e) {

        }

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