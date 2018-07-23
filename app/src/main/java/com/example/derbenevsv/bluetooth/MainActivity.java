package com.example.derbenevsv.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
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

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements CheckBox.OnCheckedChangeListener
{
    BluetoothDevice bluetoothDevice;

    String adress = "00:21:13:04:af:f2".toUpperCase();
    //String adress = "00:18:e4:34:f0:2e".toUpperCase();
    //BroadcastReceiverScan reciver;
    BluetoothListAdapter bluetoothListAdapter;
    private EditText etMac;
    private Button btScan;
    private CheckBox cbAutoOPen;
    //    private ExchangeRxJava exchangeTask;
    private Door door;
    private BTHelper btHelper;
    private Button btOpen;
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
        btOpen = findViewById(R.id.btOpen);
        cbAutoOPen.setOnCheckedChangeListener(this);
        constraintLayout = findViewById(R.id.mainLayout);
        rvDevices = findViewById(R.id.rvDevices);
        bluetoothListAdapter = new BluetoothListAdapter();
        btHelper = new BTHelper(this, bluetoothListAdapter);
        rvDevices.setAdapter(bluetoothListAdapter);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));

        toDispose = new CompositeDisposable();
        btOpen.setOnClickListener(view ->
        {
            try
            {
               door = btHelper.Connect(adress);
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }
            door.OpenDoor()
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(e ->
                            Log.d("LOG", e.toString()), throwable ->
                            throwable.printStackTrace());
        });


    }


    @Override
    protected void onResume()
    {
        super.onResume();

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
        btHelper.StartScan(this::OnScanFinish);


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
