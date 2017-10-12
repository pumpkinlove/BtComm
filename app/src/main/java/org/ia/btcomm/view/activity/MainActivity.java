package org.ia.btcomm.view.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.ia.btcomm.R;
import org.ia.btcomm.adapter.BtDeviceListAdapter;
import org.ia.btcomm.bean.BTDevice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_discovery)
    Button btnDiscovery;
    @BindView(R.id.lv_bt_device)
    ListView lvBtDevice;
    private List<BTDevice> deviceList;
    private BluetoothReceiver receiver;

    private BtDeviceListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initData();
        initView();
        initBtReceiver();
        startSearch();
    }

    void initData() {
        deviceList = new ArrayList<>();
        listAdapter = new BtDeviceListAdapter(deviceList, this);
    }

    void initView() {
        lvBtDevice.setAdapter(listAdapter);
    }

    void initBtReceiver() {
        receiver = new BluetoothReceiver();
        IntentFilter ifi = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ifi.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        ifi.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, ifi);
    }

    @OnClick(R.id.btn_discovery)
    void startSearch() {
        deviceList.clear();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) {
            adapter.enable();
        } else {
            adapter.startDiscovery();
        }

    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                BTDevice btDevice = new BTDevice(device);
                btDevice.setRssi(rssi);
                deviceList.add(btDevice);
                listAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                btnDiscovery.setEnabled(false);
                btnDiscovery.setText("正在搜索...");
                btnDiscovery.setTextColor(getResources().getColor(R.color.blue_band_dark3));
                searchBondConnectedDevice();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                btnDiscovery.setEnabled(true);
                btnDiscovery.setText("搜索");
                btnDiscovery.setTextColor(getResources().getColor(R.color.blue_band_dark));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @OnItemClick(R.id.lv_bt_device)
    void onDeviceClick(AdapterView<?> parent, View view, int position, long id) {
        connect(deviceList.get(position));
    }

    void connect(BTDevice device) {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        Intent i = new Intent(this, CommActivity.class);
        i.putExtra("device", device);
        startActivity(i);
    }

    private void searchBondConnectedDevice() {
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {//得到连接状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(BluetoothAdapter.getDefaultAdapter(), (Object[]) null);
            Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
            for (BluetoothDevice device : devices) {
                BTDevice btDevice = new BTDevice(device);
                if (state == BluetoothAdapter.STATE_CONNECTED) {
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if (isConnected) {
                        btDevice.setStatus(BTDevice.CONNECTED);
                    } else {
                        btDevice.setStatus(device.getBondState());
                    }
                } else {
                    btDevice.setStatus(device.getBondState());
                }
                deviceList.add(btDevice);
                listAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
