package com.bluemaestro.utility.sdk;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bluemaestro.utility.sdk.database.TemperatureTable;
import com.bluemaestro.utility.sdk.models.Sensor;
import com.bluemaestro.utility.sdk.retrofit.ApiUtils;
import com.bluemaestro.utility.sdk.retrofit.CallbackWrapper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by willem on 13-3-17.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter
{
    public static final String TAG = "BlueMaestro";
    ContentResolver mContentResolver;
    Context mContext;
    private static final int MAXIMUM_SENSOR_VALUES = 100;
    private static final int TIME_BETWEEN_REQUESTS = 100;

    public SyncAdapter(
      Context context,
      boolean autoInitialize,
      boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        mContext = context;
        //        ApiUtils.create(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient,
                              SyncResult syncResult)
    {

        Log.d(TAG, "syncing!");
        String[] projection =
          {
            TemperatureTable.COLUMN_ID,
            TemperatureTable.COLUMN_TIMESTAMP,
            TemperatureTable.COLUMN_TEMP,
            TemperatureTable.COLUMN_LATITUDE,
            TemperatureTable.COLUMN_LONGITUDE
          };
//        String selection = TemperatureTable.COLUMN_ID + ">?";
//        String[] selectionArgs = {"0"};
        String sortOrder = TemperatureTable.COLUMN_ID;
        //        Cursor mCursor = mContentResolver.query(ClosenessProvider.CONTENT_URI, projection, null, null, null);
        Cursor mCursor = mContentResolver.query(ClosenessProvider.CONTENT_URI, projection, null, null, sortOrder);
        //        Cursor mCursor = mContentResolver.query(ClosenessProvider.CONTENT_URI, projection, selection,
        //        selectionArgs, sortOrder);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        //        String client_hash = sharedPref.getString(mContext.getString(R.string.private_hash), "");
        String studyNumber = sharedPref.getString("study_number", "0");
        String participantNumber = sharedPref.getString("participant_number", "0");
        //        JSONArray jsonArray = new JSONArray();
        //        try
        //        {
        assert mCursor != null;
        String dropIdClause;
        if(mCursor.moveToFirst())
        {
            do
            {
                dropIdClause = "_ID IN (";
                List<Sensor> sensors = new ArrayList<>();
                do
                {
                    dropIdClause = dropIdClause + mCursor.getInt(mCursor.getColumnIndexOrThrow(TemperatureTable
                      .COLUMN_ID)) + ", ";

                    sensors.add(new Sensor(
                        mCursor.getFloat(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_TEMP)),
                        mCursor.getString(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_TIMESTAMP)),
                        mCursor.getFloat(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_LATITUDE)),
                        mCursor.getFloat(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_LONGITUDE))
                      )
                    );
                    // mCursor must be at the end otherwise it moves twice when reaching MAXIMUM_SENSOR_VALUES
                } while(sensors.size() < MAXIMUM_SENSOR_VALUES && mCursor.moveToNext());

                dropIdClause = dropIdClause.replaceAll(", $", "");
                dropIdClause = dropIdClause + ")";
                Log.d(TAG, dropIdClause);
                //            final String dropIdClause2 = dropIdClause;
                if(!sensors.isEmpty())
                {
                    try {
                        sendDataToServer(studyNumber, participantNumber, sensors, dropIdClause);
                        Thread.sleep(TIME_BETWEEN_REQUESTS);
                    } catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            } while(mCursor.moveToNext());
        }
        mCursor.close();
        ClosenessProvider.releaseLock();
    }

    private void sendDataToServer(final String studyNumber, final String participantNumber,
                                  final List<Sensor> sensors, final String dropIdClause)
    {
        try
        {
            ApiUtils.create(mContext)
              .addSensor(studyNumber, participantNumber, (new Gson())
                .toJsonTree(sensors.subList(0, Math.min(sensors.size(), MAXIMUM_SENSOR_VALUES))))
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribeWith(new CallbackWrapper<retrofit2.Response<Void>>(mContext)
              {
                  @Override
                  protected void onSuccess(retrofit2.Response<Void> voidResponse)
                  {
                      Log.d(TAG, "successfully updated");

                      int rowsDeleted = mContentResolver.delete(ClosenessProvider.CONTENT_URI, dropIdClause, null);
                      Log.d(TAG, "rows deleted: " + rowsDeleted);
                  }

                  @Override
                  public void onError(Throwable e)
                  {
                      super.onError(e);
                      Log.e(TAG, e.toString());
                  }
              })
              .onComplete();
        } catch(Exception e)
        {
            Log.e(TAG, e.toString());
        }
    }
}
