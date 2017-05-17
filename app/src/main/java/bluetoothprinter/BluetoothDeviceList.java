package bluetoothprinter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xkdx.serial_test.R;

import java.util.Set;

public class BluetoothDeviceList extends AppCompatActivity {
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_RE_PAIR = "re_pair";
    public static String EXTRA_DEVICE_NAME = "device_name";

    private Context mContext;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ListView pairedListView;
    private Button scanButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device_list);
        setTitle("请选择要连接的设备");
        mContext = this;
        setResult(Activity.RESULT_CANCELED);
        initView();
    }

    private void initView() {
        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDiscovery();
                view.setEnabled(false);
            }
        });
        backButton = (Button) findViewById(R.id.button_bace);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_device_item);
        pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();//set集合中不允许重复的出现
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "( "
                        + getResources().getText(R.string.has) + " )"
                        + "\n" + device.getAddress());
            }
        }
    }

    @Override
    protected void onStop() {//停止
        if (mBtAdapter != null && mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {//恢复
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        super.onResume();
    }

    private void doDiscovery() {//发现
        setSupportProgressBarIndeterminateVisibility(true);
        setTitle("扫描中...");
        if (mBtAdapter.isDiscovering()) {//当在搜索设备的时候
            mBtAdapter.cancelDiscovery();//取消搜索设备
        }
        mPairedDevicesArrayAdapter.clear();//清理适配器
        mBtAdapter.startDiscovery();//开始搜索设备
    }

    private void returnToPreviousActivity(String address, boolean re_pair, String name) {//返回主界面的设备
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
        intent.putExtra(EXTRA_RE_PAIR, re_pair);
        intent.putExtra(EXTRA_DEVICE_NAME, name);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            String name = info.substring(0, info.length() - 17);
            returnToPreviousActivity(address, false, name);
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//额外的设备
                String itemName = device.getName()
                        + "( "
                        + getResources()
                        .getText(
                                device.getBondState() == BluetoothDevice.BOND_BONDED ? R.string.has//结合
                                        : R.string.not_paired) + " )"
                        + "\n" + device.getAddress();
                Log.i("新设备", device.getName() + "\n" + device.getAddress());
                mPairedDevicesArrayAdapter.remove(itemName);
                mPairedDevicesArrayAdapter.add(itemName);
                pairedListView.setEnabled(true);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setSupportProgressBarIndeterminateVisibility(false);
                setTitle("请选择要连接的设备");
                if (mPairedDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mPairedDevicesArrayAdapter.add(noDevices);
                    pairedListView.setEnabled(false);
                }
                scanButton.setEnabled(true);
            }
        }
    };

    private void showToast(String string) {
        Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show();
    }
}
