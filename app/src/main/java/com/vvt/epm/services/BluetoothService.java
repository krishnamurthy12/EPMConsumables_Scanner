package com.vvt.epm.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.vvt.epm.utils.Utilities.turnOnBluetooth;


public class BluetoothService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    public static final String B_UUID = "00001101-0000-1000-8000-00805f9b34fb";
// 00000000-0000-1000-8000-00805f9b34fb

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private ConnectBtThread mConnectThread;
    private static ConnectedBtThread mConnectedThread;

    private static Handler mHandler = null;
    //public static Handler bluetoothIn;
    final int handlerState = 0;
    public static int mState = STATE_NONE;

//IBinder mIBinder = new LocalBinder();

    static Context context;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //mHandler = getApplication().getHandler();
        return null;
    }
    public void toast(String mess){
        Toast.makeText(this,mess,Toast.LENGTH_SHORT).show();
    }
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context=this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //String deviceg = intent.getStringExtra("DeviceAddress");

        SharedPreferences sharedPreferences=getSharedPreferences("BTDETAILS",MODE_PRIVATE);
        String devicAddress =sharedPreferences.getString("MACADDRESS",null);
        String deviceName=sharedPreferences.getString("DEVICENAME",null);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.v("BluetoothService","bBluetooth Connected"+Thread.currentThread().getId());

        connectToDevice(devicAddress);
        receiveDataFromBT();

        return START_STICKY;
    }
    private synchronized void connectToDevice(String macAddress){
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
        if (mState == STATE_CONNECTING){
            if (mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectBtThread(device);
        toast("connecting...");
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    private void setState(int state){
        mState = state;
        if (mHandler != null){
            // mHandler.obtainMessage();
        }
    }
    public synchronized void stop(){
        setState(STATE_NONE);
        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mBluetoothAdapter != null){
            mBluetoothAdapter.cancelDiscovery();
        }

        stopSelf();
    }

    public static void sendData(String message){

        Log.d("BluetoothService", "inside sendData, value=>" + message);
        try {
            if (mConnectedThread != null) {
                mConnectedThread.write(message.getBytes());
                Log.d("BluetoothService", "inside sendData, mConnectedThread=>true");
            } else {
                Log.d("BluetoothService", "inside sendData, mConnectedThread=>false");
            }
        } catch (Exception e) {
           // writeToLogFile(context,"Exception in sendData() method while sending data to BT in service:  and exception is=>"+e.getMessage()+"");
            //Log.d("findoutrootcause","Exception while sending data to BT=>"+e.getMessage());
        }
    }



    @Override
    public boolean stopService(Intent name) {
        setState(STATE_NONE);

        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mBluetoothAdapter.cancelDiscovery();
        return super.stopService(name);
    }

    private synchronized void connected(BluetoothSocket mmSocket){

        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedBtThread(mmSocket);
        mConnectedThread.start();


        setState(STATE_CONNECTED);
    }

    private class ConnectBtThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;


        public ConnectBtThread(BluetoothDevice device){
            mDevice = device;
            BluetoothSocket socket = null;

            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(B_UUID));
            } catch (IOException e) {
                //  e.printStackTrace();
               // writeToLogFile(context,"Exception in ConnectBtThread() method while trying createInsecureRfcommSocketToServiceRecord  to BT in service:  and exception is=>"+e.getMessage()+"");
            }
            mSocket = socket;


        }

        @Override
        public void run() {

            mBluetoothAdapter.cancelDiscovery();

            try {
                mSocket.connect();

            } catch (IOException e) {
                // Log.d("BluetoothService","Exception =>"+e.getMessage());
                //writeToLogFile(context,"Exception in ConnectBtThread() run() method while trying mSocket.connect() in service:  and exception is=>"+e.getMessage()+"");

                if(e.getMessage().equalsIgnoreCase("Bluetooth is off"))
                {
                    turnOnBluetooth();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                /*try {
                    mSocket.close();
                    //Log.d("BluetoothService","connect thread run method ( close function)");
                } catch (IOException e1) {
                    writeToLogFile(context,"Exception in ConnectBtThread() run() method while trying mSocket.connect() in service:  and exception is=>"+e.getMessage()+"");
                    //e1.printStackTrace();
                   // Log.d("BluetoothService","connect thread catch exception=>"+e.getMessage());
                }*/
                //e.printStackTrace();
            }
            //connected(mSocket);
            mConnectedThread = new ConnectedBtThread(mSocket);
            mConnectedThread.start();
        }

        public void cancel(){

            try {
                mSocket.close();
                Log.d("BluetoothService","connect thread cancel method");
            } catch (IOException e) {
                // e.printStackTrace();
                //writeToLogFile(context,"Exception in ConnectBtThread() cancel() method while trying mSocket.close() in service:  and exception is=>"+e.getMessage()+"");
            }
        }
    }

    private void receiveDataFromBT() {
       /* bluetoothIn = new Handler(Looper.getMainLooper()) {

            @SuppressLint("SimpleDateFormat")
            public void handleMessage(android.os.Message msg) {
                *//*if (msg.what == handlerState) {

                    String readMessage = (String) msg.obj;

                    Log.d("receivedstring",readMessage);


                }*//*

            }

        };*/
    }

    private class ConnectedBtThread extends Thread {
        private final BluetoothSocket cSocket;
        private final InputStream inS;
        private final OutputStream outS;

        public ConnectedBtThread(BluetoothSocket socket){
            cSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();

            } catch (IOException e) {
                //e.printStackTrace();
               // writeToLogFile(context,"Exception in ConnectedBtThread()  method while getting Input and Output Streams in service:  and exception is=>"+e.getMessage()+"");
            }

            inS = tmpIn;
            outS = tmpOut;
        }

        @Override
        public void run() {
            // Keep looping to listen for received messages
            while (true) {
                int read_bytes=0;
                int availableBytes = 0;
                try {
                    availableBytes = inS.available();
                    if (availableBytes > 0) {

                        byte[] bufffer = new byte[availableBytes];

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                           // writeToLogFile(context,"Exception in ConnectedBtThread()  run() method while trying Thread.sleep(1000) in service:  and exception is=>"+e.getMessage()+"");
                        }


                        /*
                        InputStream.read(byte[] b) method reads b.length number of bytes
                        from the input stream to the buffer array b
                        */
                        try {
                            read_bytes = inS.read(bufffer);  //read bytes from input buffer
                        }catch (IOException e)
                        {
                           // writeToLogFile(context,"Exception in ConnectedBtThread()  while reading data from input stream in service:  and exception is=>"+e.getMessage()+"");
                        }

                        String readMessage = new String(bufffer, 0, read_bytes);
                        Log.d("datareceiving",readMessage);
                        if(readMessage.startsWith("Temperature:"))
                        {
                            if(readMessage.length()>=17)
                            {
                                readMessage=readMessage.trim();
                                //bluetoothIn.obtainMessage(handlerState, bytes, -1, bufffer).sendToTarget();
                                //dataReceiveHandler.obtainMessage(handlerState, read_bytes, -1, readMessage).sendToTarget();
                            }

                        }

                    }
                    else {
                        // Log.d("datareceiving","available bytes < 0");
                    }
                } catch (IOException e) {
                  //  writeToLogFile(context,"Exception in ConnectedBtThread()  whole run() method  in service:  and exception is=>"+e.getMessage()+"");
                    break;
                }
            }
            //Log.d("BluetoothService","connected thread run method");

        }


        public void write(byte[] buff){
            try {
                outS.write(buff);
            } catch (IOException e) {
                //e.printStackTrace();
               // writeToLogFile(context,"Exception in write(byte[] buff) method  in service:  and exception is=>"+e.getMessage()+"");
                Toast.makeText(BluetoothService.this, "Communication lost with BT", Toast.LENGTH_SHORT).show();
                startService(new Intent(BluetoothService.this, BluetoothService.class));
            }
        }

        private void cancel(){
            try {
                cSocket.close();
                Log.d("service","connected thread cancel method");
            } catch (IOException e) {
                e.printStackTrace();
               // writeToLogFile(context,"Exception in cancel() method while trying cSocket.close()  in service:  and exception is=>"+e.getMessage()+"");
            }
        }
    }

    @Override
    public void onDestroy() {
        this.stop();
        super.onDestroy();
    }
}
