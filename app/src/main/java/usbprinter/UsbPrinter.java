package usbprinter;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usbinstance.PrinterConstants;
import com.example.usbinstance.PrinterInstance_One;
import com.example.usbinstance.TextType;
import com.xkdx.serial_test.Barcode;
import com.xkdx.serial_test.FileChooser;
import com.xkdx.serial_test.QRcode;
import com.xkdx.serial_test.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.PrefUtils;
import bluetoothprinter.BluetoothDeviceList;
import monochrome.BitmapConvertor;
import usb.USBPort;

import monochrome.BitmapConvertor;

import static android.R.attr.data;

public class UsbPrinter extends AppCompatActivity {
    private static final String TAG = "主界面";
    private TextView name, address, zhijiance, working, huanchong, quzhi, zhijiangjin, jitou, zhuangtai;
    public static boolean isConnected = false;
    private Button Usb_connect, Bth_connect;
    private Context mContext;
    private static final String ACTION_USB_PERMISSION = "com.android.usb.USB_PERMISSION";
    private static final int CONNECT_DEVICE_USB = 1;
    private static final int CONNECT_DEVICE_BTH = 2;
    private List<UsbDevice> deviceList;
    private static UsbDevice mUSBDevice;
    private static PrinterInstance_One myPrinter;
    private ProgressDialog dialog, dialogUpdate;
    private IntentFilter bluDisconnectFilter;
    private BluetoothAdapter mBtAdapter;
    private static String devicesName = "未知设备";
    private static String devicesAddress;
    private static BluetoothDevice mDevice;
    private static boolean hasRegDisconnectReceiver = false;
    private static boolean isFirst = true;
    private EditText mEdit;
    private GridView mGridView;
    private String test[] = {"普通打印", "状态", "打印二维码", "一维码/条码打印", "位置及旋转", "设置字体添加上/下划线", "打印图片", "回车换行", "打印进纸n行", "标签模式", "直线", "虚线", "国际字符集", "切刀", "查找文件", "DNW传输文件", "打印文件", "测试页"};
    private Button button;
    private Bitmap bitmap;
    private BitmapConvertor convertor;//转换图片
    private byte b;
    private int k;//左中右
    private int j;//旋转
    private int t;//上/下对齐
    private int h;
    private Intent intent;
    private static boolean isLine = false;
    private int isisLine = 0;
    public static boolean isChoose = false;
    private static final int REQUEST_CODE = 3;
    public static final String EXTRA_FILE_CHOOSER = "file_chooser";
    String FilePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_printer);
        Update();
        convertor = new BitmapConvertor(this);
        setTitle("荣达打印机测试");
        mContext = this;
        init();

        isFirst = false;
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        myPrinter = PrinterInstance_One.mPrinter;
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void Update() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    //缩放图片
    private Bitmap bit() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.my_monochrome_image);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = 380;
        int newHeight = 250;
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBip = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newBip;
    }

    //图片转换成黑白
    public Bitmap convertToBlackWhite() {

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.icbc);
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组

        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                //分离三原色
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                //转化成灰度像素
                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;


            }
        }
        //新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        //设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);

        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width, height);
        return resizeBmp;
    }

    //Handler收发消息
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PrinterConstants.Connect.SUCCESS:
                    isConnected = true;
                    bluDisconnectFilter = new IntentFilter();
                    bluDisconnectFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                    mContext.registerReceiver(myReceiver, bluDisconnectFilter);
                    hasRegDisconnectReceiver = true;
                    showToast("连接成功");
                    break;
                case PrinterConstants.Connect.FAILED:
                    isConnected = false;
                    showToast("连接失败");
                    break;
                case PrinterConstants.Connect.CLOSED:
                    isConnected = false;
                    showToast("连接关闭");
                    break;
                case PrinterConstants.Connect.NODEVICE:
                    isConnected = false;
                    showToast("没有连接");
                    break;
            }
            updateButtonState(isConnected);
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }

        }
    };


    public static String readFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        is.close();
        String result = baos.toString();
        baos.close();
        return result;
    }


    //改变按钮及机器文字
    private void updateButtonState(boolean isConnected) {//当有连接的时候
        if (isConnected) {
            name.setText(getString(R.string.printerName).split(":")[0]
                    + ": " + devicesName);
            address.setText(getString(R.string.printerAddress)
                    .split(":")[0] + ": " + devicesAddress);
        } else if (!isConnected || mDevice == null || mUSBDevice == null) {
            name.setText(getString(R.string.printerName));
            address.setText(getString(R.string.printerAddress));
        }
    }

    //初始化控件
    private void init() {
        zhijiance = (TextView) findViewById(R.id.zhijiance);
        zhuangtai = (TextView) findViewById(R.id.zhuangtai);
        working = (TextView) findViewById(R.id.working);
        huanchong = (TextView) findViewById(R.id.huanchong);
        quzhi = (TextView) findViewById(R.id.quzhi);
        zhijiangjin = (TextView) findViewById(R.id.zhijiangjin);
        jitou = (TextView) findViewById(R.id.jitou);

        mEdit = (EditText) findViewById(R.id.mEdit);
        mGridView = (GridView) findViewById(R.id.mGridView);
        mGridView.setAdapter(new MyAdapter());
        mGridView.setOnItemClickListener(Onlistener);
        name = (TextView) findViewById(R.id.name);
        address = (TextView) findViewById(R.id.address);
        Usb_connect = (Button) findViewById(R.id.Usb_connect);
        Usb_connect.setOnClickListener(listener);
        Bth_connect = (Button) findViewById(R.id.Bth_connect);
        Bth_connect.setOnClickListener(listener);
        dialog = new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("连接中....");
        dialog.setMessage("请等待...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        updateButtonState(isConnected);
        dialogUpdate = new ProgressDialog(mContext);
        dialogUpdate.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialogUpdate.setCancelable(false);
    }


    private AdapterView.OnItemClickListener Onlistener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (PrinterInstance_One.mPrinter != null) {
                switch (i) {
                    case 0://打印文字
                        edText();
                        break;
                    case 1://获得
                        states();
                        break;
                    case 2://打印二维码
                        intent = new Intent(mContext, QRcode.class);
                        startActivity(intent);
                        break;
                    case 3:// 打印一维码
                        intent = new Intent(mContext, Barcode.class);
                        startActivity(intent);
                        break;
                    case 4://位置及旋转
                        isRoate();
                        break;
                    case 5://设置文字属性
                        setFont();
                        break;
                    case 6://打印图片     注：图片需要转换
                        try {
                            Bitmap bitmap123 = BitmapFactory.decodeResource(getResources(), R.mipmap.icbc);
                            int width = bitmap123.getWidth();
                            int height = bitmap123.getHeight();
                            int newWidth = 380;
                            int newHeight = 250;
                            float scaleWidth = ((float) newWidth) / width;
                            float scaleHeight = ((float) newHeight) / height;
                            Matrix matrix = new Matrix();
                            matrix.postScale(scaleWidth, scaleHeight);
                            Bitmap newBip = Bitmap.createBitmap(bitmap123, 0, 0, width, height, matrix, true);
                            Bitmap BorWBitmap = convertor.convertBitmap(newBip);//转换图片
                            myPrinter.setPricture(BorWBitmap);
//                            myPrinter.setPricture(bit());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 7://回车
                        myPrinter.setCarriage();
                        break;
                    case 8://打印进纸
                        mFeed();
                        break;
                    case 9://标签
                        isLine = true;
                        edText();
                        myPrinter.Label(isLine);
                        showToast("进入标签模式");
                        break;
                    case 10://打印直线
                        myPrinter.setisLine();
                        break;
                    case 11://打印直线/虚线
                        myPrinter.setisnoLine();
                        break;
                    case 12://国际字符集
                        international();
                        break;
                    case 13://切刀
                        myPrinter.cutPaper();
                        break;
                    case 14://查找文件
                        Intent fileChooserIntent = new Intent(mContext, FileChooser.class);
                        startActivityForResult(fileChooserIntent, REQUEST_CODE);
                        break;
                    case 15:
                        if (FilePath != null) {
                            myPrinter.readFile(FilePath);
                        } else {
                            showToast("没有文件");
                        }
                        break;
                    case 16:
                        if (FilePath != null) {
                            myPrinter.sendFileData(FilePath);
                        } else {
                            showToast("没有文件");
                        }

                        break;
                    case 17:
                        isLine = true;
                        String s = "\n" + "肋排" + "\n" +
                                "打印时间：2017-3-30 11：30" + "\n" +
                                "单价：33.96/KG" + "\n" +
                                "重量：1.768KG" + "\n" +
                                "售价：￥60.04元";
                        myPrinter.printText(s);
                        try {
                            String as = new String(s.getBytes("utf-8"), "ISO-8859-1");
                            myPrinter.setQrCode(1, 2, 1, 1, as);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        myPrinter.Label(isLine);
                        break;
                }
            } else {
                showToast("请先连接打印机");
            }
        }
    };


    //    文字打印
    private void edText() {
        String content = mEdit.getText().toString();
        if (!content.equals("")) {
            myPrinter.printText(content + "\n");
        } else {
            showToast("请输入文字");
        }
    }

    //获得
    private void states() {
        int len = (int) myPrinter.getPrintStates();
//        if ((len & 0x20) == 0x20) {
//            jitou.setText("机头：机头正常");
//        } else {
//            jitou.setText("机头：机头打开");
//        }
//        if ((len & 0x10) == 0x10) {
//            zhijiangjin.setText("纸将尽检测器：纸将尽传感器有纸");
//        } else {
//            zhijiangjin.setText("纸将尽检测器：纸将尽");
//        }
//
        if ((len & 0x08) == 0x08) {
            zhuangtai.setText("打印机状态：错误");
        } else {
            zhuangtai.setText("打印机状态：正常");
        }
        if ((len & 0x04) == 0x04) {
            huanchong.setText("接收缓冲区：打印缓存区满");
        } else {
            huanchong.setText("接收缓冲区：打印缓存区未满");
        }
        if ((len & 0x02) == 0x02) {
            working.setText("工作：打印机正在打印");
        } else {
            working.setText("工作：打印机空闲");
        }
        if ((len & 0x01) == 0x01) {
            zhijiance.setText("纸检测器：打印机有纸");
        } else {
            zhijiance.setText("纸检测器：打印机无纸");
        }
    }

    //国际字符集
    private void international() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("设置国际字符集");
        View internat = getLayoutInflater().inflate(R.layout.activity_international, null);
        RadioGroup inter = (RadioGroup) internat.findViewById(R.id.inter);
        int country = PrefUtils.getInt(mContext, "country", 15);
        switch (country) {
            case 15:
                inter.check(R.id.China);
                break;
            case 0:
                inter.check(R.id.USA);
                break;
            case 1:
                inter.check(R.id.France);
                break;
            case 2:
                inter.check(R.id.Germany);
                break;
            case 3:
                inter.check(R.id.UK);
                break;
            case 4:
                inter.check(R.id.Denmark);
                break;
            case 5:
                inter.check(R.id.Sweden);
                break;
            case 6:
                inter.check(R.id.Italy);
                break;
            case 7:
                inter.check(R.id.SpainI);
                break;
            case 8:
                inter.check(R.id.Japan);
                break;
            case 9:
                inter.check(R.id.Norway);
                break;
            case 10:
                inter.check(R.id.DenmarkII);
                break;
            case 11:
                inter.check(R.id.SpainII);
                break;
            case 12:
                inter.check(R.id.Latin);
                break;
            case 13:
                inter.check(R.id.Korea);
                break;
            case 14:
                inter.check(R.id.Slovnia);
                break;
            default:
                inter.check(R.id.China);
                break;
        }
        inter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i) {
                    case R.id.China:
                        h = 15;
                        break;
                    case R.id.USA:
                        h = 0;
                        break;
                    case R.id.France:
                        h = 1;
                        break;
                    case R.id.Germany:
                        h = 2;
                        break;
                    case R.id.UK:
                        h = 3;
                        break;
                    case R.id.Denmark:
                        h = 4;
                        break;
                    case R.id.Sweden:
                        h = 5;
                        break;
                    case R.id.Italy:
                        h = 6;
                        break;
                    case R.id.SpainI:
                        h = 7;
                        break;
                    case R.id.Japan:
                        h = 8;
                        break;
                    case R.id.Norway:
                        h = 9;
                        break;
                    case R.id.DenmarkII:
                        h = 10;
                        break;
                    case R.id.SpainII:
                        h = 11;
                        break;
                    case R.id.Latin:
                        h = 12;
                        break;
                    case R.id.Korea:
                        h = 13;
                        break;
                    case R.id.Slovnia:
                        h = 14;
                        break;
                }
                PrefUtils.setInt(mContext, "country", h);
            }
        });

        builder.setView(internat);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                myPrinter.setInternational(h);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    //位置及旋转
    private void isRoate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("设置位置");
        View roate = getLayoutInflater().inflate(R.layout.activity_align_ment, null);
        RadioGroup TopAndBottom = (RadioGroup) roate.findViewById(R.id.TopAndBottom);
        int top = PrefUtils.getInt(mContext, "top/botoom", 1);
        switch (top) {
            case 0:
                TopAndBottom.check(R.id.isTop);
                break;
            case 1:
                TopAndBottom.check(R.id.isBottom);
                break;
        }
        TopAndBottom.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.isTop:
                        t = 0;
                        break;
                    case R.id.isBottom:
                        t = 1;
                        break;
                }
                PrefUtils.setInt(mContext, "top/botoom", t);
            }
        });
        RadioGroup LocalGroup = (RadioGroup) roate.findViewById(R.id.LocalGroup);
        int Local = PrefUtils.getInt(mContext, "LocalGroup", 0);
        switch (Local) {
            case 0:
                LocalGroup.check(R.id.mLight);
                break;
            case 1:
                LocalGroup.check(R.id.mCenter);
                break;
            case 2:
                LocalGroup.check(R.id.mRight);
                break;
        }
        LocalGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.mLight:
                        k = 0;
                        break;
                    case R.id.mCenter:
                        k = 1;
                        break;
                    case R.id.mRight:
                        k = 2;
                        break;
                }
                PrefUtils.setInt(mContext, "LocalGroup", k);
            }
        });

        RadioGroup RoateGroup = (RadioGroup) roate.findViewById(R.id.RoateGroup);
        int Roate = PrefUtils.getInt(mContext, "RoateGroup", 0);
        switch (Roate) {
            case 0:
                RoateGroup.check(R.id.radio0);
                break;
            case 1:
                RoateGroup.check(R.id.radio90);
                break;
            case 2:
                RoateGroup.check(R.id.radio180);
                break;
            case 3:
                RoateGroup.check(R.id.radio270);
                break;
        }
        RoateGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radio90:
                        j = 1;
                        break;
                    case R.id.radio180:
                        j = 2;
                        break;
                    case R.id.radio270:
                        j = 3;
                        break;
                    case R.id.radio0:
                        j = 0;
                        break;
                }
                PrefUtils.setInt(mContext, "RoateGroup", j);
            }
        });
        builder.setView(roate);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                myPrinter.Alignment(t, k, j);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    //打印进纸
    private void mFeed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("设置进纸行数");
        View feed = getLayoutInflater().inflate(R.layout.activity_text_util, null);
        final EditText editText = (EditText) feed.findViewById(R.id.mfeed);
        String er = PrefUtils.getString(mContext, "editText", editText.getText().toString());
        editText.setText(er);
        builder.setView(feed);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String ed = editText.getText().toString();
                edText();
                PrefUtils.setString(mContext, "editText", ed);
                myPrinter.Formfeed(Integer.valueOf(ed));
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();

    }

    //设置字体
    private void setFont() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("设置");
        View FontView = getLayoutInflater().inflate(R.layout.activity_font_topand_bottom, null);
        final CheckBox isShx = (CheckBox) FontView.findViewById(R.id.isShx);
        final CheckBox shiXhx = (CheckBox) FontView.findViewById(R.id.shiXhx);
        final CheckBox isBold = (CheckBox) FontView.findViewById(R.id.isBold);
        final CheckBox isFanBai = (CheckBox) FontView.findViewById(R.id.isFanBai);
        final CheckBox isJiaCu = (CheckBox) FontView.findViewById(R.id.isJiaCu);
        final EditText Bk = (EditText) FontView.findViewById(R.id.Bk);
        final EditText Bg = (EditText) FontView.findViewById(R.id.Bg);
        String Bg1 = PrefUtils.getString(mContext, "BG", Bg.getText().toString());
        Bg.setText(Bg1);
        String Bk1 = PrefUtils.getString(mContext, "BK", Bk.getText().toString());
        Bk.setText(Bk1);
        RadioGroup FontGroup = (RadioGroup) FontView.findViewById(R.id.FontGroup);
        isChoose = PrefUtils.getBoolean(mContext, "isShx", false);
        if (isChoose) {
            isShx.setChecked(true);
        }
        isChoose = PrefUtils.getBoolean(mContext, "shiXhx", false);
        if (isChoose) {
            shiXhx.setChecked(true);
        }
        isChoose = PrefUtils.getBoolean(mContext, "isBold", false);
        if (isChoose) {
            isBold.setChecked(true);
        }
        isChoose = PrefUtils.getBoolean(mContext, "isFanBai", false);
        if (isChoose) {
            isFanBai.setChecked(true);
        }
        isChoose = PrefUtils.getBoolean(mContext, "isJiaCu", false);
        if (isChoose) {
            isJiaCu.setChecked(true);
        }
        int Font = PrefUtils.getInt(mContext, "FontGroup", 3);
        switch (Font) {
            case 0:
                FontGroup.check(R.id.type_6x8);
                break;
            case 1:
                FontGroup.check(R.id.type_6x8_2);
                break;
            case 2:
                FontGroup.check(R.id.ASCII);
                break;
            case 3:
                FontGroup.check(R.id.Chinese);
                break;
        }
        FontGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int number = 0;
                switch (i) {
                    case R.id.Chinese://汉字集
                        number = 3;
                        b = TextType.TYPE_FOUR;
                        break;
                    case R.id.type_6x8://字符集一
                        number = 0;
                        b = TextType.TYPE_ONE;
                        break;
                    case R.id.type_6x8_2://字符集二
                        number = 1;
                        b = TextType.TYPE_TWO;
                        break;
                    case R.id.ASCII://ASCII码集
                        number = 2;
                        b = TextType.TYPE_THREE;
                        break;

                }

                PrefUtils.setInt(mContext, "FontGroup", number);
            }

        });
        builder.setView(FontView);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String bg = Bg.getText().toString();
                String bk = Bk.getText().toString();

                if (bg.equals("") && !bk.equals("")) {
                    bg = "0";
                    myPrinter.drawFont(b, isShx.isChecked(), shiXhx.isChecked(), isBold.isChecked(), Integer.parseInt(Bk.getText().toString()), Integer.parseInt(bg), isFanBai.isChecked());
                } else if (bk.equals("") && !bg.equals("")) {
                    bk = "0";
                    myPrinter.drawFont(b, isShx.isChecked(), shiXhx.isChecked(), isBold.isChecked(), Integer.parseInt(bk), Integer.parseInt(Bg.getText().toString()), isFanBai.isChecked());
                } else if (bk.equals("") && bg.equals("")) {
                    bg = "0";
                    bk = "0";
                    myPrinter.drawFont(b, isShx.isChecked(), shiXhx.isChecked(), isBold.isChecked(), Integer.parseInt(bk), Integer.parseInt(bg), isFanBai.isChecked());
                } else {
                    myPrinter.drawFont(b, isShx.isChecked(), shiXhx.isChecked(), isBold.isChecked(), Integer.parseInt(Bk.getText().toString()), Integer.parseInt(Bg.getText().toString()), isFanBai.isChecked());
                }
                if (shiXhx.isChecked()) {
                    myPrinter.sendBytesData(new byte[]{0x1b, 0x2e, 1});
                } else {
                    myPrinter.sendBytesData(new byte[]{0x1b, 0x2e, 0});
                }
                if (isJiaCu.isChecked()) {
                    myPrinter.sendBytesData(new byte[]{0x1b, 0x21, 0x08});
                } else {
                    myPrinter.sendBytesData(new byte[]{0x1b, 0x21, 0x00});
                }
                PrefUtils.setString(mContext, "BG", bg);
                PrefUtils.setString(mContext, "BK", bk);
                PrefUtils.setBoolean(mContext, "isShx", isShx.isChecked());
                PrefUtils.setBoolean(mContext, "shiXhx", shiXhx.isChecked());
                PrefUtils.setBoolean(mContext, "isBold", isBold.isChecked());
                PrefUtils.setBoolean(mContext, "isFanBai", isFanBai.isChecked());
                PrefUtils.setBoolean(mContext, "isJiaCu", isJiaCu.isChecked());
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    public Action getIndexApiAction() {
//        Thing object = new Thing.Builder()
//                .setName("Main Page") // TODO: Define a title for the content shown.
//                // TODO: Make sure this auto-generated URL is correct.
//                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
//                .build();
//        return new Action.Builder(Action.TYPE_VIEW)
//                .setObject(object)
//                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
//                .build();
//    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        AppIndex.AppIndexApi.start(client, getIndexApiAction());
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        AppIndex.AppIndexApi.end(client, getIndexApiAction());
//        client.disconnect();
//    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return test.length;
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            button = new Button(UsbPrinter.this);
            button.setText(test[i]);
            button.setPadding(8, 8, 8, 8);
            button.setTextSize(10);
            button.setFocusable(false);
            button.setClickable(false);
            button.setHeight(60);
            return button;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hasRegDisconnectReceiver) {
            mContext.unregisterReceiver(myReceiver);
            hasRegDisconnectReceiver = false;
        }
    }

    //连接打印机
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isConnected) {
                switch (view.getId()) {
                    //USB连接
                    case R.id.Usb_connect:
                        AlertDialog.Builder usb_builder = new AlertDialog.Builder(mContext);
                        usb_builder.setTitle("提示：");
                        usb_builder.setMessage("搜索设备");
                        usb_builder.setPositiveButton("返回", null);
//          UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
//          上一次的设备
//          usbAutoConn(manager);
                        usb_builder.setNegativeButton("查找设备", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(mContext, UsbDeviceList.class);
                                startActivityForResult(intent, CONNECT_DEVICE_USB);
                            }
                        });
                        usb_builder.show();
                        break;
                    //蓝牙连接
                    case R.id.Bth_connect:
                        AlertDialog.Builder bth_builder = new AlertDialog.Builder(mContext);
                        bth_builder.setTitle("提示：");
                        bth_builder.setMessage("是否连接上次设备");
                        bth_builder.setPositiveButton("确认连接", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!(mBtAdapter == null)) {
                                    if (!mBtAdapter.isEnabled()) {
                                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                        startActivity(enableIntent);
                                    } else {
                                        //mDevice
                                        if (devicesAddress == null || devicesAddress.length() <= 0) {
                                            showToast("您是第一次启动程序，请选择重新搜索连接！");
                                        } else {
                                            connect2BluToothdevice();
                                        }
                                    }
                                }

                            }
                        });
                        bth_builder.setNegativeButton("重新搜索", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!(mBtAdapter == null)) {
                                    if (!mBtAdapter.isEnabled()) {
                                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                        startActivity(enableIntent);
                                        Intent intent = new Intent(mContext, BluetoothDeviceList.class);
                                        startActivityForResult(intent, CONNECT_DEVICE_BTH);
                                    } else {
                                        Intent intent = new Intent(mContext, BluetoothDeviceList.class);
                                        startActivityForResult(intent, CONNECT_DEVICE_BTH);
                                    }
                                }
                            }
                        });
                        bth_builder.show();
                        break;
                }
            } else {
                if (myPrinter != null) {
                    myPrinter.closeConnection();
                    myPrinter = null;
                    if (hasRegDisconnectReceiver) {
                        mContext.unregisterReceiver(myReceiver);
                        hasRegDisconnectReceiver = false;
                    }
                }
            }
        }
    };

    //得到Wifi名字

    //接收设备
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == CONNECT_DEVICE_BTH) {
            devicesAddress = data.getExtras().getString(BluetoothDeviceList.EXTRA_DEVICE_ADDRESS);
            devicesName = data.getExtras().getString(BluetoothDeviceList.EXTRA_DEVICE_NAME);
            connect2BluToothdevice();
        } else if (requestCode == CONNECT_DEVICE_USB) {
            mUSBDevice = data.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            myPrinter = PrinterInstance_One.getPrinterInstance(mContext, mUSBDevice, mHandler);
            devicesName = "USB device";
            UsbManager mUsbManager = (UsbManager) mContext.getSystemService(USB_SERVICE);
            if (mUsbManager.hasPermission(mUSBDevice)) {
                myPrinter.openConnection();
            } else {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                mContext.registerReceiver(mUsbReceiver, filter);//注册广播
                mUsbManager.requestPermission(mUSBDevice, pendingIntent);
            }
        } else if (requestCode == REQUEST_CODE) {
            FilePath = data.getStringExtra(EXTRA_FILE_CHOOSER);
            if (FilePath != null) {
                showToast("Choose File : " + FilePath);
            } else {
                showToast("打开文件失败");
            }
        }
    }

    //连接两个设备
    private void connect2BluToothdevice() {
        dialog.show();
        mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(devicesAddress);
        devicesName = mDevice.getName();
        myPrinter = PrinterInstance_One.getPrinterInstance(mDevice, mHandler);
        if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            //未绑定
            IntentFilter boundFilter = new IntentFilter();
            boundFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            mContext.registerReceiver(boundDeviceReceiver, boundFilter);
            PairOrConnect(true);
        } else {
            PairOrConnect(false);
        }
    }

    //是否已经配对
    private void PairOrConnect(boolean pair) {
        if (pair) {
            IntentFilter boundFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            mContext.registerReceiver(boundDeviceReceiver, boundFilter);
            boolean success = false;
            try {
                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                success = (boolean) createBondMethod.invoke(mDevice);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            new connectThread().start();
        }
    }

    //获取之前的控件
    public void usbAutoConn(UsbManager manager) {
        doDiscovery(manager);
        if (!deviceList.isEmpty()) {
            mUSBDevice = deviceList.get(0);
        }
        if (mUSBDevice != null) {
            PrinterInstance_One.getPrinterInstance(mContext, mUSBDevice, mHandler).openConnection();

        } else {
            mHandler.obtainMessage(PrinterConstants.Connect.FAILED).sendToTarget();
            myPrinter.closeConnection();
        }
    }

    //得到USB设备
    private void doDiscovery(UsbManager manager) {
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        deviceList = new ArrayList<UsbDevice>();
        for (UsbDevice device : devices.values()) {
            if (USBPort.isUSBPrinter(device)) {
                deviceList.add(device);
//                showToast(device.getDeviceName() + device.getVendorId());
            }
        }
    }

    //USB权限广播
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    mContext.unregisterReceiver(mUsbReceiver);
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && mUSBDevice.equals(device)) {
                        myPrinter.openConnection();
                    } else {
                        mHandler.obtainMessage(PrinterConstants.Connect.FAILED).sendToTarget();
                    }
                }
            }
        }
    };
    //关闭蓝牙
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                if (device != null && myPrinter != null && isConnected && device.equals(mDevice)) {
                    myPrinter.closeConnection();
                    mHandler.obtainMessage(PrinterConstants.Connect.CLOSED).sendToTarget();
                }
            }
        }
    };
    //蓝牙连接广播
    private BroadcastReceiver boundDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mDevice.equals(device)) {
                    return;
                }
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:

                        break;
                    case BluetoothDevice.BOND_BONDED:
                        mContext.unregisterReceiver(boundDeviceReceiver);
                        dialog.show();
                        if (myPrinter != null) {
                            new connectThread().start();
                        }
                        break;
                    case BluetoothDevice.BOND_NONE:
                        mContext.unregisterReceiver(boundDeviceReceiver);
                        break;
                }
            }
        }
    };

    //连接中线程
    private class connectThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (myPrinter != null) {
                isConnected = myPrinter.openConnection();
            }
        }
    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            exitBy2Click();
//        }
//        return false;
//    }
//
//    private static Boolean isExit = false;
//
//    private void exitBy2Click() {
//        Timer tExit = null;
//        if (isExit == false) {
//            isExit = true;
//            showToast("请再按一次退出程序");
//            tExit = new Timer();
//            tExit.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    isExit = false;
//                }
//            }, 2000);
//        } else {
//            finish();
//            System.exit(0);
//        }
//    }

    private void showToast(String string) {
        Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show();
    }

}
