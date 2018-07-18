package com.example.derbenevsv.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity implements CheckBox.OnCheckedChangeListener
{
    private static String myUUID = "00001101-0000-1000-8000-00805F9B34FB";
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    String adress = "00:21:13:04:af:f2".toUpperCase();
    BroadcastReceiverScan reciver;
    BluetoothListAdapter bluetoothListAdapter;
    private BluetoothAdapter bluetoothHardwareAdapter;
    private EditText etMac;
    private Button btScan;
    private CheckBox cbAutoOPen;
    private ExchangeRxJava exchangeTask;
    private ConstraintLayout constraintLayout;
    private RecyclerView rvDevices;
    private int ACCESS_COARSE_LOCATION_PERMISSION = 15;
    private CompositeDisposable toDispose;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etMac = findViewById(R.id.etMac);
        etMac.setText(adress);
        btScan = findViewById(R.id.btScan);
        cbAutoOPen = findViewById(R.id.cbAutoOPen);
        btScan.setOnClickListener(view ->
        {
            if (CheckPermissions())
            {
                StartScan();
            }
        });
        cbAutoOPen.setOnCheckedChangeListener(this);
        bluetoothHardwareAdapter = BluetoothAdapter.getDefaultAdapter();
        constraintLayout = findViewById(R.id.mainLayout);
        rvDevices = findViewById(R.id.rvDevices);
//        BluetoothListAdapter.BluetoothDeviceListObserver bluetoothDeviceListObserver = new BluetoothDeviceListObserver();ы
        bluetoothListAdapter = new BluetoothListAdapter();
        rvDevices.setAdapter(bluetoothListAdapter);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
//        reciver = new BroadcastReceiverScan();

        bluetoothDevice = bluetoothHardwareAdapter.getRemoteDevice(adress);
        toDispose = new CompositeDisposable();


    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if (!bluetoothHardwareAdapter.isEnabled())
        {
            bluetoothHardwareAdapter.enable();
        }
        if (CheckPermissions())
        {
            StartScan();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == ACCESS_COARSE_LOCATION_PERMISSION)
        {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(constraintLayout, "Разрешение получено",
                        Snackbar.LENGTH_SHORT)
                        .show();

            }
            else
            {
                // Permission request was denied.
                Snackbar.make(constraintLayout, "Разрешение не получено.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }


    private BluetoothSocket Connect(BluetoothDevice bluetoothDevice) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException
    {

        BluetoothSocket bluetoothSocket = (BluetoothSocket) bluetoothDevice.getClass()
                .getMethod("createInsecureRfcommSocket", new Class[]{int.class})
                .invoke(bluetoothDevice, 1);
        bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(myUUID));


        if (!bluetoothSocket.isConnected())
        {
            bluetoothSocket.connect();
            exchangeTask = new ExchangeRxJava(bluetoothSocket);
        }
        return bluetoothSocket;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        toDispose.dispose();
    }

    private boolean CheckPermissions()
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_DENIED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                // TODO: 12.07.2018 тут по идее должно быть описание зачем нам надо это разрешение
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_PERMISSION);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_PERMISSION);
            }

            return false;
        }
        else
        {
            return true;
        }

    }

    private void StartScan()
    {

        if (!bluetoothHardwareAdapter.isDiscovering())
        {
            if (reciver == null)
            {
                reciver = new BroadcastReceiverScan(bluetoothListAdapter, () ->
                        OnScanFinish());
                //toDispose.add(bluetoothListAdapter.getDisposable());
            }

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothDevice.ACTION_UUID);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            //unregisterReceiver(reciver.getReceiver());
            registerReceiver(reciver, intentFilter);
            bluetoothHardwareAdapter.startDiscovery();
        }

    }

    private void openDoor()
    {


        if (BluetoothAdapter.checkBluetoothAddress(adress))
        {

            if (bluetoothHardwareAdapter.getBondedDevices()
                    .contains(bluetoothDevice))
            {

            }

        }
        else
        {
            Toast.makeText(getApplicationContext(), "adress is not valid.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
    {

    }

    private void OnScanFinish()
    {
        bluetoothListAdapter.ClearDevices();
        StartScan();
        Log.d("OnScan", "Activity finish scan");
    }
}
