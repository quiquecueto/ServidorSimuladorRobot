package com.example.servidorsimuladorrobot;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothServerManager extends Thread{
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothServerSocket bluetoothServerSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Handler handlerNetworkExecutorResult;
    public static Boolean exitCurrentConnection = false;

    @SuppressLint("MissingPermission")
    public void BluetoothServerManagerClass(BluetoothAdapter _bluetoothAdapter, Handler _handlerNetworkExecutorResult) {
        this.bluetoothAdapter = _bluetoothAdapter;
        this.handlerNetworkExecutorResult = _handlerNetworkExecutorResult;
        try {
            UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            String NAME = "BTROBOTSERVERSIM";
            bluetoothServerSocket = _bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
            Log.e("Error:","Bluetooth connection:"+e);
        }
    }

    public void showDisplayMessage(String displayMessage) {
        Message msg = new Message();
        msg.arg1 = 0;
        msg.obj = displayMessage.replaceAll("_", " ");
        handlerNetworkExecutorResult.sendMessage(msg);
    }
    public void showSocketStateMessage(String displayMessage) {
        Message msg = new Message();
        msg.arg1 = 1;
        msg.obj = displayMessage.replaceAll("_", " ");
        handlerNetworkExecutorResult.sendMessage(msg);
    }

    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                showSocketStateMessage("WAITING FOR CONNECTION");
                socket = bluetoothServerSocket.accept();
                String remoteDeviceAddress = socket.getRemoteDevice().getAddress();
                @SuppressLint("MissingPermission") String remoteDeviceName = socket.getRemoteDevice().getName();
                showSocketStateMessage("CONNECTED: NAME="+remoteDeviceName+": MAC="+remoteDeviceAddress);
            } catch (IOException e) {
                showSocketStateMessage("SOCKET ERROR:"+e);
                break;
            }
            //Si la conexion fue aceptada
            if (socket != null) {
                manageConnectedSocket(socket);
            }
        }
    }

    public void manageConnectedSocket(BluetoothSocket socket){
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            exitCurrentConnection = false;
            while (!exitCurrentConnection) {
                String cadena = "";
                byte[] buffer = new byte[1024];
                for (int i=0; i<1024; i++){
                    buffer[i]=0;
                }
                inputStream.read(buffer);
                cadena = new String(buffer);
                if (cadena != null) {
                    if((cadena.equals("EXIT")||(cadena.equals("exit")))){
                        exitCurrentConnection = true;
                    }else{
                        showDisplayMessage(cadena);
                    }
                }
            }
        }catch(IOException e){
            Log.d("ERROR:",""+e);
        }
    }
}
