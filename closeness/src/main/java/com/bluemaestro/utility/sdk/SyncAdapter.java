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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bluemaestro.utility.sdk.database.TemperatureTable;
import com.bluemaestro.utility.sdk.models.Sensor;
import com.bluemaestro.utility.sdk.retrofit.ApiUtils;
import com.bluemaestro.utility.sdk.retrofit.CallbackWrapper;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.bluemaestro.utility.sdk.database.Database.ID_PARTICIPANT_FIELD;
import static com.bluemaestro.utility.sdk.database.Database.ID_STUDY_FIELD;
import static com.bluemaestro.utility.sdk.database.Database.IS_PARTNER_CLOSE_FIELD;
import static com.bluemaestro.utility.sdk.database.Database.TEMPERATURE_FIELD;
import static com.bluemaestro.utility.sdk.database.Database.TIMESTAMP_DEVICE_FIELD;
import static com.bluemaestro.utility.sdk.retrofit.WebService.ADD_SENSOR_URL;

/**
 * Created by willem on 13-3-17.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter
{
    public static final String TAG = "BlueMaestro";
    ContentResolver mContentResolver;
    Context mContext;
    private static final int MAXIMUM_SENSOR_VALUES = 100;

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
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult)
    {
        //        if(false)
        //        {

        Log.d(TAG, "syncing!");
        String[] projection = {TemperatureTable.COLUMN_ID, TemperatureTable.COLUMN_TIMESTAMP, TemperatureTable.COLUMN_TEMP,
                TemperatureTable.COLUMN_PARTNER};
        String selection = TemperatureTable.COLUMN_ID + ">?";
        String[] selectionArgs = {"0"};
        String sortOrder = "";
        Cursor mCursor = mContentResolver.query(ClosenessProvider.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        //        String client_hash = sharedPref.getString(mContext.getString(R.string.private_hash), "");
        String studyNumber = sharedPref.getString("study_number", "0");
        String participantNumber = sharedPref.getString("participant_number", "0");
        String dropIdClause = "_ID IN (";
        //        JSONArray jsonArray = new JSONArray();
        List<Sensor> sensors = new ArrayList<>();
        //        try
        //        {
        while(mCursor.moveToNext())
        {
            dropIdClause = dropIdClause + String.valueOf(
                    mCursor.getInt(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_ID))) + ", ";

            sensors.add(new Sensor(
                    mCursor.getFloat(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_TEMP)),
                    (mCursor.getInt(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_PARTNER)) > 0),
                    mCursor.getString(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_TIMESTAMP)))
            );

            //                JSONObject item = new JSONObject();
            //                //                item.put("client_hash", client_hash);
            //                //                item.put(ID_PARTICIPANT_FIELD, Integer.valueOf(participantNumber));
            //                //                item.put(ID_STUDY_FIELD, Integer.valueOf(studyNumber));
            //                String timestamp = mCursor.getString(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_TIMESTAMP));
            //                item.put(TIMESTAMP_DEVICE_FIELD, timestamp);
            //                float temp = mCursor.getFloat(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_TEMP));
            //                item.put(TEMPERATURE_FIELD, temp);
            //                boolean isPartnerClose = (mCursor.getInt(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_PARTNER)) > 0);
            //                item.put(IS_PARTNER_CLOSE_FIELD, isPartnerClose);
            //                jsonArray.put(item);
        }
        mCursor.close();

        dropIdClause = dropIdClause.replaceAll(", $", "");
        dropIdClause = dropIdClause + ")";
        final String dropIdClause2 = dropIdClause;
        if(!sensors.isEmpty())
        {
            sendDataToServer(studyNumber, participantNumber, sensors, dropIdClause2);
        }
        //        } catch(JSONException e)
        //        {
        //            e.printStackTrace();
        //        } finally
        //        {
        //            mCursor.close();
        //        }
        //        }
    }

    private void sendDataToServer(final String studyNumber, final String participantNumber, final List<Sensor> sensors, final String dropIdClause2)
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

                            if(sensors.size() > MAXIMUM_SENSOR_VALUES)
                            {
                                List<Sensor> sensorsTemp = sensors.subList(MAXIMUM_SENSOR_VALUES, sensors.size());
                                sendDataToServer(studyNumber, participantNumber, sensorsTemp, dropIdClause2);
                            } else
                            {
                                int rowsDeleted = mContentResolver.delete(ClosenessProvider.CONTENT_URI, dropIdClause2, null);
                                Log.d(TAG, String.valueOf(rowsDeleted));
                            }
                        }
                    })
                    .onComplete();
        } catch(Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    //        @Override
    public void onPerformSync2(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult)
    {
        //        Read from ContentProvider
        Log.d(TAG, "syncing!");
        String[] projection = {TemperatureTable.COLUMN_ID, TemperatureTable.COLUMN_TIMESTAMP, TemperatureTable.COLUMN_TEMP,
                TemperatureTable.COLUMN_PARTNER};
        String selection = TemperatureTable.COLUMN_ID + ">?";
        String[] selectionArgs = {"0"};
        String sortOrder = "";
        Cursor mCursor = mContentResolver.query(ClosenessProvider.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        //        String client_hash = sharedPref.getString(mContext.getString(R.string.private_hash), "");
        String studyNumber = sharedPref.getString("study_number", "0");
        String participantNumber = sharedPref.getString("participant_number", "0");
        String dropIdClause = "_ID IN (";
        JSONArray jsonArray = new JSONArray();
        try
        {
            while(mCursor.moveToNext())
            {
                dropIdClause = dropIdClause + String.valueOf(
                        mCursor.getInt(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_ID))) + ", ";
                JSONObject item = new JSONObject();
                //                item.put("client_hash", client_hash);
                item.put(ID_PARTICIPANT_FIELD, Integer.valueOf(participantNumber));
                item.put(ID_STUDY_FIELD, Integer.valueOf(studyNumber));
                String timestamp = mCursor.getString(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_TIMESTAMP));
                item.put(TIMESTAMP_DEVICE_FIELD, timestamp);
                float temp = mCursor.getFloat(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_TEMP));
                item.put(TEMPERATURE_FIELD, temp);
                boolean isPartnerClose = (mCursor.getInt(mCursor.getColumnIndexOrThrow(TemperatureTable.COLUMN_PARTNER)) > 0);
                item.put(IS_PARTNER_CLOSE_FIELD, isPartnerClose);
                jsonArray.put(item);
            }
        } catch(JSONException e)
        {
            e.printStackTrace();
        } finally
        {
            mCursor.close();
        }

        dropIdClause = dropIdClause.replaceAll(", $", "");
        dropIdClause = dropIdClause + ")";
        final String dropIdClause2 = dropIdClause;
        final String jsonContent = jsonArray.toString();
        if(!jsonContent.equals("[]"))
        {

            RequestQueue queue = Volley.newRequestQueue(mContext);
            String url = sharedPref.getString("server_url_main", "");
            //FIXME API structure hard coded...
            url = url + ADD_SENSOR_URL;

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.PUT, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {
                            response = response.replaceAll("\\s$", "");
                            response = response.replaceAll("\"", "");
                            Log.d(TAG, response);
                            if(response.length() == 2)
                            {
                                Log.d(TAG, "All fine, proceed with deletion");
                                int rowsDeleted = mContentResolver.delete(ClosenessProvider.CONTENT_URI, dropIdClause2, null);
                                Log.d(TAG, String.valueOf(rowsDeleted));
                            } else
                            {
                                Log.d(TAG, "Something wrong");
                            }
                        }
                    }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.d(TAG, error.toString());
                    Log.d(TAG, "That didn't work!");
                }
            })
            {
                @Override
                public String getBodyContentType()
                {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("data", jsonContent);
                    return params;
                }
            };
            // Add the request to the RequestQueue.
            stringRequest.setRetryPolicy(new RetryPolicy()
            {
                @Override
                public int getCurrentTimeout()
                {
                    // Here goes the new timeout
                    return 30000;
                }

                @Override
                public int getCurrentRetryCount()
                {
                    // The max number of attempts
                    return 1;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError
                {
                    // Here you could check if the retry count has gotten
                    // To the max number, and if so, send a VolleyError msg
                    // or something
                }
            });
            queue.add(stringRequest);
        }
    }
}
