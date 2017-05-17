package usbprinter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.xkdx.serial_test.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import usb.USBPort;

public class UsbDeviceList extends AppCompatActivity {
    private ArrayAdapter<String> deviceArrayAdapter;
    private ListView mFoundDevicesListView;
    private Button scanButton, backButton;
    private List<UsbDevice> deviceList;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_device_list);
        setTitle("请选择要连接的设备");
        mContext = this;
        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDiscovery();
            }
        });

        backButton = (Button) findViewById(R.id.button_bace);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        deviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_device_item);
        mFoundDevicesListView = (ListView) findViewById(R.id.paired_devices);
        mFoundDevicesListView.setAdapter(deviceArrayAdapter);
        mFoundDevicesListView.setOnItemClickListener(mDeviceClickListerner);
        doDiscovery();
    }

    //发现设备
    private void doDiscovery() {
        deviceArrayAdapter.clear();
        UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        deviceList = new ArrayList<>();
        for (UsbDevice device : devices.values()) {
            if (USBPort.isUSBPrinter(device)) {
                deviceArrayAdapter.add(device.getDeviceName() + "\nvid: "
                        + device.getVendorId() + " pid: "
                        + device.getProductId());
                deviceList.add(device);
            }
        }
    }

    private void returnToPreviousActivity(UsbDevice device) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable(UsbManager.EXTRA_DEVICE, device);
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private AdapterView.OnItemClickListener mDeviceClickListerner = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            returnToPreviousActivity(deviceList.get(i));

        }
    };
}
