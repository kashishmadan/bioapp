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
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bluemaestro.utility.sdk.adapter.MessageAdapter;
import com.bluemaestro.utility.sdk.databinding.MainBinding;
import com.bluemaestro.utility.sdk.utility.Utils;
import com.bluemaestro.utility.sdk.views.dialogs.BMAlertDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class ClosenessMainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener
{

    public static final String TAG = "BlueMaestro";
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.bluemaestro.utility.sdk.contentprovider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "nl.closeness";
    // The account name
    //    public static final String ACCOUNT = "admin@example.com";
    //    public static final String PASSWORD = "viu";
    public static final String ACCOUNT = "dummyaccount";
    public static final String PASSWORD = null;

    public static final long RETRY_CONNECTING_TIME = 60 * 1000;

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
    private BluetoothService mService = null;
    private BluetoothDevice mDevice = null;
    private String mPrivateHash = "";
    private BluetoothAdapter mBtAdapter = null;
    private boolean partnerSensorConnected = false;
    private boolean manualConnect = false;
    private Handler handler = new Handler();
    /************************** UART STATUS CHANGE **************************/

    // UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder rawBinder)
        {
            mService = ((BluetoothService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if(!mService.initialize())
            {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname)
        {
            Log.d(TAG, "service disconnect: " + classname);
            mService = null;
        }
    };
    private MainBinding activityMainBinding;
    private Runnable autoReconnectRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            logMessage(getString(R.string.time_auto_reconnect));
            ClosenessMainActivity.this.connectDevices();
        }
    };
    // Main UART broadcast receiver
    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            switch(action)
            {
                case BluetoothService.ACTION_GATT_CONNECTED:
                    onGattConnected(intent.getExtras().getString("device_address"));
                    break;
                case BluetoothService.ACTION_GATT_DISCONNECTED:
                    onGattDisconnected(intent.getExtras().getString("device_address"));
                    break;
                case BluetoothService.ACTION_GATT_SERVICES_DISCOVERED:
                    onGattServicesDiscovered();
                    break;
                case BluetoothService.ACTION_DATA_AVAILABLE:
                    onDataAvailable(intent.getByteArrayExtra(BluetoothService.EXTRA_DATA));
                    break;
                case BluetoothService.DEVICE_DOES_NOT_SUPPORT_BLUETOOTH:
                    onDeviceDoesNotSupportBluetooth();
                    break;
                default:
                    Log.d(TAG, "broadcast received not handled");
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothService.DEVICE_DOES_NOT_SUPPORT_BLUETOOTH);
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
        this.activityMainBinding = DataBindingUtil.setContentView(this, R.layout.main);
        //        setContentView(R.layout.main);

        String serverUrl = PreferenceManager.getDefaultSharedPreferences(this).getString("server_url_main", "");
        Log.i(TAG, "Server URL: " + serverUrl);

        this.mResolver = getContentResolver();
        this.mAccount = CreateSyncAccount(this);
        ContentResolver.setSyncAutomatically(this.mAccount, "com.bluemaestro.utility.sdk.contentprovider", true);
        PreferenceManager.setDefaultValues(this, R.xml.closeness_preferences, false);

        // Delete all databases; testing only
        String[] addresses = getApplicationContext().databaseList();
        for(String address : addresses)
        {
            getApplicationContext().deleteDatabase(address);
        }

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

        // Handle Disconnect & Connect button
        this.activityMainBinding.buttonConnectDisconnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickConnectDisconnect();
            }
        });

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(bluetoothBroadcastReceiver);
        } catch(Exception ignore)
        {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        if(mService != null)
        {
            mService.stopSelf();
            mService = null;
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
            case REQUEST_SELECT_DEVICE:
                // When the DeviceListActivity return, with the selected device address
                if(resultCode == Activity.RESULT_OK && data != null)
                {
                    //                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceAddress = getPartnerSensorName();
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    this.activityMainBinding.deviceName.setText(mDevice.getName() + " - connecting");
                    mService.connect(deviceAddress, true);
                }
                break;
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
        Intent bindIntent = new Intent(this, BluetoothService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(bluetoothBroadcastReceiver, makeGattUpdateIntentFilter());
    }

    private void onGattConnected(final String deviceAddress)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                String partnerDeviceAddress = getPartnerSensorName();
                if(deviceAddress.equals(partnerDeviceAddress))
                {
                    logMessage("partner sensor is in range !!");
                    partnerSensorConnected = true;
                } else
                {
                    Log.d(TAG, "UART_CONNECT_MSG");
                    //                    activityMainBinding.buttonConnectDisconnect.setText("Disconnect");
                    activityMainBinding.deviceName.setText(mDevice.getName() + " - ready");
                    logMessage("Connected to: " + mDevice.getName());
                    // TODO scroll ?
                    //                messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    mState = UART_PROFILE_CONNECTED;
                }
            }
        });
    }

    private void logMessage(String message)
    {
        ((MessageAdapter) this.activityMainBinding.messageContainer.getAdapter()).addMessage(message);
        this.activityMainBinding.messageContainer
                .scrollToPosition(this.activityMainBinding.messageContainer.getAdapter().getItemCount() - 1);
        Log.d(TAG, "logMessage: " + message);
    }

    private void onGattDisconnected(final String deviceAddress)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                String partnerDeviceAddress = getPartnerSensorName();
                if(deviceAddress.equals(partnerDeviceAddress))
                {
                    logMessage(getString(R.string.partner_sensor_left_range));
                    if(partnerSensorConnected)
                    {
                        partnerSensorConnected = false;
                    } else
                    {
                        logMessage("Couldn't connect to partner's device");
                    }
                } else
                {
                    Log.d(TAG, "UART_DISCONNECT_MSG");
                    //                    activityMainBinding.buttonConnectDisconnect.setText("Connect");
                    activityMainBinding.deviceName.setText("Not Connected");
                    //                logMessage("Disconnected from: " + mDevice.getName());
                    if(mState != UART_PROFILE_DISCONNECTED)
                    {
                        logMessage("Disconnected from: " + mDevice.getAddress());
                        mState = UART_PROFILE_DISCONNECTED;
                    } else
                    {
                        logMessage("Couldn't connect to device");
                    }
                    //                messageListView.setSelection(listAdapter.getCount() - 1);
                    mService.close();
                }
                if(manualConnect)
                {
                    // a device has been disconnected, we try to reconnect in RETRY_CONNECTING_TIME
                    handler.postDelayed(autoReconnectRunnable, RETRY_CONNECTING_TIME);
                }
            }
        });
    }

    private void onGattServicesDiscovered()
    {
        mService.enableNotification();
    }

    private void onDataAvailable(final byte[] value)
    {
        runOnUiThread(new Runnable()
        {

            public void run()
            {
                try
                {
                    int mantissa = (value[1] & 0xFF) + ((value[2] & 0xFF) << 8) + ((value[3] & 0xFF) << 16);
                    int exponent = (value[4] & 0xFF) > 128 ? (value[4] & 0xFF) - 256 : (value[4] & 0xFF);
                    double temperature = mantissa * Math.pow(10, exponent);

                    //                    sendDataToServer(temperature);
                    saveDataToDB(temperature, partnerSensorConnected);

                    logMessage("Temperature: " + String.format("%.2f", temperature) + "°C");

                    //                    if(mBMDevice == null)
                    //                    {
                    //                        return;
                    //                    }
                    // TODO updateChart ??
                    //                    mBMDevice.updateChart(lineChart, text);
                    //mBMDatabase.addData(BMDatabase.TIMESTAMP_NOW(), text);

                } catch(Exception e)
                {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    private void saveDataToDB(double temperature, boolean isPartnerClose)
    {
        try
        {
            String timestamp = Utils.dateToIsoString(new Date());
            ContentValues values = new ContentValues();
            values.put(TemperatureTable.COLUMN_TEMP, temperature);
            values.put(TemperatureTable.COLUMN_TIMESTAMP, timestamp);
            values.put(TemperatureTable.COLUMN_PARTNER, isPartnerClose);
            getContentResolver().insert(ClosenessProvider.CONTENT_URI, values);
            //            Uri uri = getContentResolver().insert(ClosenessProvider.CONTENT_URI, values);
        } catch(Exception e)
        {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    private void onDeviceDoesNotSupportBluetooth()
    {
        showMessage("Device doesn't support UART. Disconnecting");
        mService.disconnect();
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
            if(this.activityMainBinding.buttonConnectDisconnect.getText().equals(getString(R.string.closeness_button_connect)))
            {
                // user clicked on connect
                this.manualConnect = true;
                this.logMessage(getString(R.string.closeness_user_connect));

                activityMainBinding.buttonConnectDisconnect.setText(R.string.closeness_button_disconnect);
                //                 Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                //                                Intent newIntent = new Intent(ClosenessMainActivity.this, DeviceListActivity.class);
                //                                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                //                edtMessage.setText("");
                //                edtMessage.setVisibility(View.VISIBLE);
                //                messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                this.connectDevices();
            } else
            {
                // Disconnect button pressed
                this.activityMainBinding.buttonConnectDisconnect.setText(R.string.closeness_button_connect);
                this.manualConnect = false;
                if(mDevice != null)
                {
                    mService.disconnect();
                }

                handler.removeCallbacks(autoReconnectRunnable);
                this.logMessage(getString(R.string.closeness_user_disconnect));
                //                messageListView.setVisibility(View.VISIBLE);
                //                messageListView.setSelection(listAdapter.getCount() - 1);
            }
        }
    }

    private void connectDevices()
    {
        Log.d(TAG, "start connection to device and partner");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String deviceAddress = sharedPref.getString("sensor_name", "");
        this.mPrivateHash = sharedPref.getString(getString(R.string.private_hash), "");
        if(mPrivateHash.equals(""))
        {
            registerDevice();
        } else
        {
            Log.d(TAG, "using private hash");
            Log.d(TAG, mPrivateHash);
        }
        try
        {
            if(this.mState == UART_PROFILE_DISCONNECTED)
            {
                Log.d(TAG, "try connecting to device");
                this.mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                //                    mBMDevice = BMDeviceMap.INSTANCE.getBMDevice(mDevice.getAddress());
                Log.d(TAG, "... onActivityResultdevice.address==" + this.mDevice + "mserviceValue" + this.mService);
                this.mService.connect(deviceAddress, true);
            } else
            {
                Log.d(TAG, "device already connected");
            }


            // try connecting to partner's sensor
            if(!this.partnerSensorConnected)
            {
                Log.d(TAG, "try connecting to partner");
                String partnerDeviceAddress = getPartnerSensorName();
                if(partnerDeviceAddress == null)
                {
                    logMessage("no partner sensor entered");
                } else
                {
                    try
                    {
                        mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(partnerDeviceAddress);
                        mService.connect(partnerDeviceAddress, false);
                    } catch(IllegalArgumentException e)
                    {
                        e.printStackTrace();
                        logMessage("partner address incorrect");
                        //                    Toast.makeText(ClosenessMainActivity.this, "Error connecting to the partner", Toast
                        // .LENGTH_SHORT).show();
                    }
                }
            } else
            {
                Log.d(TAG, "partner already connected");
            }

            //                    final Handler handler = new Handler();
            //                    handler.postDelayed(new Runnable() {
            //                        @Override
            //                        public void run() {
            //                            startLogging();
            //                        }
            //                    }, 1000);
        } catch(IllegalArgumentException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Error connecting", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerDevice()
    {
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;
        String uniqueID = UUID.randomUUID().toString();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String url;
        url = sharedPref.getString("server_url_main", "http://192.168.1.50:5000");
        //FIXME API structure hard coded...
        url = url + "/register/" + uniqueID;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(TAG, response);
                        response = response.replaceAll("\\s$", "");
                        response = response.replaceAll("\"", "");
                        Log.d(TAG, response);
                        Log.d(TAG, Integer.toString(response.length()));
                        if(response.length() == 28)
                        {
                            Toast toast = Toast.makeText(context, R.string.registration_impossible, duration);
                            toast.show();
                            Log.d(TAG, "Registration is not possible");
                        } else
                        {
                            if(response.length() == 64)
                            {
                                mPrivateHash = response;
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.private_hash), response);
                                editor.commit();

                                Log.d(TAG, response);
                            } else
                            {
                                Toast toast = Toast.makeText(context, R.string.server_error_msg, duration);
                                toast.show();
                                Log.d(TAG, "Server error occured");
                            }
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d(TAG, "That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void showMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed()
    {
        //        Intent startMain = new Intent(Intent.ACTION_MAIN);
        //        startMain.addCategory(Intent.CATEGORY_HOME);
        //        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //        startActivity(startMain);
        //        showMessage("Blue Maestro Utility App is running in background. Disconnect to exit");

        //        Intent startMain = new Intent(Intent.ACTION_MAIN, CoRegulationMainActivity.class);
        //        startActivity(startMain);

        //        if(false)
        //        {

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
        //    }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {

    }
}
