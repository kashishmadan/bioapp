
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

package com.embrwave.embrwave.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothService extends Service
{

    public static final String ACTION_GATT_CONNECTED =
            "com.bluemaestro.utility.sdk.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED =
            "com.bluemaestro.utility.sdk.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED =
            "com.bluemaestro.utility.sdk.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE =
            "com.bluemaestro.utility.sdk.ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA =
            "com.bluemaestro.utility.sdk.EXTRA_DATA";
    public static final String DEVICE_DOES_NOT_SUPPORT_BLUETOOTH =
            "com.bluemaestro.utility.sdk.DEVICE_DOES_NOT_SUPPORT_BLUETOOTH";
    private static final String TAG = BluetoothService.class.getSimpleName();
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    // insightSIP
    //    public final UUID TEMPERATURE_SERVICE_UUID = convertFromInteger(0x1889);
    //    public final UUID TEMPERATURE_MEASURMENT_UUID = convertFromInteger(0x2AAB);
    //    public final UUID CLIENT_CHARACTERISTIC_CONFIG = convertFromInteger(0x2902);
    //    public final UUID SESSION_START_TIME_SERVICE_UUID = convertFromInteger(0x1888);
    //    public final UUID SESSION_START_TIME_SESSION_UUID = convertFromInteger(0x2AAA);
    // Embr Wave
//    public final UUID EMBR_WAVE_SERVICE_UUID = convertFromInteger(0x8001);
    public final UUID EMBR_WAVE_SERVICE_UUID = UUID.fromString("00009004-1112-efde-1523-725a2aab0123");
//    public final UUID EMBR_WAVE_LED_BLINK_UUID = convertFromInteger(0x4003);
    public final UUID EMBR_WAVE_LED_BLINK_UUID = UUID.fromString("00004003-1112-efde-1523-725a2aab0123");
    public final UUID EMBR_WAVE_LED_START_STOP_UUID = UUID.fromString("00004009-1112-efde-1523-725a2aab0123");
    public final UUID EMBR_WAVE_START_COOL_WARM_UUID = UUID.fromString("00004005-1112-efde-1523-725a2aab0123");
    public final UUID EMBR_WAVE_STOP_COOL_WARM_UUID = UUID.fromString("00004008-1112-efde-1523-725a2aab0123");

    private final IBinder mBinder = new LocalBinder();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private boolean isSessionNotificationEnabled = false;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            String intentAction;

            if(newState == BluetoothProfile.STATE_CONNECTED)
            {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(gatt, intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection. if it's the main sensor
                if(gatt.getDevice().getAddress().equals(mBluetoothDeviceAddress))
                {
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());
                }

            } else if(newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(gatt, intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                Log.w(TAG, "mBluetoothGatt = " + mBluetoothGatt);

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else
            {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {

            //            BluetoothGattCharacteristic characteristic =
            //                    gatt.getService(TEMPERATURE_SERVICE_UUID)
            //                            .getCharacteristic(TEMPERATURE_MEASURMENT_UUID);


            //            BluetoothGattCharacteristic characteristic =
            //                    gatt.getService(SESSION_START_TIME_SERVICE_UUID)
            //                            .getCharacteristic(SESSION_START_TIME_SESSION_UUID);
            //
            //            gatt.writeCharacteristic(descriptor.getCharacteristic());

            // the temperature is finished, we launch the session
            if(!isSessionNotificationEnabled)
            {
                isSessionNotificationEnabled = true;
                enableNotificationSession();
            }

            //            characteristic.setValue(new byte[]{1, 1});
            //            gatt.writeCharacteristic(characteristic);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status)
        {
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
            //            if(characteristic.getUuid().equals(TEMPERATURE_MEASURMENT_UUID))
            //            {
            //                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            //            }
        }
    };

    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final BluetoothGatt gatt, final String action)
    {
        final Intent intent = new Intent(action);
        intent.putExtra("device_address", gatt.getDevice().getAddress());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic)
    {
        final Intent intent = new Intent(action);

        // This is handling for the notification on TX Character of NUS service
        //        if(TEMPERATURE_MEASURMENT_UUID.equals(characteristic.getUuid()))
        //        {
        //
        //            // Log.d(TAG, String.format("Received TX: %d",characteristic.getValue() ));
        //            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        //        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void blinkLeds() {
        BluetoothGattCharacteristic characteristic =
                mBluetoothGatt.getService(EMBR_WAVE_SERVICE_UUID)
                        .getCharacteristic(EMBR_WAVE_LED_BLINK_UUID);
        characteristic.setValue(new byte[]{1});
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void startCoolingWarming(int value)
    {
        BluetoothGattCharacteristic characteristic =
                mBluetoothGatt.getService(EMBR_WAVE_SERVICE_UUID)
                        .getCharacteristic(EMBR_WAVE_START_COOL_WARM_UUID);
//        if(value < 0) {
//            Log.d(TAG, "value: " + (byte) (0xFF + (value+1)));
//            characteristic.setValue(new byte[]{(byte) (0xFF + value)});
//        } else {
//            Log.d(TAG, "value: " + (byte) value);
//            characteristic.setValue(new byte[]{(byte) value});
//        }
        Log.d(TAG, "value: " + (byte) value);
        characteristic.setValue(new byte[]{(byte) value});
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void stopCoolingWarming() {
        BluetoothGattCharacteristic characteristic =
                mBluetoothGatt.getService(EMBR_WAVE_SERVICE_UUID)
                        .getCharacteristic(EMBR_WAVE_STOP_COOL_WARM_UUID);
        characteristic.setValue(new byte[]{0,0,0,0x20});
        mBluetoothGatt.writeCharacteristic(characteristic);
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
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize()
    {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if(mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBluetoothManager == null)
            {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null)
        {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address)
    {
        if(mBluetoothAdapter == null || address == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if(address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null)
        {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if(mBluetoothGatt.connect())
            {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else
            {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if(device == null)
        {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect()
    {
        if(mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        // just disconnects the device but gatt stays available and you can reconnect with connect()
        mBluetoothGatt.disconnect();
        // completely closes the gatt
        mBluetoothGatt.close();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close()
    {
        if(mBluetoothGatt == null)
        {
            return;
        }
        Log.w(TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth
     * .BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if(mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enable Notification on TX characteristic
     *
     * @return
     */
    public void enableNotificationTemperature()
    {
        //        if(this.mBluetoothGatt == null)
        //        {
        //            Log.e(TAG, "mBluetoothGatt null");
        //            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_BLUETOOTH);
        //            return;
        //        }
        //
        //        BluetoothGattCharacteristic characteristic;
        //        boolean worked;
        //        BluetoothGattDescriptor descriptor;
        //        //        BluetoothGattCharacteristic characteristic =
        //        //                this.mBluetoothGatt.getService(TEMPERATURE_SERVICE_UUID)
        //        //                        .getCharacteristic(TEMPERATURE_MEASURMENT_UUID);
        //
        //
        //        characteristic =
        //                this.mBluetoothGatt.getService(TEMPERATURE_SERVICE_UUID)
        //                        .getCharacteristic(TEMPERATURE_MEASURMENT_UUID);
        //
        //        worked = this.mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        //        descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        //        if(descriptor == null)
        //        {
        //            Log.e(TAG, "descriptor is null, probably wrong UUID");
        //        } else
        //        {
        //            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        //            this.mBluetoothGatt.writeDescriptor(descriptor);
        //        }

    }

    /**
     * Enable Notification on TX characteristic
     *
     * @return
     */
    public void enableNotificationSession()
    {
        //        if(this.mBluetoothGatt == null)
        //        {
        //            Log.e(TAG, "mBluetoothGatt null");
        //            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_BLUETOOTH);
        //            return;
        //        }
        //
        //        BluetoothGattCharacteristic characteristic;
        //        boolean worked;
        //        BluetoothGattDescriptor descriptor;
        //
        //        characteristic =
        //                this.mBluetoothGatt.getService(SESSION_START_TIME_SERVICE_UUID)
        //                        .getCharacteristic(SESSION_START_TIME_SESSION_UUID);
        //
        //        worked = this.mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        //        descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        //        if(descriptor == null)
        //        {
        //            Log.e(TAG, "descriptor is null, probably wrong UUID");
        //        } else
        //        {
        //            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        //            this.mBluetoothGatt.writeDescriptor(descriptor);
        //        }

    }

    //    private void showMessage(String msg)
    //    {
    //        Log.e(TAG, msg);
    //    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices()
    {
        if(mBluetoothGatt == null)
        {
            return null;
        }

        return mBluetoothGatt.getServices();
    }

    private UUID convertFromInteger(int i)
    {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    public class LocalBinder extends Binder
    {
        BluetoothService getService()
        {
            return BluetoothService.this;
        }
    }
}
