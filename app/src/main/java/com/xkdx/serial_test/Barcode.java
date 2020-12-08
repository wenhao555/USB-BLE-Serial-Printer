package com.xkdx.serial_test;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usbinstance.PrinterInstance_One;

import java.io.File;

import android_serialport_api.MyBarCode;
import android_serialport_api.PrefUtils;

public class Barcode extends AppCompatActivity {
    private TextView explain;
    private RadioGroup BarGroup;
    private EditText BarText, codeWidth, codeHeight, numberBar, Barstart;
    private Button printBar, barfinish;
    private Context mContext;
    private byte b;//条形码类型
    private PrinterIntence myPrinter;
    private Intent intent;
    private PrinterInstance_One myPrinter1;
    private String defaultAddress = "/dev/ttyS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        intent = getIntent();

        if (intent.getExtras() == null) {
            myPrinter1 = PrinterInstance_One.mPrinter;
            Log.e("串口为空", "悟空悟空悟空");
        } else {
            String prot = intent.getExtras().getString("prot1");
            String address = intent.getExtras().getString("address1");
            int baudrate = intent.getExtras().getInt("baudrate1");

            if (prot.equals("")) {

                myPrinter = new PrinterIntence(new File(address), baudrate, 0);

            } else {

                myPrinter = new PrinterIntence(new File(defaultAddress + prot), baudrate, 0);

            }
            myPrinter.Open();
            Log.e("USB Ble为空", "为空为空为空");
        }

        init();
    }


    private void init() {
        explain = (TextView) findViewById(R.id.explain);
        BarGroup = (RadioGroup) findViewById(R.id.BarGroup);
        BarText = (EditText) findViewById(R.id.BarText);
        codeWidth = (EditText) findViewById(R.id.codeWidth);
        codeHeight = (EditText) findViewById(R.id.codeHeight);
        numberBar = (EditText) findViewById(R.id.numberBar);
        Barstart = (EditText) findViewById(R.id.Barstart);
        printBar = (Button) findViewById(R.id.printBar);
        barfinish = (Button) findViewById(R.id.barfinish);
//        int bar = PrefUtils.getInt(mContext, "BarCode", 0);
//        switch (bar) {
//            case 0:
//                BarGroup.check(R.id.UPCA);
//                break;
//            case 1:
//                BarGroup.check(R.id.UPCE);
//                break;
//            case 2:
//                BarGroup.check(R.id.JAN13);
//                break;
//            case 3:
//                BarGroup.check(R.id.JAN8);
//                break;
//            case 4:
//                BarGroup.check(R.id.CODE39);
//                break;
//            case 5:
//                BarGroup.check(R.id.ITF);
//                break;
//            case 6:
//                BarGroup.check(R.id.CODABAR);
//                break;
//            case 7:
//                BarGroup.check(R.id.CODE128);
//                break;
//        }
        BarGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int number = 0;
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.UPCA:
                        number = 0;
                        explain.setText("注：UPC-A类型一维码数据范围0-9，码长度为11位，最后一位为校验位。");
                        BarText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
                        b = MyBarCode.UPC_A;
                        break;
                    case R.id.UPCE:
                        number = 1;
                        explain.setText("注：UPC_E码数据范围是0-9，商品条码是纯数字，是由UPC_A缩减而成，位数是7位，而且首位必须为0，在编码过后外加一位校验码，共8位。");
                        BarText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                        b = MyBarCode.UPC_E;
                        break;
                    case R.id.JAN13:
                        number = 2;
                        BarText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
                        explain.setText("注：JAN13是纯数字，而且位数是12位，在编码过后外加一位校验码，组成13位数字，校验码打印机自动生成。");
                        b = MyBarCode.JAN13;
                        break;
                    case R.id.JAN8:
                        number = 3;
                        BarText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
                        explain.setText("注：JAN8是纯数字，而且位数是7位，在编码过后外加一位校验码，组成8位数字，校验码打印机自动生成。");
                        b = MyBarCode.JAN8;
                        break;
                    case R.id.CODE39:
                        number = 4;
                        explain.setText("注：CODE39条码生成字符集包括数字、大写字母以及-.$/+%*空格等字符，其中*只用于标记开始和结束，本打印机打印时不显示开始及结束符。");
                        b = MyBarCode.CODE39;
                        break;
                    case R.id.ITF:
                        number = 5;
                        BarText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
                        explain.setText("注：ITF交叉25码，字符集仅为数字0-9且个数为偶数。");
                        b = MyBarCode.ITF;
                        break;
                    case R.id.CODABAR:
                        number = 6;
                        explain.setText("注：CODABAR条码生成，字符集包括数字和-$:/.+以及ABCD等字符，其中ABCD只用于开始或者结尾，作为标识符使用。打印此条码时开始与结束必须以ABCD中的任意字母开始或者结束。");
                        b = MyBarCode.CODABAR;
                        break;
                    case R.id.CODE93:
                        number = 7;
                        explain.setText("注：CODE93条码生成字符集包括数字、大写字母以及-.$/+%*空格等字符，其中*只用于标记开始和结束，本打印机打印时不显示开始及结束符。");
                        b = MyBarCode.CODE93;
                        break;
                    case R.id.CODE128:
                        number = 8;
                        explain.setText("注：CODE128字符集包括大小写字母、数字、常用标点符号..");
                        b = MyBarCode.CODE128;
                        break;
                }
//                PrefUtils.setInt(mContext, "BarCode", number);
            }
        });
        printBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String width = codeWidth.getText().toString();
                String height = codeHeight.getText().toString();
                String number = numberBar.getText().toString();
                String start = Barstart.getText().toString();
                String content = BarText.getText().toString();

                if ((content.equals("")) || (width.equals("")) || (height.equals("")) || (number.equals("")) || (start.equals(""))) {
//                    showToast("请输入规范内容");
                    Toast.makeText(mContext, "请输入规范内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (content.equals("")) {
//                    showToast("请输入规范内容");
                    Toast.makeText(mContext, "请输入规范内容", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (intent.getExtras() == null) {
                    myPrinter1.codebar_init(Integer.valueOf(width), Integer.valueOf(height), Integer.valueOf(number), Integer.valueOf(start));
                    myPrinter1.printBarcode(b, content);

                } else {
                    myPrinter.codebar_init(Integer.valueOf(width), Integer.valueOf(height), Integer.valueOf(number), Integer.valueOf(start));
                    myPrinter.printBarcode(b, content);

                }
            }
        });
        barfinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


//    private void showToast(String string) {
//        Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show();
//    }
}
