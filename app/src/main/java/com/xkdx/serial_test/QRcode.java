package com.xkdx.serial_test;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.usbinstance.PrinterInstance_One;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class QRcode extends AppCompatActivity {
    private EditText QRCode, start_l, start_b, start_v, start_r;
    private Button printQR, finish;
    private Context mCentext;
    private PrinterIntence myPrinter;
    private String prot;
    int baudrate;
    private Intent intent;
    private PrinterInstance_One myPrinter1;
    private String defaultAddress = "/dev/ttyS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
//        myPrinter = PrinterIntence.mPrinter;
        intent = getIntent();
        if (intent.getExtras() == null) {
            myPrinter1 = PrinterInstance_One.mPrinter;
            Log.e("串口为空", "悟空悟空悟空");
        } else {
            String prot = intent.getExtras().getString("prot");
            String address = intent.getExtras().getString("address");
            int baudrate = intent.getExtras().getInt("baudrate");
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
        QRCode = (EditText) findViewById(R.id.QRCode);
        start_l = (EditText) findViewById(R.id.start_l);
        start_b = (EditText) findViewById(R.id.start_b);
        start_v = (EditText) findViewById(R.id.start_v);
        start_r = (EditText) findViewById(R.id.start_r);
        printQR = (Button) findViewById(R.id.printQR);
        printQR.setOnClickListener(listener);
        finish = (Button) findViewById(R.id.finish);
        finish.setOnClickListener(listener);
        start_l.setText("1");
        start_b.setText("1");
        start_v.setText("1");
        start_r.setText("1");
        QRCode.setText("12345678");
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.printQR:
                    if (PrinterInstance_One.mPrinter != null || myPrinter != null) {
                        try {
                            String l = start_l.getText().toString();
                            String b = start_b.getText().toString();
                            String v = start_v.getText().toString();
                            String r = start_r.getText().toString();
                            String QR = QRCode.getText().toString();
                            String q = new String(QR.getBytes("unicode"), "unicode");
                            if ((l.equals("")) || (b.equals("")) || (v.equals("")) || (r.equals("")) || (QR.equals(""))) {
                                showToast("请输入规范内容");
                                return;
                            }
                            if (Integer.valueOf(b) > 8) {
                                b = "8";
                            }
                            if (Integer.valueOf(v) > 20) {
                                v = "20";
                            }
                            if (Integer.valueOf(r) > 4) {
                                r = "4";
                            }
                            if (intent.getExtras() == null) {

                                myPrinter1.setQrCode(Integer.valueOf(l), Integer.valueOf(b), Integer.valueOf(v), Integer.valueOf(r), q);
                            } else {
                                myPrinter.setQrCode(Integer.valueOf(l), Integer.valueOf(b), Integer.valueOf(v), Integer.valueOf(r), q);

                            }

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        showToast("打印机断开连接");
                    }
                    break;
                case R.id.finish:
                    finish();
                    break;
            }
        }
    };

    private void showToast(String string) {
        Toast.makeText(mCentext, string, Toast.LENGTH_SHORT).show();
    }
}
