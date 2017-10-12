package org.ia.btcomm.view.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;

import org.ia.btcomm.R;
import org.ia.btcomm.bean.BTDevice;
import org.ia.btcomm.utils.MyUUID;

import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CommActivity extends AppCompatActivity {

    private static final String TAG = CommActivity.class.getSimpleName();

    @BindView(R.id.tb_comm)
    Toolbar tbComm;
    @BindView(R.id.btn_test)
    Button btnTest;
    @BindView(R.id.tl_comm)
    TabLayout tlComm;
    @BindView(R.id.vp_comm)
    ViewPager vpComm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comm);
        ButterKnife.bind(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();
    }

    private BluetoothSocket clientSocket;

    private void connect() {
        BTDevice btDevice = getIntent().getParcelableExtra("device");
        BluetoothDevice device = btDevice.getDevice();
        try {
            clientSocket = device.createRfcommSocketToServiceRecord(MyUUID.COMM_UUID);
            clientSocket.connect();
            Log.e(TAG, "connected");
        } catch (IOException e) {
            Log.e(TAG, "connect exception " + e.getMessage());
        }
    }

    @OnClick(R.id.btn_test)
    void onSend() {
        sendMsg("hehehe");
    }

    private void sendMsg(String msg) {
        Observable
                .just(msg)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if (clientSocket != null) {
                            OutputStream writer = null;
                            try {
                                writer = clientSocket.getOutputStream();
                                if (writer != null)
                                    writer.write(s.getBytes());
                            } catch (Exception e) {

                            } finally {
                                if (writer!= null) {
                                    writer.close();
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeSocket();
    }

    private void closeSocket() {
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
