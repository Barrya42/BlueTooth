package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class BluetoothListAdapter extends RecyclerView.Adapter implements SingleObserver<BluetoothDevice>
{


    private List<BluetoothDevice> bluetoothDevices;

    private Disposable disposableSource;
    private DeviceInteractable deviceInteractable;
    private View.OnClickListener onClickConnectListener;
    private View.OnClickListener onClickOpenListener;


    public BluetoothListAdapter()
    {
        bluetoothDevices = new ArrayList<>();

        //Добавим спаренные устройства поумолчанию
        for (BluetoothDevice bluetoothDevice : BluetoothAdapter.getDefaultAdapter()
                                                               .getBondedDevices())
        {
            bluetoothDevices.add(bluetoothDevice);
        }

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
        BluetoothDevice device = bluetoothDevices.get(position);
        BluetoothVH bluetoothVH = ((BluetoothVH) holder);
        View root = bluetoothVH.getRootView();
        root.setTag(position);
        bluetoothVH.setTvMac(device.getAddress());
        bluetoothVH.setTvName(device.getName() + (device.getBondState() == BluetoothDevice.BOND_BONDED ? " (paired)" : ""));

        bluetoothVH.getBtConnect()
                   .setOnClickListener(view ->
                   {
                       int i = (Integer) (root).getTag();
                       if (deviceInteractable != null)
                       {
                           deviceInteractable.OnClickConnect(bluetoothVH, bluetoothDevices.get(i));
                       }
                   });
        bluetoothVH.getBtOpen()
                   .setOnClickListener(view ->
                   {
                       int i = (Integer) (root).getTag();
                       if (deviceInteractable != null)
                       {
                           deviceInteractable.OnClickOpen(bluetoothVH, bluetoothDevices.get(i));
                       }
                   });
        root.setOnLongClickListener(view ->
        {
            int i = (Integer) (root).getTag();
            if (deviceInteractable != null)
            {
                deviceInteractable.OnLongClick(bluetoothVH, bluetoothDevices.get(i));
            }
            return true;
        });
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
        if (disposableSource != null)
        {
            disposableSource.dispose();
        }
        disposableSource = d;
    }

    @Override
    public void onSuccess(BluetoothDevice o)
    {
        Log.d("OnScan", "Adapter found device");
        if (!bluetoothDevices.contains(o))
        {
            bluetoothDevices.add(o);
            notifyItemInserted(bluetoothDevices.size() - 1);
        }

//        notifyDataSetChanged();


    }

    @Override
    public void onError(Throwable e)
    {

    }

    public void ClearDevices(boolean RemoveOnlyNotPaired)
    {
        if (RemoveOnlyNotPaired)
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            {
                for (int i = 0; i <= bluetoothDevices.size() - 1; i++)
                {
                    BluetoothDevice bluetoothDevice = bluetoothDevices.get(i);
                    if (bluetoothDevice.getBondState() != bluetoothDevice.BOND_BONDED)
                    {
                        bluetoothDevices.remove(bluetoothDevice);
                        notifyItemRemoved(i);
                    }
                }
            }
            else
            {
                bluetoothDevices = bluetoothDevices.stream()
                                                   .filter(bt -> bt.getBondState() == BluetoothDevice.BOND_BONDED)
                                                   .collect(Collectors.toList());
                notifyDataSetChanged();
            }
        }
        else
        {
            bluetoothDevices.clear();
            notifyDataSetChanged();
        }

    }

    public void setDeviceInteractable(DeviceInteractable deviceInteractable)
    {
        this.deviceInteractable = deviceInteractable;
    }

    public interface DeviceInteractable
    {
        void OnClickConnect(BluetoothVH vh, BluetoothDevice bluetoothDevice);

        void OnClickOpen(BluetoothVH vh, BluetoothDevice bluetoothDevice);

        void OnLongClick(BluetoothVH vh, BluetoothDevice bluetoothDevice);
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

        public View getRootView()
        {
            return rootView;
        }

        public void setTvName(String tvName)
        {
            this.tvName.setText(tvName);
        }

        public void setTvMac(String tvMac)
        {
            this.tvMac.setText(tvMac);
        }

        public View getPbConnecting()
        {
            return pbConnecting;
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
