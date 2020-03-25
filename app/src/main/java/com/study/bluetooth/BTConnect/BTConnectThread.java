package com.study.bluetooth.BTConnect;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.study.bluetooth.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


/**
 * 1.连接蓝牙
 * 2.处理蓝牙数据
 */
public class BTConnectThread extends Thread {

    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECT_UUID);//获得蓝牙设备的Socket对象必要的UUID标志

    private BluetoothSocket mBTSocket;

    private InputStream mBTInput;
    private OutputStream mBTOutput;

    private Handler msgHandler;//异步消息处理器

    /**
     * 接受指定蓝牙设备和MessageActivity的异步消息处理器(Handler),
     * 并获得Socket对象
     * @param device 蓝牙设备
     * @param handler 异步核心处理器
     */
    public BTConnectThread(BluetoothDevice device, Handler handler) {

        msgHandler = handler;

        /**
         * 通过UUID获得Socket对象，
         * 用于连接蓝牙设备
         */
        try {
            mBTSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            Log.d("BlueTooth", "createSocket");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("BlueTooth", "createSocketError");
        }

    }

    /**
     * 开启两个线程分别处理Socket连接和接受的异步操作，
     * 并发出各种消息给MessageActivity的异步消息处理器处理。
     */
    @Override
    public void run() {

        /**
         * 创建一个Socket连接，
         * 用于获取InputStream对象
         */
        try {
            mBTSocket.connect();
            msgHandler.sendEmptyMessage(Constant.CONNECT_SUCCESSD);
            Log.d("BlueTooth", "connected");
        } catch (IOException e) {
            Log.d("BlueTooth", "connectError");
            e.printStackTrace();
            msgHandler.sendMessage(msgHandler.obtainMessage(Constant.CONNECT_ERROR));
            try {
                mBTSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }

        if (mBTSocket != null) {

            /**
             * 开启一个线程，处理异步数据接收操作
             */
            new Thread(new Runnable() {
                @Override
                public void run() {

                    /**
                     * 获取InputStream对象
                     */
                    try {
                        mBTInput = mBTSocket.getInputStream();
                        mBTOutput = mBTSocket.getOutputStream();
                        Log.d("BlueTooth", "获取InputStream&OutputStream对象成功");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("BlueTooth", "获取InputStream&OutputStream对象失败");
                    }

                    byte[] buffer = new byte[1024];  // 用于流的缓冲存储
                    int bytes; // 从read()返回bytes

                    msgHandler.sendEmptyMessage(Constant.MSG_START_LISTENING);//开始监听

                    /**
                     * 持续监听InputStream，
                     * 直到出现异常，
                     *并读取数据发送给MessageActivity的异步核心处理器(Handler)。
                     */
                    while (true) {
                        Log.d("BlueTooth", "startRead");
                        try {
                            bytes = mBTInput.read(buffer);// 从InputStream读取数据
                            if (bytes > 0) {
                                msgHandler.sendMessage(msgHandler.obtainMessage(Constant.BT_GET_DATA, new String(buffer, 0, bytes, "utf-8")));// 将获得的bytes消息发送到UI层(主线程activity）
                            }
                            Log.d("BlueTooth", "message size" + bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("BlueTooth", "readError");
                            break;
                        }
                    }
                }
            }).start();

        }

    }

    /**
     * 向目标蓝牙设备发送数据
     * @param bytes 发送的数据
     */
    public void sendData(byte[] bytes) {
        try {
            mBTOutput.write(bytes);
            Log.d("BlueTooth", "send " + bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭Socket连接
     */
    public void cancel() {
        try {
            mBTSocket.close();
            Log.d("BlueTooth", "socketClose");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
