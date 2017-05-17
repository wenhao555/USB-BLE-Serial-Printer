package com.xkdx.serial_test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class SerialList extends AppCompatActivity {
    private static final String TAG = "查找串口列表";
    private ListView serial_devices;
    private Button button_serial, button_return;
    private EditText serial_edit;
    private String[] entries;//设备数组
    private String[] entryValues;
    private ArrayAdapter<String> deviceArrayAdapter;//设备适配器
    private String baudrateStr;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_list);

        setTitle("查找串口设备");
        init();
    }

    private void init() {
        deviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_device_item);
        serial_devices = (ListView) findViewById(R.id.serial_devices);
        button_serial = (Button) findViewById(R.id.button_serial);
        button_serial.setOnClickListener(listener);
        button_return = (Button) findViewById(R.id.button_return);
        button_return.setOnClickListener(listener);
        serial_edit = (EditText) findViewById(R.id.serial_edit);
        serial_devices.setAdapter(deviceArrayAdapter);
        serial_devices.setOnItemClickListener(onItemClickListener);
        doDiscovery();
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_serial:
                    doDiscovery();//遍历新设备
                    break;
                case R.id.button_return:
                    finish();
                    break;
            }
        }
    };

    private void doDiscovery() {
        Finder mSerialPortFinder = new Finder();//查找串口设备
        entries = mSerialPortFinder.getAllDevices();//得到所有的设备
        entryValues = mSerialPortFinder.getAllDevicesPath();//得到所有设备的路径
        for (int i = 0; i < entries.length; i++) {//遍历设备
            deviceArrayAdapter.add(entries[i] + "\n" + "path:" + entryValues[i]);
        }
    }

    private void returnActivity(String path, String baudrate) {
        Intent intent = new Intent();
        intent.putExtra("path", path);//传递串口的路径
        intent.putExtra("baudrate", baudrate);//传递串口的波特率
        setResult(RESULT_OK, intent);
        finish();
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            baudrateStr = serial_edit.getText().toString();//得到输入的串口号
            path = entryValues[i];
            returnActivity(path, baudrateStr);//传递返回值

        }
    };
}
