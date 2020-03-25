package com.study.bluetooth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.study.bluetooth.BTConnect.BTConnectThread;
import com.study.bluetooth.BTConnect.MessageAdapter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙数据交互界面
 */
public class MessageActivity extends AppCompatActivity {

    protected static BluetoothDevice device;//接收用户指定的蓝牙设备

    private BTConnectThread mBTConnectThread;

    //***************蓝牙设备UI列表*************//
    private ListView mListView;
    private MessageAdapter mMsgAdapter;
    private List<String> mMsgList = new ArrayList<>();
    //*****************************************//

    private EditText mEditText;
    private String message;//从获取EditText获得的消息数据
    private Button mButton;//发送按键

    private MsgHandler msgHandler = new MsgHandler();//异步消息处理器

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coneccted);
        initUI();
        mBTConnectThread = new BTConnectThread(device, msgHandler);//蓝牙连接、数据通信
        mBTConnectThread.start();
    }

    private void initUI() {
        mListView = findViewById(R.id.msg_list);
        mMsgAdapter = new MessageAdapter(mMsgList, this);
        mListView.setAdapter(mMsgAdapter);
        mEditText = findViewById(R.id.output_text);
        mButton = findViewById(R.id.send);
        //点击按键发送的操作
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( (message = mEditText.getText().toString()) != null ) {
                    if ( mBTConnectThread != null ) {
                        try {
                            mBTConnectThread.sendData(message.getBytes("utf-8"));//发送蓝牙数据
                            mMsgList.add("me: " + message);//显示到界面上
                            mMsgAdapter.notifyDataSetChanged();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            Log.d("BlueTooth", "字符转换错误");
                        }
                    }
                }
            }
        });
    }

    /**
     * 异步消息处理器，用于子线程对主线程的回调
     */
    private class MsgHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.BT_GET_DATA:
                    mMsgList.add(device.getName() + ": " + msg.obj);
                    mMsgAdapter.notifyDataSetChanged();
                    break;
                case Constant.CONNECT_SUCCESSD:
                    showToast("创建Socket连接成功");
                    break;
                case Constant.CONNECT_ERROR:
                    showToast("创建Socket连接失败");
                    break;
                case Constant.MSG_START_LISTENING:
                    showToast("开始监听");
                    break;
            }
        }
    }

    /**
     * 设置toast的标准格式
     * @param text: 要显示的字符串
     */
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

    /**
     * 活动销毁时，自动关闭BTSocket连接
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBTConnectThread.cancel();
    }
}
