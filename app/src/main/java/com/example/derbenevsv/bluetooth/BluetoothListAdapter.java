package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class BluetoothListAdapter extends RecyclerView.Adapter implements SingleObserver<BluetoothDevice>
{


    //private BluetoothDeviceListObserver bluetoothDeviceListObserver;ы
    private ArrayList<BluetoothDevice> bluetoothDevices;



    private Disposable disposable;

    public BluetoothListAdapter()
    {
        //bluetoothDeviceListObserver = new BluetoothDeviceListObserver(this);
        bluetoothDevices = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetooth_list_item, parent, false);
        BluetoothVH vh = new BluetoothVH(mView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        BluetoothVH bluetoothVH = ((BluetoothVH) holder);

        BluetoothDevice device = bluetoothDevices
                .get(position);
        bluetoothVH.setTvMac(device.getAddress());
        bluetoothVH.setTvName(device.getName() + (device.getBondState() == BluetoothDevice.BOND_BONDED ? " (paired)" : ""));
    }

    @Override
    public int getItemCount()
    {
        return bluetoothDevices
                .size();
    }

    @Override
    public int getItemViewType(int position)
    {
        return 1;
    }


    @Override
    public void onSubscribe(Disposable d)
    {
        if (disposable != null)
        {
            disposable.dispose();
        }
        disposable = d;
    }

    @Override
    public void onSuccess(BluetoothDevice o)
    {
        Log.d("OnScan", "Adapter found device");
        bluetoothDevices.add(o);
        notifyDataSetChanged();


    }

    @Override
    public void onError(Throwable e)
    {

    }

    public void ClearDevices()
    {
        bluetoothDevices.clear();
    }
    class BluetoothVH extends RecyclerView.ViewHolder
    {
        private TextView tvName;
        private TextView tvMac;

        public BluetoothVH(View itemView)
        {
            super(itemView);
            tvMac = itemView.findViewById(R.id.tvMac);
            tvName = itemView.findViewById(R.id.tvName);
        }

        public void setTvName(String tvName)
        {
            this.tvName.setText(tvName);
        }

        public void setTvMac(String tvMac)
        {
            this.tvMac.setText(tvMac);
        }
    }


}
