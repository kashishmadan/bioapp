/*
 * Copyright (c) 2016, Blue Maestro
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from
 * this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.bluemaestro.utility.sdk;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bluemaestro.utility.sdk.adapter.MessageAdapter;
import com.bluemaestro.utility.sdk.databinding.MainBinding;
import com.bluemaestro.utility.sdk.service.TemperatureService;
import com.bluemaestro.utility.sdk.views.dialogs.BMAlertDialog;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class ClosenessMainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener
{

    public static final String TAG = "ClosenessMainActivity";
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.bluemaestro.utility.sdk.contentprovider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "nl.closeness";
    // The account name
    //    public static final String ACCOUNT = "admin@example.com";
    //    public static final String PASSWORD = "viu";
    public static final String ACCOUNT = "dummyaccount";
    public static final String PASSWORD = null;


    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 1L;
    public static final long SYNC_INTERVAL = 1L;
    //    public static final long SYNC_INTERVAL =
    //            SYNC_INTERVAL_IN_MINUTES *
    //                    SECONDS_PER_MINUTE;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_HISTORY_DEVICE = 3;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    // Instance fields
    Account mAccount;
    ContentResolver mResolver;
    private int mState = UART_PROFILE_DISCONNECTED;
    private String mPrivateHash = "";
    private BluetoothAdapter mBtAdapter = null;
    private TemperatureService temperatureService;
    /************************** UART STATUS CHANGE **************************/

    private ServiceConnection temperatureServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            temperatureService = ((TemperatureService.LocalBinder) service).getService();
            Log.d(TAG, "on temperature service connected: " + temperatureService);
            temperatureService.connected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            temperatureService = null;
        }
    };


    private MainBinding activityMainBinding;
    private final BroadcastReceiver temperatureBroadcastReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            switch(action)
            {
                case TemperatureService.ACTION_LOG:
                    logMessage(intent.getExtras().getString(TemperatureService.MESSAGE_KEY));
                    break;
                case TemperatureService.ACTION_DEVICE_READY:
                    activityMainBinding.deviceName.setText(intent.getExtras().getString("value"));
                    break;
                case TemperatureService.ACTION_NOTIFY_SYNC_ADAPTER:
//                    ContentResolver.
//                    Bundle bundle = new Bundle();
//                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
//                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
//                    ContentResolver.requestSync(mAccount, "com.bluemaestro.utility.sdk.contentprovider", bundle);
                    break;
                default:
                    Log.d(TAG, "temperature service, case not handled: " + intent.getAction());
                    //                case BluetoothService.ACTION_GATT_CONNECTED:
                    //                    onGattConnected(intent.getExtras().getString("device_address"));
                    //                    break;
                    //                case BluetoothService.ACTION_GATT_DISCONNECTED:
                    //                    onGattDisconnected(intent.getExtras().getString("device_address"));
                    //                    break;
                    //                case BluetoothService.ACTION_GATT_SERVICES_DISCOVERED:
                    //                    onGattServicesDiscovered();
                    //                    break;
                    //                case BluetoothService.ACTION_DATA_AVAILABLE:
                    //                    onDataAvailable(intent.getByteArrayExtra(BluetoothService.EXTRA_DATA));
                    //                    break;
                    //                case BluetoothService.DEVICE_DOES_NOT_SUPPORT_BLUETOOTH:
                    //                    onDeviceDoesNotSupportBluetooth();
                    //                    break;
                    //                default:
                    //                    Log.d(TAG, "broadcast received not handled");
            }
        }
    };

    private static IntentFilter temperatureServiceIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TemperatureService.ACTION_LOG);
        intentFilter.addAction(TemperatureService.ACTION_DEVICE_READY);
        intentFilter.addAction(TemperatureService.ACTION_NOTIFY_SYNC_ADAPTER);
        return intentFilter;
    }

    public static Account CreateSyncAccount(Context context)
    {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if(!accountManager.addAccountExplicitly(newAccount, PASSWORD, null))
        {
            //        if (!accountManager.addAccountExplicitly(newAccount, null, null)) {
            Log.d(TAG, "Account issue");
        }
        return (newAccount);
    }

    private String getPartnerSensorName()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ClosenessMainActivity.this.getApplicationContext());
        return sharedPref.getString("partner_sensor_name", null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
//        String temp = null;
//        temp.length();
        this.activityMainBinding = DataBindingUtil.setContentView(this, R.layout.main);
        //        setContentView(R.layout.main);

        String serverUrl = PreferenceManager.getDefaultSharedPreferences(this).getString("server_url_main", "");
        Log.i(TAG, "Server URL: " + serverUrl);

//        ApiUtils.create(this);


        this.mResolver = getContentResolver();
        this.mAccount = CreateSyncAccount(this);
        //        ContentResolver.addPeriodicSync(this.mAccount, "com.bluemaestro.utility.sdk.contentprovider", null, 10);
        ContentResolver.setSyncAutomatically(this.mAccount, "com.bluemaestro.utility.sdk.contentprovider", true);
//        mResolver.
        PreferenceManager.setDefaultValues(this, R.xml.closeness_preferences, false);

        // Delete all databases; testing only
//        String[] addresses = getApplicationContext().databaseList();
//        for(String address : addresses)
//        {
//            getApplicationContext().deleteDatabase(address);
//        }

        View rootView = findViewById(android.R.id.content).getRootView();
        StyleOverride.setDefaultTextColor(rootView, Color.BLACK);
        StyleOverride.setDefaultFont(rootView, this, "Montserrat-Regular.ttf");

        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter == null)
        {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        this.activityMainBinding.messageContainer.setLayoutManager(new LinearLayoutManager(this));
        this.activityMainBinding.messageContainer
                .setAdapter(new MessageAdapter(this, new ArrayList<String>()));
        this.activityMainBinding.messageContainer.setHasFixedSize(true);
        this.activityMainBinding.messageContainer.getAdapter().notifyDataSetChanged();

        // Initialise Bluetooth service
        service_init();

        if(TemperatureService.isRunning)
        {
            this.activityMainBinding.buttonConnectDisconnect.setText(R.string.closeness_button_disconnect);
            if(TemperatureService.connectedDevice != null)
            {
                this.activityMainBinding.deviceName.setText(TemperatureService.connectedDevice + " - ready");
            }
            //            this.getSystemService(TemperatureService.class);
        }

        // Handle Disconnect & Connect button
        this.activityMainBinding.buttonConnectDisconnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickConnectDisconnect();
            }
        });


        // create dummy data
//        for(int i = 0; i < 30; i++)
//        {
//            try
//            {
//                String timestamp = Utils.dateToIsoString(new Date());
//                ContentValues values = new ContentValues();
//                values.put(TemperatureTable.COLUMN_TEMP, i);
//                values.put(TemperatureTable.COLUMN_TIMESTAMP, timestamp);
//                values.put(TemperatureTable.COLUMN_PARTNER, false);
//                getContentResolver().insert(ClosenessProvider.CONTENT_URI, values);
//                //            Uri uri = getContentResolver().insert(ClosenessProvider.CONTENT_URI, values);
//            } catch(Exception e)
//            {
//                Log.e(TAG, e.toString());
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(temperatureBroadcastReceiver);
        } catch(Exception ignore)
        {
            Log.e(TAG, ignore.toString());
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume");
        if(!mBtAdapter.isEnabled())
        {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if(resultCode == Activity.RESULT_OK)
                {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                } else
                {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    /************************** INITIALISE **************************/

    private void service_init()
    {
        Intent bindIntent = new Intent(this, TemperatureService.class);

        LocalBroadcastManager.getInstance(this).registerReceiver(this.temperatureBroadcastReceiver, temperatureServiceIntentFilter());
    }


    private void logMessage(String message)
    {
        ((MessageAdapter) this.activityMainBinding.messageContainer.getAdapter()).addMessage(message);
        this.activityMainBinding.messageContainer
                .scrollToPosition(this.activityMainBinding.messageContainer.getAdapter().getItemCount() - 1);
        Log.d(TAG, "logMessage: " + message);
    }


    /************************** BUTTON CLICK HANDLERS **************************/

    private void onClickConnectDisconnect()
    {
        if(!mBtAdapter.isEnabled())
        {
            Log.i(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else
        {
            if(activityMainBinding.buttonConnectDisconnect.getText().equals(getString(R.string.closeness_button_connect)))
            {
                // user clicked on connect
                //                this.manualConnect = true;
                logMessage(getString(R.string.closeness_user_connect));
                activityMainBinding.buttonConnectDisconnect.setText(R.string.closeness_button_disconnect);
                connectDevices();
            } else
            {
                // Disconnect button pressed
                activityMainBinding.buttonConnectDisconnect.setText(R.string.closeness_button_connect);

                Intent intent = new Intent(this, TemperatureService.class);
                stopService(intent);
                //                this.temperatureService.onDisconnect();
                logMessage(getString(R.string.closeness_user_disconnect));
            }
        }
    }

    private void connectDevices()
    {
        Log.d(TAG, "start connection to device and partner");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        String deviceAddress = sharedPref.getString("sensor_name", "");
//        this.mPrivateHash = sharedPref.getString(getString(R.string.private_hash), "");
        try
        {
            Intent bindIntent = new Intent(this, TemperatureService.class);
            //        bindService(bindIntent, this.temperatureServiceConnection, Context.BIND_AUTO_CREATE);

            bindIntent.putExtra("sensor_address", PreferenceManager.getDefaultSharedPreferences(this).getString("sensor_name", ""));
            bindIntent.putExtra("partner_sensor_address", this.getPartnerSensorName());

            startService(bindIntent);
        } catch(IllegalArgumentException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Error connecting", Toast.LENGTH_SHORT).show();
        }
    }


    private void showMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed()
    {

        finish();
        if(false)
        {
            if(mState == UART_PROFILE_CONNECTED)
            {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
                showMessage("Blue Maestro Utility App is running in background. Disconnect to exit");
            } else
            {
                BMAlertDialog dialog = new BMAlertDialog(this,
                        "",
                        "Do you want to quit this Application?");
                dialog.setPositiveButton("YES", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        finish();
                    }
                });
                dialog.setNegativeButton("NO", null);

                dialog.show();
                dialog.applyFont(this, "Montserrat-Regular.ttf");
            }
        }
        //    }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {

    }
}
