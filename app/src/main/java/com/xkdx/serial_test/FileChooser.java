package com.xkdx.serial_test;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import usbprinter.UsbPrinter;

public class FileChooser extends AppCompatActivity {
    private GridView mGridView;
    private View mBackView;
    private View mBtExit;
    private TextView mTvPath;
    private String mSdcardRootPath;
    private String mLastFilePath;
    private ArrayList<FileChooserAdapter.FileInfo> mFileLists;
    private FileChooserAdapter mAdatper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);
        mSdcardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        init();
    }

    private void init() {
        mBackView = findViewById(R.id.imgBackFolder);
        mBackView.setOnClickListener(mClickListener);
        mBtExit = findViewById(R.id.btExit);
        mBtExit.setOnClickListener(mClickListener);
        mTvPath = (TextView) findViewById(R.id.tvPath);
        mGridView = (GridView) findViewById(R.id.gvFileChooser);
        mGridView.setEmptyView(findViewById(R.id.tvEmptyHint));
        mGridView.setOnItemClickListener(mItemClickListener);
        setGridViewAdapter(mSdcardRootPath);
    }

    private void setGridViewAdapter(String filePath) {
        updateFileItems(filePath);
        mAdatper = new FileChooserAdapter(this, mFileLists);
        mGridView.setAdapter(mAdatper);
    }

    private void updateFileItems(String filePath) {
        mLastFilePath = filePath;
        mTvPath.setText(mLastFilePath);
        if (mFileLists == null) {
            mFileLists = new ArrayList<FileChooserAdapter.FileInfo>();
        }
        if (!mFileLists.isEmpty())
            mFileLists.clear();

        File[] files = folderScan(filePath);
        if (files == null)
            return;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isHidden())
                continue;
            String fileAbsolutePath = files[i].getAbsolutePath();
            String fileName = files[i].getName();
            boolean isDirectory = false;
            if (files[i].isDirectory()) {
                isDirectory = true;
            }
            FileChooserAdapter.FileInfo fileInfo = new FileChooserAdapter.FileInfo(fileAbsolutePath, fileName, isDirectory);
            mFileLists.add(fileInfo);

        }
        if (mAdatper != null) {
            mAdatper.notifyDataSetChanged();
        }

    }

    // 获得当前路径的所有文件
    private File[] folderScan(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        return files;
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.imgBackFolder:
                    backProcess();
                    break;
                case R.id.btExit:
                    Intent intent = new Intent();
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            FileChooserAdapter.FileInfo fileInfo = (FileChooserAdapter.FileInfo) (((FileChooserAdapter) adapterView
                    .getAdapter()).getItem(i));
            if (fileInfo.isDirectory()) // 点击项为文件夹, 显示该文件夹下所有文件
                updateFileItems(fileInfo.getFilePath());
            else if (true) { // 选择的文件
                Intent intent = new Intent();
                intent.putExtra(UsbPrinter.EXTRA_FILE_CHOOSER,
                        fileInfo.getFilePath());
                setResult(RESULT_OK, intent);
                finish();
            } else { // 其他文件.....
                toast("文件格式错误");
            }

        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            backProcess();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }// 返回上一层目录的操作

    public void backProcess() {
        // 判断当前路径是不是sdcard路径 ， 如果不是，则返回到上一层。
        if (!mLastFilePath.equals(mSdcardRootPath)) {
            File thisFile = new File(mLastFilePath);
            String parentFilePath = thisFile.getParent();
            updateFileItems(parentFilePath);
        } else {// 是sdcard路径 ，直接结束
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    private void toast(CharSequence hint) {
        Toast.makeText(this, hint, Toast.LENGTH_SHORT).show();
    }
}