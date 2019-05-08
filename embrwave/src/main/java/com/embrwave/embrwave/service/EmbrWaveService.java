package com.embrwave.embrwave.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.embrwave.embrwave.R;

public class EmbrWaveService extends Service
{
    private final IBinder mBinder = new LocalBinder();
    private String deviceAddress;
    private BluetoothService mService = null;
    private final String TAG = "EmbrWaveService";
    public static boolean isRunning = false;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_HISTORY_DEVICE = 3;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private int mState = UART_PROFILE_DISCONNECTED;
    private BluetoothDevice mDevice = null;
    public static String connectedDevice;

    public static final String SERVICE_ACTION_KEY = "action_key";
    public static final String SERVICE_VALUE_KEY = "value_key";
    public static final String SERVICE_ACTION_LED_BLINK = "led_blink";
    public static final String SERVICE_ACTION_START_COOL_WARM = "start_cool_warm";
    public static final String SERVICE_ACTION_STOP_COOL_WARM = "stop_cool_warm";




    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder rawBinder)
        {
            mService = ((BluetoothService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if(!mService.initialize())
            {
                Log.e(TAG, getString(R.string.error_bluetooth));
                Toast.makeText(EmbrWaveService.this, R.string.error_bluetooth, Toast.LENGTH_LONG).show();
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
        if(deviceAddress.equals(deviceAddress))
        {
            Log.d(TAG, "UART_CONNECT_MSG");
            EmbrWaveService.connectedDevice = mDevice.getName();
            mState = UART_PROFILE_CONNECTED;
        }
    }


    private void onGattDisconnected(final String deviceAddress)
    {
        if(deviceAddress.equals(this.deviceAddress))
        {
            Log.d(TAG, "UART_DISCONNECT_MSG");
            if(mState != UART_PROFILE_DISCONNECTED)
            {
//                this.broadcastMessage("Disconnected from: " + mDevice.getAddress());
                Toast.makeText(this, "disconnected from: " + mDevice.getAddress(), Toast.LENGTH_LONG).show();
                EmbrWaveService.connectedDevice = null;
                //                logMessage("Disconnected from: " + mDevice.getAddress());
                mState = UART_PROFILE_DISCONNECTED;
            } else
            {
//                this.broadcastMessage("Couldn't connect to device");
                Toast.makeText(this, "Couldn't connect to device", Toast.LENGTH_LONG).show();
            }
            //                messageListView.setSelection(listAdapter.getCount() - 1);
            mService.close();
        }
    }

    private void onGattServicesDiscovered()
    {
        this.mService.enableNotificationTemperature();
    }

    private void onDeviceDoesNotSupportBluetooth()
    {
        Log.d(TAG, "Device doesn't support UART. Disconnecting");
        mService.disconnect();
    }


    private void broadcastUpdate(final String action, String key, String value)
    {
        final Intent intent = new Intent(action);
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void connect()
    {
        Log.d(TAG, "onConnect");

        //        this.broadcastMessage("Hello there");
        if(this.mState == UART_PROFILE_DISCONNECTED)
        {
            Log.d(TAG, "try connecting to device");
            this.mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.deviceAddress);
            //                    mBMDevice = BMDeviceMap.INSTANCE.getBMDevice(mDevice.getAddress());
            Log.d(TAG, "... onActivityResultdevice.address==" + this.mDevice + "mserviceValue" + this.mService);
            this.mService.connect(this.deviceAddress);
        } else
        {
            Log.d(TAG, "device already connected");
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(EmbrWaveService.isRunning) {
            // value to activate
//            deviceAddress = intent.getExtras().getString(getString(R.string.embr_wave_address_key));
            String action = intent.getExtras().getString(EmbrWaveService.SERVICE_ACTION_KEY);
            switch(action) {
                case EmbrWaveService.SERVICE_ACTION_LED_BLINK:
                    this.mService.blinkLeds();
                    break;
                case EmbrWaveService.SERVICE_ACTION_START_COOL_WARM:
                    this.mService.startCoolingWarming(intent.getExtras().getInt(EmbrWaveService.SERVICE_VALUE_KEY));
                    break;
                case EmbrWaveService.SERVICE_ACTION_STOP_COOL_WARM:
                    this.mService.stopCoolingWarming();
                    break;
                default:
                    Log.d(TAG, "unhandled case");
            }
            return Service.START_STICKY;
        } else {

            deviceAddress = intent.getExtras().getString(getString(R.string.embr_wave_address_key));
            //        this.connect();

            Intent bindIntent = new Intent(this, BluetoothService.class);
            bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

            LocalBroadcastManager.getInstance(this).registerReceiver(bluetoothBroadcastReceiver, makeGattUpdateIntentFilter());
            EmbrWaveService.isRunning = true;
            return Service.START_STICKY;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return this.mBinder;
    }

    public class LocalBinder extends Binder
    {
        public EmbrWaveService getService()
        {
            return EmbrWaveService.this;
        }
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

        deviceAddress = null;
        if(this.mDevice != null)
        {
            this.mService.disconnect();
        }
        unbindService(this.mServiceConnection);
        EmbrWaveService.isRunning = false;
    }
}
