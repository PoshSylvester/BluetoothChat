package com.example.posholi.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * Created by posholi on 1/4/18.
 */

public class ConnectServer extends Thread{

    private final String TAG = this.getClass().getSimpleName();
    private final BluetoothServerSocket mmServerSocket;
    private final String NAME ="Server";


    BluetoothAdapter btAdapter;
    public ConnectServer(BluetoothAdapter adapter){

        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        btAdapter=adapter;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = btAdapter.listenUsingRfcommWithServiceRecord(NAME, AppConstants.uuID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;


    }


    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
               // manageMyConnectedSocket(socket); // TODO: implemnt communications thread
                
                closeSocket(socket);
                break;
            }
        }
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }


    public boolean closeSocket(BluetoothSocket socket){

        if(socket != null){

            try {
                mmServerSocket.close();
                return true;
            }catch (IOException e){
                Log.e(TAG, "Error Closing the socket", e);
            }
        }
        return false;
    }
}
