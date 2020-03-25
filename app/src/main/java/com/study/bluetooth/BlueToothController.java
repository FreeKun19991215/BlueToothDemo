package com.study.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙控制器
 */
public class BlueToothController {

    private BluetoothAdapter mbtAdapter;

    public BlueToothController() {
        mbtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothAdapter getAdapter() {
        return mbtAdapter;
    }

    /**
     * 打开蓝牙
     */
    public void openBlueTooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 关闭蓝牙
     */
    public void stopBlueTooth() {
        mbtAdapter.disable();
    }

    /**
     * 获得当前蓝牙状态
     * @return  true:打开状态   false:关闭状态
     */
    public boolean getBlueToothState() {
        return ( mbtAdapter.getState() == BluetoothAdapter.STATE_ON );
    }

    /**
     * 搜索蓝牙设备
     */
    public void findBlueTooth() {
        if ( mbtAdapter.isDiscovering() ) {
            mbtAdapter.cancelDiscovery();
        }
            mbtAdapter.startDiscovery();
            Log.d("BlueTooth", "搜索蓝牙设备");
    }

    /**
     * 获取已绑定设备
     */
    public List<BluetoothDevice> getBondedDeviceList(){
        return new ArrayList<>(mbtAdapter.getBondedDevices());
    }

}
