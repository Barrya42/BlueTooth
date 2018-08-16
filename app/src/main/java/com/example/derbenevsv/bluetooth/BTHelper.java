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
    public static int STATE_IDLE = 0x0;
    public static int STATE_CONNECTED = 0x2;
    public static int STATE_CONNECTING = 0x3;
    private static String myUUID = "00001101-0000-1000-8000-00805F9B34FB";
    private final BluetoothAdapter bluetoothHardwareAdapter;
    private BluetoothSocket bluetoothSocket;
    private BroadcastReceiver receiver;
    private Context context;
    private BluetoothListAdapter bluetoothListAdapter;
    private int BTstate;

    // TODO: 31.07.2018 Реализовать запрос на спаривание
    BTHelper(Context context, BluetoothListAdapter bluetoothListAdapter)
    {
        BTstate = STATE_IDLE;
        this.context = context;
        bluetoothHardwareAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothListAdapter = bluetoothListAdapter;
    }

    public int getBTstate()
    {
        return BTstate;
    }

    private void ConnectToSocket(BluetoothDevice bluetoothDevice) throws IOException
    {
        if (bluetoothSocket == null)
        {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(myUUID));
        }

        if (!bluetoothSocket.isConnected())
        {
            BTstate = STATE_CONNECTING;
            try
            {
                bluetoothSocket.connect();
                BTstate = STATE_CONNECTED;
            }
            catch (IOException e)
            {
                BTstate = STATE_IDLE;
                throw e;
            }

        }
    }

    public Single<Door> Connect(BluetoothDevice bluetoothDevice)
    {
        return Single.fromCallable(() ->
        {
            try
            {
                ConnectToSocket(bluetoothDevice);
            }
            catch (IOException e)
            {
                //throw new RuntimeException(e.getMessage()).addSuppressed(e);
                IOException tr = new IOException("Не удалось подключиться.");
                tr.addSuppressed(e);
                throw tr;
            }
            return new BTExchangeRxJava(bluetoothSocket);
        });
    }

    public Single<Door> Connect(String address)
    {


        return Single.fromCallable(() ->
        {
            try
            {
                ConnectToSocket(bluetoothHardwareAdapter.getRemoteDevice(address));
            }
            catch (IOException e)
            {
                //throw new RuntimeException(e.getMessage()).addSuppressed(e);
                IOException tr = new IOException("Не удалось подключиться.");
                tr.addSuppressed(e);
                throw tr;
            }
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
            //intentFilter.addAction(BluetoothDevice.ACTION_UUID);
            //intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
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

    public void CloseConnection() throws IOException
    {
        bluetoothSocket.close();
    }
}
