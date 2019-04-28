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
import android.os.Handler;
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
    private boolean manualConnect = false;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_HISTORY_DEVICE = 3;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private int mState = UART_PROFILE_DISCONNECTED;
    private BluetoothDevice mDevice = null;
    public static String connectedDevice;
    public static final String ACTION_DEVICE_READY = "device_ready";
    public static final String ACTION_NOTIFY_SYNC_ADAPTER = "sync_adapter";
    private Handler handler = new Handler();
    public static final long RETRY_CONNECTING_TIME = 60 * 1000;



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

    private Runnable autoReconnectRunnable = new Runnable()
    {
        @Override
        public void run()
        {
//            broadcastMessage(getString(R.string.time_auto_reconnect));
            connect();
            //            logMessage(getString(R.string.time_auto_reconnect));
            //            ClosenessMainActivity.this.connectDevices();
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
        if(deviceAddress.equals(deviceAddress))
        {
            Log.d(TAG, "UART_CONNECT_MSG");
            //                    activityMainBinding.buttonConnectDisconnect.setText("Disconnect");
            //            activityMainBinding.deviceName.setText(mDevice.getName() + " - ready");
            EmbrWaveService.connectedDevice = this.mDevice.getName();
            this.broadcastUpdate(EmbrWaveService.ACTION_DEVICE_READY, "value", this.mDevice.getName() + " - ready");
//            this.broadcastMessage("Connected to: " + this.mDevice.getName());
            //            logMessage("Connected to: " + mDevice.getName());
            this.mState = UART_PROFILE_CONNECTED;
        }
    }


    private void onGattDisconnected(final String deviceAddress)
    {
        if(deviceAddress.equals(this.deviceAddress))
        {
            Log.d(TAG, "UART_DISCONNECT_MSG");
            //                    activityMainBinding.buttonConnectDisconnect.setText("Connect");
            this.broadcastUpdate(EmbrWaveService.ACTION_DEVICE_READY, "value", "Not Connected");
            //            activityMainBinding.deviceName.setText("Not Connected");
            if(this.mState != UART_PROFILE_DISCONNECTED)
            {
//                this.broadcastMessage("Disconnected from: " + mDevice.getAddress());
                Toast.makeText(this, "disconnected from: " + mDevice.getAddress(), Toast.LENGTH_LONG).show();
                EmbrWaveService.connectedDevice = null;
                //                logMessage("Disconnected from: " + mDevice.getAddress());
                this.mState = UART_PROFILE_DISCONNECTED;
            } else
            {
//                this.broadcastMessage("Couldn't connect to device");
                Toast.makeText(this, "Couldn't connect to device", Toast.LENGTH_LONG).show();
            }
            //                messageListView.setSelection(listAdapter.getCount() - 1);
            mService.close();
        }
        if(manualConnect)
        {
            // a device has been disconnected, we try to reconnect in RETRY_CONNECTING_TIME
            handler.postDelayed(this.autoReconnectRunnable, RETRY_CONNECTING_TIME);
        }
    }

    private void onGattServicesDiscovered()
    {
        this.mService.enableNotificationTemperature();
    }

    private void onDataAvailable(final byte[] value)
    {
//        try
//        {
////            if(nbTemperatures == 10) {
////
////                int tempOut = ((value[0] & 0xFF) + ((value[1] & 0xFF) << 8));
////                if(tempOut > 0x8000)
////                {
////                    // 2's complement
////                    tempOut = -(0x10000 - tempOut);
////                }
////                double temperature = 42.5 + ((double) tempOut / 480);
////                // myTemp
////                //            int mantissa = (value[1] & 0xFF) + ((value[2] & 0xFF) << 8) + ((value[3] & 0xFF) << 16);
////                //            int exponent = (value[4] & 0xFF) > 128 ? (value[4] & 0xFF) - 256 : (value[4] & 0xFF);
////                //            double temperature = mantissa * Math.pow(10, exponent);
////
////                //                    sendDataToServer(temperature);
////                saveDataToDB(temperature, this.partnerSensorConnected);
////
////                Log.d(TAG, "new temperature: " + String.format("%.2f", temperature) + "°C");
////                broadcastMessage("Temperature: " + String.format("%.2f", temperature) + "°C");
////
////                nbTemperatures = 0;
////            }
////            nbTemperatures++;
//
//            //            if(nbTemperatures > 20)
//            //            {
//            //                //                ContentResolver.
//            //                broadcastUpdate(ACTION_NOTIFY_SYNC_ADAPTER);
//            //            }
//        } catch(Exception e)
//        {
//            Log.e(TAG, e.toString());
//        }
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
        this.manualConnect = true;

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
        deviceAddress = intent.getExtras().getString("bracelet_address");
        //        this.connect();

        Intent bindIntent = new Intent(this, BluetoothService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(bluetoothBroadcastReceiver, makeGattUpdateIntentFilter());
        EmbrWaveService.isRunning = true;
        return Service.START_STICKY;
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
}
