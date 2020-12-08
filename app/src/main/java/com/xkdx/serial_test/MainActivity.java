package com.xkdx.serial_test;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.os.Handler;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.BasePrinterPort;
import android_serialport_api.MyBarCode;
import android_serialport_api.PrefUtils;
import android_serialport_api.SerialPort;
import android_serialport_api.TextType;
import bmputil.BitmapConvertor;
import serialport.SerialPortFinder;

public class MainActivity extends Activity {
    protected SerialPort mSerialPort;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    private TextView text;
    private EditText mEdit;
    public static boolean isConnected = false;
    private String prot = "0";
    private int baudrate = 9600;
    private static int i = 0;
    private StringBuilder sb;
    private Context mContext;
    private BitmapConvertor convertor;
    private GridView mGridView;
    private String test[] = {"普通打印", "状态", "位置及旋转", "设置字体添加上/下划线", "打印图片", "回车换行", "打印进纸n行", "标签模式", "直线", "虚线", "切刀", "打印二维码", "一维码/条码打印"};

    private void showToast(String string) {
        Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show();
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1) {
                text.setText(text.getText().toString().trim() + sb.toString());
            }
        }
    };
    private Button button;
    private byte b;
    private int k;//左中右
    private int j;//旋转
    private int t;//上/下对齐
    private int h;
    private static boolean isLine = false;
       public static boolean isChoose = false;
    private static PrinterIntence myPrinter;
    private ProgressDialog mDialog;
    private String[] entries;//得到所有的address
    private String[] entryValues;
    private Spinner mySpinner;
    private String serials;
    private String defaultAddress = "/dev/ttyS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        setContentView(R.layout.activity_main);
        sb = new StringBuilder();
                 mContext = this;
        myPrinter = PrinterIntence.mPrinter;
        convertor = new BitmapConvertor(this);
        //接收的信息

        text = (TextView) findViewById(R.id.text_receive);
        zhijiance = (TextView) findViewById(R.id.zhijiance);
        working = (TextView) findViewById(R.id.working);
        huanchong = (TextView) findViewById(R.id.huanchong);
           zhuangtai = (TextView) findViewById(R.id.zhuangtai);
        mEdit = (EditText) findViewById(R.id.mEdit);
        mDialog = new ProgressDialog(mContext);
        mySpinner = (Spinner) findViewById(R.id.mySpinner);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mDialog.setTitle("打印中...");
        mDialog.setMessage("请稍等...");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        final EditText et_prot = (EditText) findViewById(R.id.et_prot);
        //波特率
        final EditText et_num = (EditText) findViewById(R.id.et_num);
//设置
        Button btn_set = (Button) findViewById(R.id.btn_set);
        btn_set.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //端口号

                prot = TextUtils.isEmpty(et_prot.getText().toString().trim()) ? ""
                        : et_prot.getText().toString().trim();
                baudrate = Integer.parseInt(TextUtils.isEmpty(et_num.getText()
                        .toString().trim()) ? "9600" : et_num.getText()
                        .toString().trim());
            }
        });
//打开
        Button btn_open = (Button) findViewById(R.id.btn_open);
//关闭
        Button btn_receive = (Button) findViewById(R.id.btn_receive);
        btn_receive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSerialPort();
            }
        });
        mGridView = (GridView) findViewById(R.id.mGridView);

        mGridView.setAdapter(new MyAdapter());
        mGridView.setOnItemClickListener(Onlistener);
        deviceArrayAdapter = new ArrayAdapter<String>(mContext, R.layout.activity_device_item);
        discovery();//扫描串口
        mySpinner.setAdapter(deviceArrayAdapter);
        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serials = parent.getItemAtPosition(position).toString();
                Toast.makeText(mContext, serials, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btn_open.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 打开
                if (prot.length() == 0) {

                    myPrinter = new PrinterIntence(new File(serials), baudrate, 0);
                    Toast.makeText(mContext, serials, Toast.LENGTH_SHORT).show();
                } else {

                    myPrinter = new PrinterIntence(new File(defaultAddress + prot), baudrate, 0);
                    showToast("dev");

                }
                myPrinter.Open();
                myPrinter.Read();
            }
        });
    }

    private ArrayAdapter<String> deviceArrayAdapter;

    private void discovery() {

        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        entryValues = mSerialPortFinder.getAllDevicesPath();//dev/ttyS0

        for (int i = 0; i < entryValues.length; i++) {
            deviceArrayAdapter.add(entryValues[i]);
        }
    }

    private AdapterView.OnItemClickListener Onlistener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (myPrinter != null) {
                switch (i) {
                    case 0://打印文字
                        myPrinter.sendBytesData(new byte[]{0x1b, 0x63, 0x01});
                        edText();
                        break;
                    case 1://获得状态
                        states();
                        break;
                    case 2://位置及旋转
                        isRoate();
                        break;
                    case 3://设置文字属性
                        setFont();
                        break;
                    case 4:
//打印图片     注：图片需要转换
                        try {
                            int len = (int) myPrinter.getPrintStates();
                            if ((len & 0x02) == 0x02) {
                                //正在打印
                            } else {
                                myPrinter.sendBytesData(new byte[]{0x1b, 0x63, 00});
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
                                setPricture(BorWBitmap);
//                                setPricture(bit());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 5://回车
                        myPrinter.setCarriage();
                        break;
                    case 6://打印进纸
                        mFeed();
                        break;
                    case 7://标签
                        isLine = true;
                        edText();
                        myPrinter.Label(isLine);
                        showToast("进入标签模式");
                        break;
                    case 8://打印直线

                        myPrinter.setisLine();

                        break;
                    case 9://打印直线/虚线

                        myPrinter.setisnoLine();
                        break;
                    case 10://切刀

                        myPrinter.cutPaper();
                        
                        break;
                    case 11://QR码
                        Intent intent1 = new Intent(mContext, QRcode.class);
                        intent1.putExtra("prot", prot);
                        intent1.putExtra("address", serials);
                        intent1.putExtra("baudrate", baudrate);
                        startActivity(intent1);
                        break;
                    case 12://条码

                        Intent intent2 = new Intent(mContext, Barcode.class);
                        intent2.putExtra("prot1", prot);
                        intent2.putExtra("address1", serials);
                        intent2.putExtra("baudrate1", baudrate);
                        startActivity(intent2);

                        break;
                }
            } else {
                showToast("请先连接打印机");
            }


        }
    };

    //******************************************打印图片*************************************//
    // 打印图片
    public void setPricture(final Bitmap bitmap) throws IOException {
        mDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int[] sourceData = myPrinter.getBitmapData(bitmap);
                byte[] data = myPrinter.getByteArray(sourceData);
                // 当没有打印机时直接kill
                // 行间距

                myPrinter.sendBytesData(new byte[]{0x1b, 0x31, 0x00});

                int sendLen = bitmap.getWidth();


                byte[] ImageCMD = myPrinter.getImageCmd(myPrinter.IMAGECMD, sendLen);
                final int len = (int) myPrinter.getPrintStates();

                for (int i = 0; i < data.length / sendLen; i++) {

                    byte[] temp = Arrays.copyOfRange(data, i * sendLen, (i + 1) * sendLen);

                    byte[] stemp = myPrinter.concat(temp, myPrinter.WRAP_PRINT);

                    byte[] printData = myPrinter.concat(ImageCMD, stemp);

                    try {
                        MainActivity.Write(printData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if ((len & 0x02) == 0x02) {
                            //正在打印
                        } else {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mDialog.dismiss();
                        }
                    }
                }).start();
            }
        }).start();

    }

    // 写大数据   将图片的数据读入到程序中
    @SuppressLint("NewApi")
    private static void Write(byte[] command) throws IOException {
        int maxLenth = 1024;
        int count = command.length / maxLenth;
        byte[] buffer = null;
        for (int i = 0; i <= count; i++) {
            if (i < count) {
                buffer = Arrays.copyOfRange(command, i * maxLenth, (i + 1)
                        * maxLenth);
            } else {
                buffer = Arrays.copyOfRange(command, i * maxLenth, command.length);
            }

            myPrinter.sendBytesData(buffer);

        }
    }


    //*******************************************************************************************//
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

    private TextView zhijiance, working, huanchong, zhuangtai;

    //获得状态
    private void states() {
        int len = (int) myPrinter.getPrintStates();
        //纸检测状态不带切刀
//        if ((len & 1) == 1) {
//            zhijiance.setText("纸检测器：打印机缺纸");
//            showToast("缺纸");
//        } else {
//            zhijiance.setText("纸检测器：打印机有纸");
//        }
        //纸检测带切刀
        if ((len & 0x01) == 0x01) {
            zhijiance.setText("纸检测器：打印机有纸");
        } else {
            zhijiance.setText("纸检测器：打印机无纸");
        }
        //打印机工作状态
        if ((len & 2) == 2) {
            working.setText("工作状态：打印机正在打印");
//            showToast("打印中");
        } else {
            working.setText("工作状态：打印机空闲");
        }
        //接收缓存区状态
        if ((len & 4) == 4) {
            huanchong.setText("接收缓冲区：打印缓存区满");
        } else {
            huanchong.setText("接收缓冲区：打印缓存区未满");
        }
        if ((len & 8) == 8) {
            zhuangtai.setText("打印机状态：错误");
        } else {
            zhuangtai.setText("打印机状态：正常");
        }

    }

    private void edText() {
        String content = mEdit.getText().toString();
        if (!content.equals("")) {
            myPrinter.printText(content + "\n");
        } else {
            showToast("请输入文字");
        }
    }

    private Bitmap bitmap;

    //缩放图片
    private Bitmap bit() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.my_monochrome_image);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = 384;
        int newHeight = 320;
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBip = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newBip;
    }

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
            button = new Button(MainActivity.this);
            button.setText(test[i]);
            button.setPadding(8, 8, 8, 8);
            button.setTextSize(10);
            button.setFocusable(false);
            button.setClickable(false);
            button.setHeight(60);
            return button;
        }
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {

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

    }


}
