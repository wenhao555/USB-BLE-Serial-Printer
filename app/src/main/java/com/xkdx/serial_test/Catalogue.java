package com.xkdx.serial_test;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import usbprinter.UsbPrinter;

public class Catalogue extends AppCompatActivity {
    private ListView MyCatalogue;
    private String[] strings = {"USB打印和蓝牙2.0打印", "串口打印"};
    private Intent intent;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogue);
        mContext = this;
        MyCatalogue = (ListView) findViewById(R.id.MyCatalogue);
        MyCatalogue.setAdapter(new mCatalogueAdapter());
        MyCatalogue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        intent = new Intent(mContext, UsbPrinter.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(mContext, MainActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    private class mCatalogueAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return strings.length;
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
            TextView textView = new TextView(Catalogue.this);
            textView.setPadding(10, 20, 10, 20);
            textView.setTextSize(20);
            textView.setText(strings[i]);

            return textView;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click();
        }
        return false;
    }

    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true;
            Toast.makeText(mContext, "请再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }
}
