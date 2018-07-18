package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

//
public class BroadcastReceiverScan extends BroadcastReceiver
{
    private SingleObserver<BluetoothDevice> observer;

    private Action completableObserver;

    public BroadcastReceiverScan(SingleObserver<BluetoothDevice> bluetoothDeviceListObserver, Action onFinish)
    {
        observer = bluetoothDeviceListObserver;
        this.completableObserver = onFinish;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {

        if (intent.getAction()
                .equals(BluetoothDevice.ACTION_FOUND))
        {
            Single.just((BluetoothDevice) intent.getExtras()
                    .get(BluetoothDevice.EXTRA_DEVICE))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);

        }
        else if (intent.getAction()
                .equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
        {

            Completable.complete()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(completableObserver);

        }

    }
}
