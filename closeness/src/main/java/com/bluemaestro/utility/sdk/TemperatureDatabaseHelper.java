package com.bluemaestro.utility.sdk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by willem on 12-3-17.
 */

public class TemperatureDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "temperaturetable.db";
    private static final int DATABASE_VERSION = 1;
    private static String dbFilename;

    public TemperatureDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dbFilename = context.getDatabasePath(DATABASE_NAME).toString();
        Log.i("TDBH","Temperature database = " + dbFilename);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        TemperatureTable.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        TemperatureTable.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }

    public static String getDbFileName() {
        return dbFilename;
    }
}
