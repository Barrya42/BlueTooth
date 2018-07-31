package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import java.io.IOException;
import java.util.UUID;

import io.reactivex.Single;
import io.reactivex.functions.Action;

public class BTHelper
{
    private static String myUUID = "00001101-0000-1000-8000-00805F9B34FB";
    private final BluetoothAdapter bluetoothHardwareAdapter;
    private BluetoothSocket bluetoothSocket;
    private BroadcastReceiver receiver;
    private Context context;
    private BluetoothListAdapter bluetoothListAdapter;

    // TODO: 31.07.2018 Реализовать запрос на спаривание
    BTHelper(Context context, BluetoothListAdapter bluetoothListAdapter)
    {
        this.context = context;
        bluetoothHardwareAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothListAdapter = bluetoothListAdapter;
    }


    private void ConnectToSocket(BluetoothDevice bluetoothDevice) throws IOException
    {
        if (bluetoothSocket == null)
        {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(myUUID));
        }

        if (!bluetoothSocket.isConnected())
        {
            bluetoothSocket.connect();
        }
    }

    public Single<Door> Connect(BluetoothDevice bluetoothDevice)
    {
        return Single.fromCallable(() ->
        {
            ConnectToSocket(bluetoothDevice);
            return new BTExchangeRxJava(bluetoothSocket);
        });
    }

    public Single<Door> Connect(String address)
    {

        return Single.fromCallable(() ->
        {
            ConnectToSocket(bluetoothHardwareAdapter.getRemoteDevice(address));
            return new BTExchangeRxJava(bluetoothSocket);
        });
    }

    public void StartScan(Action onFinish)
    {
        if (!bluetoothHardwareAdapter.isDiscovering())
        {
            if (receiver == null)
            {
                receiver = new BroadcastReceiverScan(bluetoothListAdapter, onFinish);
            }


            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothDevice.ACTION_UUID);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            //unregisterReceiver(receiver.getReceiver());

            context.registerReceiver(receiver, intentFilter);
            bluetoothHardwareAdapter.startDiscovery();
        }
    }
    public boolean isConnected()
    {
        return bluetoothSocket != null && bluetoothSocket.isConnected();
    }
}
