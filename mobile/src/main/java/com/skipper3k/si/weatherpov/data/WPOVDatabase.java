package com.skipper3k.si.weatherpov.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by skipper3k on 03/04/16.
 *
 * Will use the sqlite db to store data
 */
public class WPOVDatabase extends SQLiteOpenHelper {
    private static final String TAG = WPOVDatabase.class.getSimpleName();

    public WPOVDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final String TABLE_CITY = "city";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TEMP = "temperature";
    public static final String COLUMN_HUMIDITY = "humidity";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_LAST_UPDATED = "updated";
    public static final String COLUMN_FAVORED = "favoured";


    private static final String DATABASE_NAME = "cities.db";
    private static final int DATABASE_VERSION = 2;

    // todo_tag table create statement
    private static final String CREATE_TABLE_CITY = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CITY + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " text, "
            + COLUMN_TEMP + " integer, " + COLUMN_HUMIDITY + " integer, "
            + COLUMN_DESCRIPTION + " text, " + COLUMN_LAST_UPDATED + " TIMESTAMP, "
            + COLUMN_FAVORED + " integer DEFAULT 0 )";

    private static final String ALTER_TABLE_CITY = "ALTER TABLE " + TABLE_CITY + " ADD COLUMN " + COLUMN_FAVORED + " INTEGER DEFAULT 0";


    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.e(TAG, "Create table!");
        database.execSQL(CREATE_TABLE_CITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i2) {

    }
}
