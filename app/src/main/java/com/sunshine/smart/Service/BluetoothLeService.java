/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sunshine.smart.Service;

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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.sunshine.smart.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 * 通过一个device就可以获得service和管理所有的service和特征
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }
    //BLE连接状态  默认未连接
    public  boolean bleState = false;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress = "";
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    public static boolean mySwitch = false; //这里标记是手动关闭 不再重启尝试连接 为真是 手动断开模式   不再尝试重连./
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public final static String ACTION_GATT_CONNECTING =
            "com.stormrage.smart.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_CONNECTED =
            "com.stormrage.smart.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.stormrage.smart.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.stormrage.smart.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.stormrage.smart.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.stormrage.smart.EXTRA_DATA";
    public final static String ACTION_WRITE_COMPLATE = "com.stotmrage.smart.ACTION_WRITE_COMPLATE";

    //设备服务
    public BluetoothGattService GattService;
    public BluetoothGattCharacteristic StatusChar;
    public BluetoothGattCharacteristic ControlChar;
    public BluetoothGattCharacteristic PasswdChar;
    public Handler mHandelr = new Handler();

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bleState = false;
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.e(TAG, "Disconnected from GATT server.status:"+status+"---new:"+newState);
                broadcastUpdate(intentAction);
                if (status==133){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(200);
                                disconnect();
                                Thread.sleep(200);
                                close();
                                Thread.sleep(200);
                                connect(mBluetoothDeviceAddress);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //note connect success
                bleState = true;
                GattService = gatt.getService(UUID.fromString(Constants.SERVICE_UUID));
                StatusChar = GattService.getCharacteristic(UUID.fromString(Constants.STATUS_UUID));
                ControlChar = GattService.getCharacteristic(UUID.fromString(Constants.CONTROL_UUID));
                PasswdChar = GattService.getCharacteristic(UUID.fromString(Constants.PASSWD_UUID));
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.i(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
            //run go
            processTxQueue();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            switch (status){
                case BluetoothGatt.GATT_SUCCESS:
                    Log.e(TAG,"success");
                    broadcastUpdate(ACTION_WRITE_COMPLATE);
                    break;
                default:
                    Log.e(TAG,"write error :"+status);
                    break;

            }
            processTxQueue();
            Log.e(TAG,status+"------------------------------------------------------");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.e(TAG,characteristic.getValue().toString());
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            processTxQueue();
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    //读取特征更新广播
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        // 所有写入都用16进制
        final byte[] data = characteristic.getValue();
        StringBuilder stringBuilder = new StringBuilder(data.length);
        for (byte b:data){
//            Log.e(TAG,"byte:"+b);
            stringBuilder.append(b);
        }
//        Log.e(TAG,"length:"+data.length+" bytes:contents:"+stringBuilder.toString());

        if (characteristic.getUuid().toString().equals(Constants.STATUS_UUID)){
            intent.putExtra("type","status");
        }else if(characteristic.getUuid().toString().equals(Constants.CONTROL_UUID)){
            intent.putExtra("type","control");
        }else if(characteristic.getUuid().toString().equals(Constants.PASSWD_UUID)){
            intent.putExtra("type","passwd");
        }
        intent.putExtra(EXTRA_DATA,data);
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    private int countA = 0;
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()){
            Log.e(TAG,"open bluetooth success.");
            if (!mBluetoothAdapter.enable()){
                Log.e(TAG,"open ble failed.");
            }
        }

        return true;
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public synchronized boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address. connect failed");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                Log.i(TAG,"try existing success...");
                //发送连接广播
                String intentAction = ACTION_GATT_CONNECTING;
                mConnectionState = STATE_CONNECTING;
                Log.i(TAG,"connecting...");
                broadcastUpdate(intentAction);
                return true;
            } else {
                Log.i(TAG,"try existing faild...");
                return false;
            }
        }

        //发送连接广播
        String intentAction = ACTION_GATT_CONNECTING;
        mConnectionState = STATE_CONNECTING;
        Log.i(TAG,"connecting...");
        broadcastUpdate(intentAction);
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.!!!");
            return false;
        }
        mBluetoothGatt = device.connectGatt(BluetoothLeService.this, false, mGattCallback);
        Log.i(TAG, "Trying to create a new connection.");
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
    public void disconnect() {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "BluetoothAdapter not initialized from disconnect");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        //对于调用了clos的连接 全部不在尝试重连.
        Log.e(TAG,"ble service is close");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (characteristic == null){
            Log.e(TAG,"Characteristic is null!");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    public synchronized void writeControl(byte[] data){
        if (mBluetoothGatt==null){
            Log.e(TAG,"gatt is null");
            return;
        }
        if (ControlChar==null){
            Log.e(TAG,"char has not init .return ");
            return;
        }

        StringBuilder stringBuilder = new StringBuilder(data.length);
        for (byte b:data){
//            Log.e(TAG,"byte:"+b);
            stringBuilder.append(String.format("%02X ", b));
        }
        Log.e(TAG,"write control:"+stringBuilder.toString());
        ControlChar.setValue(data);
        mHandelr.postDelayed(new Runnable() {
            @Override
            public void run() {
                setCharacteristicNotification(ControlChar,true);
            }
        },300);

        mBluetoothGatt.writeCharacteristic(ControlChar);
    }

    public void writePass(byte[] data){
        StringBuilder stringBuilder = new StringBuilder(data.length);
        for (byte b:data){
//            Log.e(TAG,"byte:"+b);
            stringBuilder.append(String.format("%02X ", b));
        }
        Log.e(TAG,"write pwd:"+stringBuilder.toString());
        PasswdChar.setValue(data);
        mBluetoothGatt.setCharacteristicNotification(PasswdChar,true);
        mBluetoothGatt.writeCharacteristic(PasswdChar);
    }

    public void readCharacteristic(int i){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized from read");
            return;
        }
        if (StatusChar==null || ControlChar == null ||PasswdChar == null){
            Log.e(TAG,"found null characteristic .break.");
            return;
        }
        switch (i){
            case 1:
//                Log.e(TAG,"读取基本状态");
                mBluetoothGatt.readCharacteristic(StatusChar);
                break;
            case 2:
//                Log.e(TAG,"读取控制状态"+ControlChar.toString());
                mBluetoothGatt.readCharacteristic(ControlChar);
                break;
            case 3:
                Log.e(TAG,"service读取密码特征"+PasswdChar.toString());
                mBluetoothGatt.readCharacteristic(PasswdChar);
                break;
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "BluetoothAdapter not initialized from setCharacteristicNotification");
            return;
        }

        List<BluetoothGattDescriptor> bluetoothGattDescriptors = characteristic.getDescriptors();
        for (BluetoothGattDescriptor dp : bluetoothGattDescriptors){
            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    /* An enqueueable write operation - notification subscription or characteristic write */
    private class TxQueueItem
    {
        BluetoothGattCharacteristic characteristic;
        byte[] dataToWrite; // Only used for characteristic write
        boolean enabled; // Only used for characteristic notification subscription
        public TxQueueItemType type;
    }

    /**
     * The queue of pending transmissions
     */
    private Queue<TxQueueItem> txQueue = new LinkedList<TxQueueItem>();

    private boolean txQueueProcessing = false;

    private enum TxQueueItemType {
        SubscribeCharacteristic,
        ReadCharacteristic,
        WriteCharacteristic,
        password
    }

    /* queues enables/disables notification for characteristic */
    public void queueSetNotificationForCharacteristic(BluetoothGattCharacteristic ch, boolean enabled)
    {
        // Add to queue because shitty Android GATT stuff is only synchronous
        TxQueueItem txQueueItem = new TxQueueItem();
        txQueueItem.characteristic = ch;
        txQueueItem.enabled = enabled;
        txQueueItem.type = TxQueueItemType.SubscribeCharacteristic;
        addToTxQueue(txQueueItem);
    }

    /* queues enables/disables notification for characteristic */
    public void queueWriteDataToCharacteristic(final byte[] dataToWrite,int i)
    {
        // Add to queue because shitty Android GATT stuff is only synchronous
        TxQueueItem txQueueItem = new TxQueueItem();
        switch (i){
            case 2:
                txQueueItem.characteristic = ControlChar;
                txQueueItem.type = TxQueueItemType.WriteCharacteristic;
                break;
            case 3:
                txQueueItem.characteristic = PasswdChar;
                txQueueItem.type = TxQueueItemType.password;
                break;
        }
        txQueueItem.dataToWrite = dataToWrite;

        addToTxQueue(txQueueItem);
    }

    /* request to fetch newest value stored on the remote device for particular characteristic */
    public void queueRequestCharacteristicValue(int i) {

        // Add to queue because shitty Android GATT stuff is only synchronous
        TxQueueItem txQueueItem = new TxQueueItem();
        switch (i){
            case 1:
                txQueueItem.characteristic = StatusChar;
                break;
            case 2:
                txQueueItem.characteristic = ControlChar;
                break;
            case 3:
                txQueueItem.characteristic = PasswdChar;
                break;
        }
        txQueueItem.type = TxQueueItemType.ReadCharacteristic;
        addToTxQueue(txQueueItem);
    }

    /**
     * Add a transaction item to transaction queue
     * @param txQueueItem
     */
    private void addToTxQueue(TxQueueItem txQueueItem) {

        txQueue.add(txQueueItem);

        Log.e(TAG,"add:"+txQueue.size());
        // If there is no other transmission processing, go do this one!
        if (!txQueueProcessing || txQueue.size() > 5) {
            processTxQueue();
        }
    }

    /**
     * Call when a transaction has been completed.
     * Will process next transaction if queued
     */
    private void processTxQueue()
    {
        if (txQueue.size() <= 0)  {
            txQueueProcessing = false;
            return;
        }

        txQueueProcessing = true;
        TxQueueItem txQueueItem = txQueue.remove();
        switch (txQueueItem.type) {
            case WriteCharacteristic:
                writeControl(txQueueItem.dataToWrite);
//                writeDataToCharacteristic(txQueueItem.characteristic, txQueueItem.dataToWrite);
                break;
            case password:
                writePass(txQueueItem.dataToWrite);
                break;
            case SubscribeCharacteristic:
                //带回应
//                setNotificationForCharacteristic(txQueueItem.characteristic, txQueueItem.enabled);
                break;
            case ReadCharacteristic:
                readCharacteristic(txQueueItem.characteristic);
//                requestCharacteristicValue(txQueueItem.characteristic);
        }
    }

}
