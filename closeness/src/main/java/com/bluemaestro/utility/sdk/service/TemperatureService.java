package com.bluemaestro.utility.sdk.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bluemaestro.utility.sdk.ClosenessProvider;
import com.bluemaestro.utility.sdk.R;
import com.bluemaestro.utility.sdk.database.TemperatureTable;
import com.bluemaestro.utility.sdk.utility.Utils;

import java.util.Date;

public class TemperatureService extends Service
{
    public static final String TAG = "TemperatureService";
    public static final String ACTION_LOG = "log";
    public static final String ACTION_DEVICE_READY = "device_ready";
    public static final String ACTION_NOTIFY_SYNC_ADAPTER = "sync_adapter";
    public static final String MESSAGE_KEY = "message";
    public static final long RETRY_CONNECTING_TIME = 60 * 1000;
    public static boolean isRunning = false;
    public static String connectedDevice;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_HISTORY_DEVICE = 3;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private final IBinder mBinder = new LocalBinder();
    private int mState = UART_PROFILE_DISCONNECTED;
    private ContentResolver mResolver;
    private BluetoothService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private boolean partnerSensorConnected = false;
    private boolean manualConnect = false;
    private Handler handler = new Handler();
    private String deviceAddress;
    private String partnerDeviceAddress;
    private int nbTemperatures = 0;
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
                //                finish();
            }
            Log.d(TAG, "bluetooth service connected, we try to connect to sensor");
            connect();
        }

        public void onServiceDisconnected(ComponentName classname)
        {
            Log.d(TAG, "service disconnect: " + classname);
            mService = null;
        }
    };
    private Runnable autoReconnectRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            broadcastMessage(getString(R.string.time_auto_reconnect));
            TemperatureService.this.connect();
            //            logMessage(getString(R.string.time_auto_reconnect));
            //            ClosenessMainActivity.this.connectDevices();
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

    private void onGattConnected(final String deviceAddress)
    {
        if(deviceAddress.equals(this.partnerDeviceAddress))
        {
            this.broadcastMessage("partner sensor is in range !!");
            this.partnerSensorConnected = true;
        } else
        {
            Log.d(TAG, "UART_CONNECT_MSG");
            //                    activityMainBinding.buttonConnectDisconnect.setText("Disconnect");
            //            activityMainBinding.deviceName.setText(mDevice.getName() + " - ready");
            TemperatureService.connectedDevice = this.mDevice.getName();
            this.broadcastUpdate(TemperatureService.ACTION_DEVICE_READY, "value", this.mDevice.getName() + " - ready");
            this.broadcastMessage("Connected to: " + this.mDevice.getName());
            //            logMessage("Connected to: " + mDevice.getName());
            this.mState = UART_PROFILE_CONNECTED;
        }
    }


    private void onGattDisconnected(final String deviceAddress)
    {
        if(deviceAddress.equals(this.partnerDeviceAddress))
        {
            //            logMessage(getString(R.string.partner_sensor_left_range));
            if(this.partnerSensorConnected)
            {
                this.partnerSensorConnected = false;
                this.broadcastMessage(getString(R.string.partner_sensor_left_range));
            } else
            {
                this.broadcastMessage("Couldn't connect to partner's device");
            }
        } else
        {
            Log.d(TAG, "UART_DISCONNECT_MSG");
            //                    activityMainBinding.buttonConnectDisconnect.setText("Connect");
            this.broadcastUpdate(TemperatureService.ACTION_DEVICE_READY, "value", "Not Connected");
            //            activityMainBinding.deviceName.setText("Not Connected");
            if(this.mState != UART_PROFILE_DISCONNECTED)
            {
                this.broadcastMessage("Disconnected from: " + mDevice.getAddress());
                TemperatureService.connectedDevice = null;
                //                logMessage("Disconnected from: " + mDevice.getAddress());
                this.mState = UART_PROFILE_DISCONNECTED;
            } else
            {
                this.broadcastMessage("Couldn't connect to device");
            }
            //                messageListView.setSelection(listAdapter.getCount() - 1);
            this.mService.close();
        }
        if(this.manualConnect)
        {
            // a device has been disconnected, we try to reconnect in RETRY_CONNECTING_TIME
            this.handler.postDelayed(this.autoReconnectRunnable, RETRY_CONNECTING_TIME);
        }
    }

    private void onGattServicesDiscovered()
    {
        this.mService.enableNotificationTemperature();
    }

    private void onDataAvailable(final byte[] value)
    {
        try
        {
            if(nbTemperatures == 10) {

                int tempOut = ((value[0] & 0xFF) + ((value[1] & 0xFF) << 8));
                if(tempOut > 0x8000)
                {
                    // 2's complement
                    tempOut = -(0x10000 - tempOut);
                }
                double temperature = 42.5 + ((double) tempOut / 480);
                // myTemp
                //            int mantissa = (value[1] & 0xFF) + ((value[2] & 0xFF) << 8) + ((value[3] & 0xFF) << 16);
                //            int exponent = (value[4] & 0xFF) > 128 ? (value[4] & 0xFF) - 256 : (value[4] & 0xFF);
                //            double temperature = mantissa * Math.pow(10, exponent);

                //                    sendDataToServer(temperature);
                saveDataToDB(temperature, this.partnerSensorConnected);

                Log.d(TAG, "new temperature: " + String.format("%.2f", temperature) + "°C");
                broadcastMessage("Temperature: " + String.format("%.2f", temperature) + "°C");

                nbTemperatures = 0;
            }
            this.nbTemperatures++;

            //            if(nbTemperatures > 20)
//            {
//                //                ContentResolver.
//                broadcastUpdate(ACTION_NOTIFY_SYNC_ADAPTER);
//            }
        } catch(Exception e)
        {
            Log.e(TAG, e.toString());
        }
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
        Log.d(TAG, "Device doesn't support UART. Disconnecting");
        mService.disconnect();
    }

    public void onConnect(String sensorAddress, String partnerSensorAddress)
    {
        this.deviceAddress = sensorAddress;
        this.partnerDeviceAddress = partnerSensorAddress;
        connect();

    }

    private void connect()
    {
        Log.d(TAG, "onConnect");
        this.manualConnect = true;

        //        this.broadcastMessage("Hello there");
        if(this.mState == UART_PROFILE_DISCONNECTED)
        {
            Log.d(TAG, "try connecting to device");
            this.mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.deviceAddress);
            //                    mBMDevice = BMDeviceMap.INSTANCE.getBMDevice(mDevice.getAddress());
            Log.d(TAG, "... onActivityResultdevice.address==" + this.mDevice + "mserviceValue" + this.mService);
            this.mService.connect(this.deviceAddress, true);
        } else
        {
            Log.d(TAG, "device already connected");
        }


        // try connecting to partner's sensor
        if(!this.partnerSensorConnected)
        {
            Log.d(TAG, "try connecting to partner");
            //            String partnerDeviceAddress = getPartnerSensorName();
            if(this.partnerDeviceAddress == null)
            {
                //                logMessage("no partner sensor entered");
                this.broadcastMessage("no partner sensor entered");
            } else
            {
                try
                {
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.partnerDeviceAddress);
                    mService.connect(this.partnerDeviceAddress, false);
                } catch(IllegalArgumentException e)
                {
                    e.printStackTrace();
                    //                    logMessage("partner address incorrect");
                    this.broadcastMessage("partner address incorrect");
                    //                    Toast.makeText(ClosenessMainActivity.this, "Error connecting to the partner", Toast
                    // .LENGTH_SHORT).show();
                }
            }
        } else
        {
            Log.d(TAG, "partner already connected");
        }
    }

    public void onDisconnect()
    {
        this.manualConnect = false;
        this.deviceAddress = null;
        this.partnerDeviceAddress = null;
        this.handler.removeCallbacks(this.autoReconnectRunnable);
        if(mDevice != null)
        {
            mService.disconnect();
        }
    }

    private void broadcastMessage(final String message)
    {

        final Intent intent = new Intent(TemperatureService.ACTION_LOG);
        intent.putExtra(MESSAGE_KEY, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, String key, String value)
    {
        final Intent intent = new Intent(action);
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //TODO do something useful
        this.deviceAddress = intent.getExtras().getString("sensor_address");
        this.partnerDeviceAddress = intent.getExtras().getString("partner_sensor_address");
        //        this.connect();

        Intent bindIntent = new Intent(this, BluetoothService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(bluetoothBroadcastReceiver, makeGattUpdateIntentFilter());
        TemperatureService.isRunning = true;
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return this.mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        //        close();
        return super.onUnbind(intent);
    }

    public void connected()
    {
        Intent bindIntent = new Intent(this, BluetoothService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(bluetoothBroadcastReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(bluetoothBroadcastReceiver);
        } catch(Exception e)
        {
            e.printStackTrace();
        }

        this.manualConnect = false;
        this.deviceAddress = null;
        this.partnerDeviceAddress = null;
        this.handler.removeCallbacks(this.autoReconnectRunnable);
        if(this.mDevice != null)
        {
            this.mService.disconnect();
            this.broadcastUpdate(TemperatureService.ACTION_DEVICE_READY, "value", "Not connected");
        }
        unbindService(this.mServiceConnection);
        TemperatureService.isRunning = false;
    }

    public class LocalBinder extends Binder
    {
        public TemperatureService getService()
        {
            return TemperatureService.this;
        }
    }

}