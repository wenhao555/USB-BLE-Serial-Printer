package com.xkdx.serial_test;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class MyService extends Service {

    private SerialPort mSerialPort;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    private String prot = "ttyS2";
    private int baudrate = 9600;
    private static int number = 0;
    private int i = 0;
    public static final String DATABASE_NAME = "User_Flow";
    private Thread receiveThread;
    private StringBuilder sb;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBind();
    }

    public class MyBind extends Binder {
        public String getMyService() {
            return getNumString();
        }
    }

    public String getNumString() {
        return sb.toString();
    }

    @Override
    public void onCreate() {
        sb = new StringBuilder();
        sb.append("正在接受出口信息：");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        OpenSerial();
//        SendDate();
        return super.onStartCommand(intent, flags, startId);
    }


    private void OpenSerial() {
        // 打开
        try {
            mSerialPort = new SerialPort(new File("/dev/" + prot), baudrate,
                    0);
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();
            receiveThread();
//            mPrinter = PrinterInstance_One.getPrinterInstance(new File("/dev/" + prot), baudrate, 0, new Handler());
//            mPrinter.openConnection();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("atuan", "打开失败");
            e.printStackTrace();
        }
    }


    private void receiveThread() {
        // 接收
        receiveThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    int size;
                    try {
                        byte[] buffer = new byte[1024];
                        if (mInputStream == null)
                            return;
                        size = mInputStream.read(buffer);
                        if (size > 0) {
                            String recinfo = new String(buffer, 0,
                                    size);
                            Log.i("atuan", "接收到串口信息:" + recinfo);
                            sb.append(recinfo).append(",");
                            if ("1".equals(recinfo)) {
                                number = number
                                        + Integer.parseInt(recinfo);
//                                PreferenceSettings.setUserNumber(getApplicationContext(), DATABASE_NAME, number);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        receiveThread.start();
    }

    private void SendDate() {
        // 发送
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        mOutputStream.write(("1").getBytes());
                        Log.i("atuan", "发送成功:1" + i);
                        Thread.sleep(1000);
                        i += 1;
                    } catch (Exception e) {
                        Log.i("atuan", "发送失败");
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        if (mSerialPort != null) {
            mSerialPort.close();
        }
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!receiveThread.isInterrupted()) {
            receiveThread.stop();
        }
    }
}
