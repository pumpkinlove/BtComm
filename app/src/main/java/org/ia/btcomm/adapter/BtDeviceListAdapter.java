package org.ia.btcomm.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.ia.btcomm.R;
import org.ia.btcomm.bean.BTDevice;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xu.nan on 2017/9/19.
 */

public class BtDeviceListAdapter extends BaseAdapter {

    private List<BTDevice> deviceList;
    private Context mContext;

    public BtDeviceListAdapter(List<BTDevice> deviceList, Context mContext) {
        this.deviceList = deviceList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        if (deviceList != null) {
            return deviceList.size();
        }
        return 0;
    }

    @Override
    public BTDevice getItem(int position) {
        if (deviceList != null) {
            return deviceList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_bt_device, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        BTDevice btDevice = deviceList.get(position);
        holder.tvDeviceName.setText(btDevice.getDevice().getName());
        holder.tvDeviceMac.setText(btDevice.getDevice().getAddress());
        holder.tvDeviceRssi.setText(String.format("%s", btDevice.getRssi()));
        switch (btDevice.getStatus()) {
            case BluetoothDevice.BOND_BONDED:
                holder.tvDeviceStatus.setText("已配对");
                holder.tvDeviceStatus.setTextColor(mContext.getResources().getColor(R.color.gold_dark));
                break;
            case BluetoothDevice.BOND_BONDING:
                holder.tvDeviceStatus.setText("正在配对");
                holder.tvDeviceStatus.setTextColor(mContext.getResources().getColor(R.color.blue_band_dark3));
                break;
            case BluetoothDevice.BOND_NONE:
                holder.tvDeviceStatus.setText("未配对");
                holder.tvDeviceStatus.setTextColor(mContext.getResources().getColor(R.color.blue_band_dark3));
                break;
            case BTDevice.CONNECTED:
                holder.tvDeviceStatus.setText("已连接");
                holder.tvDeviceStatus.setTextColor(mContext.getResources().getColor(R.color.green_dark));
                break;
            default:
                holder.tvDeviceStatus.setText("未配对");
                holder.tvDeviceStatus.setTextColor(mContext.getResources().getColor(R.color.blue_band_dark3));
                break;
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_device_name)
        TextView tvDeviceName;
        @BindView(R.id.tv_device_rssi)
        TextView tvDeviceRssi;
        @BindView(R.id.tv_device_status)
        TextView tvDeviceStatus;
        @BindView(R.id.tv_device_mac)
        TextView tvDeviceMac;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
