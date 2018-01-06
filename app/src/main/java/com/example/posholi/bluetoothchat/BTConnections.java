package com.example.posholi.bluetoothchat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BTConnections extends AppCompatActivity implements View.OnClickListener{

    //Constants
    private static final int REQUEST_ENABLE_BT = 1;
    private  final String TAG = this.getClass().getSimpleName();


    //Global variables

    //Bt variables

    private BluetoothAdapter btAdapter;
    private BluetoothDevice device;
    private ProgressDialog mProgressDlg;
    private int selectedDevice;



    // empty list  of devices names
    private List<String> deviceNameList;  // string list of BT devices by names
    private ArrayAdapter<String> arrayAdapter; // array adapter for displaying to the listview

    //Bt device list
    private List <BluetoothDevice> btDeviceList;



    //connection threads
    ConnectServer server;
    ConnectClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set up view
        setContentView(R.layout.activity_btconnections);

        final Button connectBtn=findViewById(R.id.connectBtn);
        final Button discoverBtn=findViewById(R.id.discoverBtn);
        final Button disconnectBtn = findViewById(R.id.disconnectBtn);
        final ListView BTDeviceList = (ListView) findViewById(R.id.listView);

        discoverBtn.setOnClickListener(BTConnections.this);
        connectBtn.setOnClickListener(BTConnections.this);
        disconnectBtn.setOnClickListener(BTConnections.this);


        BTDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                selectedDevice = position;
            }
        });


        //BT device discovery progress dialog
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btAdapter.cancelDiscovery();
            }
        });


        // register bt broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btReceiver, filter);


        // initialize deviceList as ArrayList
         deviceNameList = new ArrayList<String>();

        // Create an ArrayAdapter from deviceList
         arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, deviceNameList);
        // DataBind ListView with items from ArrayAdapter
        BTDeviceList.setAdapter(arrayAdapter);


        //initialize device list
        btDeviceList = new ArrayList<BluetoothDevice>();



        //BT setup
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        setUpBluetooth();

        //list previously paired devices
        getPairedDevices();
    }


    @Override
    public void onClick (View v){

        switch (v.getId()){

            case R.id.discoverBtn: // discover new Bluetooth devices
                discoverDevices();

            case R.id.connectBtn:
                int index = selectedDevice;
                //connectDevice(index);


            case R.id.disconnectBtn:
                //TODO: disconnectDevice(String deviceAdress)
        }
    }


    //enables bluetooth if bluetooth is not already enabled
    //if the device does not support bluetooth, a message about incompatiblity is
    //shown in a pop-up dialog box
    private void setUpBluetooth(){

        if (btAdapter == null) {
            // Device doesn't support Bluetooth
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your device does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();


        }else if (!btAdapter.isEnabled()) { // if the device supports bluetooth
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
            //start connectserver Thread to start accepting incoming connection requests
            server = new ConnectServer(btAdapter);
            server.start();
        }


    }


    //shows the results of the enable bluetooth activity initiated by the setUpBluetooth() method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_ENABLE_BT  && resultCode  == RESULT_OK) {

                //String requiredValue = data.getStringExtra("Key");
                Toast.makeText(getApplicationContext(), "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "Failed to Enable Bluetooth", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Toast.makeText(BTConnections.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }

    //
    //
    //
    private void getPairedDevices(){
        //query previously paired devices first
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. display each paired device
            for (BluetoothDevice device : pairedDevices) {  // iterate over the paired devices set

                //add the device to the list of devices
                btDeviceList.add(device);

                //display device by name on the list of devices
                displayDevice(device);
            }
        }
    }


    //Discover new devices

    private void discoverDevices(){

        //enable coarse location access permission
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);


        // stop existing device discovery first
        // then start new discovery
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }


       btAdapter.startDiscovery();
    }


    // connects to  a selected device (from a list of available devices)
    // returns true if the connection was successful
    //called during a connect-to-device button event
    private void connectDevice(int deviceIndex){

        try {
            this.unregisterReceiver(btReceiver);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        try {
            btAdapter.cancelDiscovery();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

         //start ConnectClient thread to connect to the slected device
        BluetoothDevice targetDevice= btDeviceList.get(deviceIndex);
        client = new ConnectClient(targetDevice,btAdapter);
        client.start();

    }


    // disconnects a selected device (from a list of available devices)
    // returns true if the disconnection was successful
    //called during a disconnect-from-device button event
    private void disconnectDevice(String deviceAdress){


    }


    private void displayDevice(BluetoothDevice device){
        deviceNameList.add(device.getName());
        arrayAdapter.notifyDataSetChanged();

    }



    private BroadcastReceiver btReceiver  = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    //showToast("Enabled");

                   // showEnabled();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                mProgressDlg.show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();
                btAdapter.cancelDiscovery();

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDeviceList.add(device);
                displayDevice(device);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.

        this.unregisterReceiver(btReceiver);
        btAdapter.cancelDiscovery();

        //stop connection threads
        server.cancel();
        client.cancel();
    }


}





