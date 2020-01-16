package com.bluemaestro.utility.sdk.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bluemaestro.utility.sdk.models.Sensor;

import java.util.ArrayList;
import java.util.List;

import static com.bluemaestro.utility.sdk.database.TemperatureTable.COLUMN_ID;
import static com.bluemaestro.utility.sdk.database.TemperatureTable.COLUMN_LATITUDE;
import static com.bluemaestro.utility.sdk.database.TemperatureTable.COLUMN_LONGITUDE;
import static com.bluemaestro.utility.sdk.database.TemperatureTable.COLUMN_TEMP;
import static com.bluemaestro.utility.sdk.database.TemperatureTable.COLUMN_TIMESTAMP;
import static com.bluemaestro.utility.sdk.database.TemperatureTable.TABLE_TEMPERATURE;

/**
 * Created by willem on 12-3-17.
 */

public class TemperatureDatabaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "temperaturetable.db";
    private static final int DATABASE_VERSION = 1;
    private static String dbFilename;

    public TemperatureDatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dbFilename = context.getDatabasePath(DATABASE_NAME).toString();
        Log.i("TDBH", "Temperature database = " + dbFilename);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        TemperatureTable.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        TemperatureTable.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }

    public static String getDbFileName()
    {
        return dbFilename;
    }


    public List<Sensor> getSensorValues()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TEMPERATURE + "", null);
        List<Sensor> sensors = new ArrayList<>();
        if(cursor.moveToFirst())
        {
            do
            {
                Sensor sensor = new Sensor(
                  cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                  cursor.getFloat(cursor.getColumnIndex(COLUMN_TEMP)),
                  cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
                  cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                  cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE))
                );
                sensors.add(sensor);
            } while(cursor.moveToNext());
        }
        return sensors;
    }
}
