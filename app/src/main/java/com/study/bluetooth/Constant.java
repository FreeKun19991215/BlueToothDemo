package com.study.bluetooth;

/**
 * 用于定义各种常量
 */
public class Constant {

    public static final int BT_OPEN = 1;//用于打开蓝牙的请求码，用于intent结果的回调函数

    public static final int CONNECT_ERROR = 2;//用于连接蓝牙失败时，回调给Handler的引索

    public static final int CONNECT_SUCCESSD = 4;//用于连接蓝牙成功时，回调给Handler的引索

    public static final String CONNECT_UUID = "00001101-0000-1000-8000-00805F9B34FB";//获取蓝牙Socket对象必要的UUID码

    public static final int BT_GET_DATA = 3;//用于每当接收到蓝牙数据时，回调给Handler的引索

    public static final int MSG_START_LISTENING = 5; //用于开始监听蓝牙发送的数据时，回调给Handler的引索

}
