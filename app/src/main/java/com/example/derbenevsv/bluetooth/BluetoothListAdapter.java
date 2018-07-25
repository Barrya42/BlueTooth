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

    //private BluetoothDevice selectedDevice;
    private Disposable disposable;
    private DeviceSelectable deviceSelectable;
    private View.OnClickListener onClickListener;


    public BluetoothListAdapter()
    {
        //bluetoothDeviceListObserver = new BluetoothDeviceListObserver(this);
        bluetoothDevices = new ArrayList<>();
        onClickListener = view ->
        {
            int i = (Integer) view.getTag();
            Log.d("LOG", String.valueOf(i));
            if (deviceSelectable != null)
            {
                deviceSelectable.OnSelect(bluetoothDevices.get(i));
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetooth_list_item, parent, false);
        BluetoothVH vh = new BluetoothVH(mView);
        mView.setOnClickListener(onClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        BluetoothVH bluetoothVH = ((BluetoothVH) holder);
        bluetoothVH.setId(position);
        BluetoothDevice device = bluetoothDevices.get(position);
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
        if (!bluetoothDevices.contains(o))
        {
            bluetoothDevices.add(o);
        }
        notifyDataSetChanged();


    }

    @Override
    public void onError(Throwable e)
    {

    }

    public void ClearDevices()
    {
        bluetoothDevices.clear();
        notifyDataSetChanged();
    }


    public interface DeviceSelectable
    {
        void OnSelect(BluetoothDevice bluetoothDevice);
    }

    class BluetoothVH extends RecyclerView.ViewHolder
    {
        private TextView tvName;
        private TextView tvMac;
        private View rootView;

        public BluetoothVH(View itemView)
        {
            super(itemView);
            tvMac = itemView.findViewById(R.id.tvMac);
            tvName = itemView.findViewById(R.id.tvName);
            rootView = itemView;
        }

        public void setId(int newId)
        {
            rootView.setTag(newId);
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
