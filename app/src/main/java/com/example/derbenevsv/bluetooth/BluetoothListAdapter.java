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


    private ArrayList<BluetoothDevice> bluetoothDevices;

    private Disposable disposable;
    private DeviceInteractable deviceInteractable;
    private View.OnClickListener onClickConnectListener;
    private View.OnClickListener onClickOpenListener;


    public BluetoothListAdapter()
    {
        bluetoothDevices = new ArrayList<>();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetooth_list_item, parent, false);
        BluetoothVH vh = new BluetoothVH(mView);
        if (onClickOpenListener == null)
        {
            onClickOpenListener = view ->
            {
                int i = (Integer) (mView).getTag();
                if (deviceInteractable != null)
                {
                    deviceInteractable.OnClickOpen(vh, bluetoothDevices.get(i));
                }
            };
        }
        if (onClickConnectListener == null)
        {
            onClickConnectListener = view ->
            {
                int i = (Integer) (mView).getTag();
                if (deviceInteractable != null)
                {
                    deviceInteractable.OnClickConnect(vh, bluetoothDevices.get(i));
                }
            };
        }
        vh.setOnConnectClickListener(onClickConnectListener);
        vh.setOnOpenClickListener(onClickOpenListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        BluetoothDevice device = bluetoothDevices.get(position);
        BluetoothVH bluetoothVH = ((BluetoothVH) holder);
        bluetoothVH.setId(position);
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

    public void setDeviceInteractable(DeviceInteractable deviceInteractable)
    {
        this.deviceInteractable = deviceInteractable;
    }

    public interface DeviceInteractable
    {
        void OnClickConnect(BluetoothVH vh, BluetoothDevice bluetoothDevice);

        void OnClickOpen(BluetoothVH vh, BluetoothDevice bluetoothDevice);
    }

    class BluetoothVH extends RecyclerView.ViewHolder
    {
        private TextView tvName;
        private TextView tvMac;

        private View btConnect;
        private View btOpen;
        private View pbConnecting;

        private View rootView;

        public BluetoothVH(View itemView)
        {
            super(itemView);
            tvMac = itemView.findViewById(R.id.tvMac);
            tvName = itemView.findViewById(R.id.tvName);
            btConnect = itemView.findViewById(R.id.btConnect);
            btOpen = itemView.findViewById(R.id.btOpen);
            pbConnecting = itemView.findViewById(R.id.pbConnecting);
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

        public void setOnConnectClickListener(View.OnClickListener onConnectClickListener)
        {
            btConnect.setOnClickListener(onConnectClickListener);
        }

        public void setOnOpenClickListener(View.OnClickListener onOpenClickListener)
        {
            btOpen.setOnClickListener(onOpenClickListener);
        }

        public void setConnectingProgressBarVisibility(int visibility)
        {
            pbConnecting.setVisibility(visibility);
//            btConnect.setVisibility(visibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        }

        public View getBtConnect()
        {
            return btConnect;
        }

        public View getBtOpen()
        {
            return btOpen;
        }
    }
}
