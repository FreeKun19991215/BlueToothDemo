package com.study.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *        蓝牙HC-05与手机通信
 * 1.蓝牙HC-05发送数据给手机，显示在界面中。
 * 2.手机发送数据给蓝牙，显示在界面中。
 *
 * Create by freeKun on 2020/3/22
 */
public class MainActivity extends AppCompatActivity {

    List<String>  mPermissionList = new ArrayList<>();//需要请求的权限列表

    private BlueToothController mbtController = new BlueToothController();//蓝牙控制器：操作蓝牙各功能

    //***************蓝牙设备UI列表*************//
    private ListView mbtListView;
    private DeviceAdapter mbtAdapter;
    private List<BluetoothDevice> mbtDeviceList = new ArrayList<>();
    private List<BluetoothDevice> mbtBondedDeviceList = new ArrayList<>();
    //************************************//

    private static Toast mToast;//标准化Toast消息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        requestPermissions();//请求权限
        registerBlueToothBroadcast();//注册广播
    }

    private void initUI() {
        mbtListView = findViewById(R.id.device_list);
        mbtAdapter = new DeviceAdapter(mbtDeviceList, this);
        mbtListView.setAdapter(mbtAdapter);
        mbtListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = (BluetoothDevice) mbtAdapter.getItem(position);
                int state = device.getBondState();
                if ( state == BluetoothDevice.BOND_BONDED ) {//如果选中的蓝牙设备是绑定状态的，即连接蓝牙设备进行通信
                    mbtController.getAdapter().cancelDiscovery();
                    Log.d("BlueTooth", "跳转页面");
                    Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                    MessageActivity.device = device;//将选中的蓝牙设备传给MessageActivity
                    startActivity(intent);
                }else if ( state == BluetoothDevice.BOND_NONE ) {//如果选中的蓝牙设备是未绑定状态的，即绑定蓝牙设备
                    device.createBond();//绑定蓝牙设备
                    mbtDeviceList.remove(device);//从未绑定蓝牙设备列表中移除已绑定蓝牙设备
                    mbtAdapter.notifyDataSetChanged();
                }else if ( state == BluetoothDevice.BOND_BONDING ) {//如果选中的蓝牙设备是正在绑定状态的，即显示出来
                    showToast("蓝牙设备正在绑定");//一般不会显示
                }
            }
        });
    }

    /**
     *请求权限
     */
    private void requestPermissions() {

        mPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        mPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if ( ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
        }else {
            mPermissionList.remove(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if ( ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 2);
        }else {
            mPermissionList.remove(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if ( mPermissionList.isEmpty() ) {
            showToast("已获得全部所需权限");
        }else {
            showToast("未获得全部所需权限，无法搜索到为匹配蓝牙");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    mPermissionList.remove(Manifest.permission.ACCESS_COARSE_LOCATION);
                }
            case 2:
                if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    mPermissionList.remove(Manifest.permission.ACCESS_FINE_LOCATION);
                }
        }
    }

    private void registerBlueToothBroadcast() {

        IntentFilter mBTFilter = new IntentFilter();

        //蓝牙状态改变
        mBTFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //开始查找
        mBTFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //结束查找
        mBTFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //查找设备
        mBTFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //绑定状态改变
        mBTFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(mBTReceiver, mBTFilter);
    }

    private BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if ( BluetoothAdapter.ACTION_STATE_CHANGED.equals(action) ) {//蓝牙关闭

                if ( mbtController.getBlueToothState() == false ) {
                    showToast("蓝牙关闭");
                    mbtDeviceList.clear();
                    mbtBondedDeviceList.clear();
                    mbtAdapter.notifyDataSetChanged();
                }
            }else if ( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action) ) {//开始搜索
                showToast("开始搜索");
                mbtDeviceList.clear();
                mbtBondedDeviceList.clear();
                mbtAdapter.notifyDataSetChanged();
            }else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) ) {//120秒搜索结束
                showToast("搜索结束");
            } else if ( BluetoothDevice.ACTION_FOUND.equals(action) ) {//每找到一个蓝牙设备，添加进ListView
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {//如果是已绑定设备，添加进mbtBondedDeviceList列表
                    if ( mbtBondedDeviceList.contains(device) == false ) {
                        mbtBondedDeviceList.add(device);
                    }
                } else {
                    if ( mbtDeviceList.contains(device) == false ) {//如果是未绑定设备，添加进mbtDeviceList列表
                        mbtDeviceList.add(device);
                    }
                }
                mbtAdapter.notifyDataSetChanged();
            }else if ( BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action) ) {//绑定成功及解绑成功
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if ( device.getBondState() == BluetoothDevice.BOND_BONDED ) {
                    showToast("绑定成功，请点击查看绑定设备查看");
                }else if ( device.getBondState() == BluetoothDevice.BOND_NONE ) {
                    showToast("解绑成功");//官方SDK中未给出解绑蓝牙设备API，不建议饭射源码的内部类
                }
            }
        }
    };

    /**
     * 打开蓝牙回调结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode == RESULT_OK) {
            showToast("打开成功");
        }else {
            showToast("打开失败");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //获取被点击的item的id
        int id = item.getItemId();

        if ( id == R.id.bt_open ) {//打开蓝牙
            mbtController.openBlueTooth(this, Constant.BT_OPEN);
        } else if ( id == R.id.bt_stop ) {//关闭蓝牙
            mbtController.stopBlueTooth();
        } else if ( id == R.id.bt_find ) {//搜索蓝牙设备
            mbtAdapter.setAdapterData(mbtDeviceList);
            mbtController.findBlueTooth();
        }else if ( id == R.id.bt_find_bonded ) {//查看绑定设备
            mbtAdapter.setAdapterData(mbtController.getBondedDeviceList());
        }

        return super.onOptionsItemSelected(item);
    }

    //设置toast的标准格式
    private void showToast(String text){
        if(mToast == null){
            mToast = Toast.makeText(this, text,Toast.LENGTH_SHORT);
            mToast.show();
        }
        else {
            mToast.setText(text);
            mToast.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //never forget close BroadcastReceiver
        unregisterReceiver(mBTReceiver);
    }

}
