package com.bluemaestro.utility.sdk.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private final IBinder mBinder = new LocalBinder();
    private int mState = UART_PROFILE_DISCONNECTED;
    private BluetoothService mService = null;
    private BluetoothDevice mDevice = null;
    private boolean manualConnect = false;
    private Handler handler = new Handler();
    private String deviceAddress;

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
            connect();
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
        Log.d(TAG, "UART_CONNECT_MSG");
        TemperatureService.connectedDevice = mDevice.getName();
        broadcastUpdate(TemperatureService.ACTION_DEVICE_READY, "value", mDevice.getName() + " - ready");
        broadcastMessage("Connected to: " + mDevice.getName());
        mState = UART_PROFILE_CONNECTED;
    }


    private void onGattDisconnected(final String deviceAddress)
    {
        Log.d(TAG, "UART_DISCONNECT_MSG");
        broadcastUpdate(TemperatureService.ACTION_DEVICE_READY, "value", "Not Connected");
        if(mState != UART_PROFILE_DISCONNECTED)
        {
            broadcastMessage("Disconnected from: " + mDevice.getAddress());
            TemperatureService.connectedDevice = null;
            mState = UART_PROFILE_DISCONNECTED;
        } else
        {
            broadcastMessage("Couldn't connect to device");
        }
        mService.close();
        if(manualConnect)
        {
            // a device has been disconnected, we try to reconnect in RETRY_CONNECTING_TIME
            handler.postDelayed(autoReconnectRunnable, RETRY_CONNECTING_TIME);
        }
    }

    private void onGattServicesDiscovered()
    {
        mService.enableNotificationTemperature();
    }

    private void onDataAvailable(final byte[] value)
    {
        try
        {
            int tempOut = ((value[0] & 0xFF) + ((value[1] & 0xFF) << 8));
            if(tempOut > 0x8000)
            {
                // 2's complement
                tempOut = -(0x10000 - tempOut);
            }
            double temperature = 42.5 + ((double) tempOut / 480);
            saveDataToDB(temperature);

            Log.d(TAG, "new temperature: " + String.format("%.2f", temperature) + "°C");
            broadcastMessage("Temperature: " + String.format("%.2f", temperature) + "°C");
        } catch(Exception e)
        {
            Log.e(TAG, e.toString());
        }
    }


    private void saveDataToDB(double temperature)
    {
        try
        {
            String timestamp = Utils.dateToIsoString(new Date());
            ContentValues values = new ContentValues();
            values.put(TemperatureTable.COLUMN_TEMP, temperature);
            values.put(TemperatureTable.COLUMN_TIMESTAMP, timestamp);
            values.put(TemperatureTable.COLUMN_LATITUDE, Utils.latitude);
            values.put(TemperatureTable.COLUMN_LONGITUDE, Utils.longitude);
            getContentResolver().insert(ClosenessProvider.CONTENT_URI, values);
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
        deviceAddress = sensorAddress;
        connect();

    }

    private void connect()
    {
        Log.d(TAG, "onConnect");
        manualConnect = true;

        //        broadcastMessage("Hello there");
        if(mState == UART_PROFILE_DISCONNECTED)
        {
            Log.d(TAG, "try connecting to device");
            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
            //                    mBMDevice = BMDeviceMap.INSTANCE.getBMDevice(mDevice.getAddress());
            Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
            mService.connect(deviceAddress);
        } else
        {
            Log.d(TAG, "device already connected");
        }
    }

    public void onDisconnect()
    {
        manualConnect = false;
        deviceAddress = null;
        handler.removeCallbacks(autoReconnectRunnable);
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
        deviceAddress = intent.getExtras().getString("sensor_address");
        //        connect();

        Intent bindIntent = new Intent(this, BluetoothService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this)
          .registerReceiver(bluetoothBroadcastReceiver, makeGattUpdateIntentFilter());
        TemperatureService.isRunning = true;
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
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

        LocalBroadcastManager.getInstance(this)
          .registerReceiver(bluetoothBroadcastReceiver, makeGattUpdateIntentFilter());
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

        manualConnect = false;
        deviceAddress = null;
        handler.removeCallbacks(autoReconnectRunnable);
        if(mDevice != null)
        {
            mService.disconnect();
            broadcastUpdate(TemperatureService.ACTION_DEVICE_READY, "value", "Not connected");
        }
        unbindService(mServiceConnection);
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