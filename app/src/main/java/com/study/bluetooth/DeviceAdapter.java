package com.study.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private List<BluetoothDevice> mData;
    private Context mContext;

    public DeviceAdapter(List<BluetoothDevice> data, Context context){
        mData = data;
        mContext = context.getApplicationContext();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        //复用view，优化性能
        if(view == null){
            view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent,false);
        }

        TextView line1 = view.findViewById(android.R.id.text1);
        TextView line2 = view.findViewById(android.R.id.text2);

        //获取对应的蓝牙设备
        BluetoothDevice device = (BluetoothDevice) getItem(position);

        //显示设备名称
        line1.setText(device.getName());
        //显示设备地址
        line2.setText(device.getAddress());

        return view;
    }

    /**
     * 设置适配器关联数据
     * @param data
     */
    public void setAdapterData(List<BluetoothDevice> data) {
        mData = data;
        notifyDataSetChanged();
    }

}
