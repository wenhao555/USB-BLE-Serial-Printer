package com.xkdx.serial_test;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android_serialport_api.SerialPort;

/**
 * Created by Administrator on 2017/1/18.
 */

public class PrinterUtil {
    private OutputStream mOutputStream;
    private String charsetName = "gbk";
    private SerialPort mSerialPort;
    private InputStream mInputStream;

    public void OpenPrinterSial(String path) {
        try {
            mSerialPort = new SerialPort(new File("/dev/" + path), 9600, 0);
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();
//                    receiveThread();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("test", "打开失败");
            e.printStackTrace();
        }
    }

    private void sendBytesData(byte[] srcData) {
        try {
            mOutputStream.write(srcData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printText(String content) {
        byte[] data = null;
        if (this.charsetName != "") {
            try {
                data = content.getBytes(this.charsetName);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {

            data = content.getBytes();

        }
        this.sendBytesData(data);
    }
}
