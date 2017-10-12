package org.ia.btcomm.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import org.ia.btcomm.utils.MyUUID;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class BtServerService extends Service {

    private static final String TAG = BtServerService.class.getSimpleName();
    private MyBinder myBinder = new MyBinder();
    private BluetoothServerSocket serverSocket;

    public BtServerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        startServer();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e(TAG, "onStart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return myBinder;
    }

    void startServer() {
        ServerThread st = new ServerThread();
        st.start();
    }

    private class MyBinder extends Binder {
        public BtServerService getService() {
            return BtServerService.this;
        }
    }

    private class ServerThread extends Thread {
        @Override
        public void run() {

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (!adapter.isEnabled()) {
                adapter.enable();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (true) {
                try {
                    serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord("SERVER_SOCKET", MyUUID.COMM_UUID);
                    Log.e(TAG, "socket accept start");
                    BluetoothSocket socket = serverSocket.accept();
                    Log.e(TAG, "socket accepted");
                    new ServerReceiveThread(socket).start();

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

        }
    }

    private class ServerReceiveThread extends Thread {

        BluetoothSocket socket;

        public ServerReceiveThread(BluetoothSocket s) {
            socket = s;
        }

        @Override
        public void run() {
            InputStream reader = null;
            try {
                reader = socket.getInputStream();
                byte[] buffer = new byte[1024];
                while (true) {
                    int n = reader.read(buffer);        //监听输入流
                    if (n != -1) {
                        byte[] text = new byte[n];
                        for (int i = 0; i < n; i ++) {
                            text[i] = buffer[i];
                        }
                        String s = new String(text);    //接收到的数据
                        Log.e("receive", "s = " + s);
                    }
                    if (!socket.isConnected()) {
                        Log.e(TAG, "ServerReceiveThread break");
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "ServerReceiveThread Exception " + e.getMessage());
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

}
