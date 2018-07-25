package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import io.reactivex.functions.Action;

public class BTHelper
{
    private static String myUUID = "00001101-0000-1000-8000-00805F9B34FB";
    private final BluetoothAdapter bluetoothHardwareAdapter;
    BluetoothSocket bluetoothSocket;
    BroadcastReceiver receiver;
    Context context;
    private BluetoothListAdapter bluetoothListAdapter;
    //private BluetoothDevice bluetoothDevice;

    BTHelper(Context context, BluetoothListAdapter bluetoothListAdapter)
    {
        this.context = context;
        bluetoothHardwareAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothListAdapter = bluetoothListAdapter;
    }

    // TODO: 23.07.2018 Этот метод должен выполнятся в другом потоке
    private void ConnectToSocket(BluetoothDevice bluetoothDevice) throws IOException
    {
        if (bluetoothSocket == null)
        {
//            try
//            {
//                bluetoothSocket = (BluetoothSocket) bluetoothDevice.getClass()
//                        .getMethod("createRfcommSocket", new Class[]{int.class})
//                        .invoke(bluetoothDevice, 1);
//            }
//            catch (IllegalAccessException e)
//            {
//                e.printStackTrace();
//            }
//            catch (InvocationTargetException e)
//            {
//                e.printStackTrace();
//            }
//            catch (NoSuchMethodException e)
//            {
//                e.printStackTrace();
//            }
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(myUUID));
        }

        if (!bluetoothSocket.isConnected())
        {
            bluetoothSocket.connect();
        }
    }

    public Door Connect(BluetoothDevice bluetoothDevice) throws IOException
    {
        ConnectToSocket(bluetoothDevice);
        Door door = new BTExchangeRxJava(bluetoothSocket);

        return door;
    }

    public Door Connect(String address) throws IOException
    {
        BluetoothDevice bluetoothDevice = bluetoothHardwareAdapter.getRemoteDevice(address);
        ConnectToSocket(bluetoothDevice);

        Door  door = new BTExchangeRxJava(bluetoothSocket);

        return door;
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
}
