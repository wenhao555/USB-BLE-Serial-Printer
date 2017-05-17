package bmputil;

/**
 * Created by Administrator on 2017/3/24.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapConvertor {
    private static final String TAG = "转化成黑白图的Class";

    private int mDataWidth;
    private byte[] mRawBitmapData;
    private byte[] mDataArray;
    private ProgressDialog mPd;
    private Context mContext;
    private int mWidth;
    private int mHeight;
    private String mStatus;
    private String mFileName;
    private File file = null;
    private static final String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase("mounted") ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";
    private static final String SAVA_REAL_PATH;

    static {
        SAVA_REAL_PATH = SAVE_PIC_PATH + "/good/savaPic";
    }

    public BitmapConvertor(Context context) {
        this.mContext = context;
    }

    public Bitmap convertBitmap(Bitmap inputBitmap) {//转换图片
        Bitmap bitmap = null;
        this.mWidth = inputBitmap.getWidth();
        this.mHeight = inputBitmap.getHeight();
        this.mFileName = "my_monochrome_image";
        this.mDataWidth = (this.mWidth + 31) / 32 * 4 * 8;
        this.mDataArray = new byte[this.mDataWidth * this.mHeight];
        this.mRawBitmapData = new byte[this.mDataWidth * this.mHeight / 8];
        this.convertArgbToGrayscale(inputBitmap, this.mWidth, this.mHeight);
        this.createRawMonochromeData();
        this.mStatus = this.saveImage(this.mFileName, this.mWidth, this.mHeight);
        Log.i("BitmapConvertor", "mStatus:" + this.mStatus);
        if (this.mStatus == "Success") {
            bitmap = getmonoChromeImage(this.file.getPath());
        }

        return bitmap;
    }

    private void convertArgbToGrayscale(Bitmap bmpOriginal, int width, int height) {//将普通的彩色图片转化成灰度图片
        int k = 0;
        boolean B = false;
        boolean G = false;
        boolean R = false;

        try {
            for (int e = 0; e < height; ++e) {
                int p;
                for (p = 0; p < width; ++k) {
                    int pixel = bmpOriginal.getPixel(p, e);
                    int var14 = Color.red(pixel);
                    int var13 = Color.green(pixel);
                    int var12 = Color.blue(pixel);
                    var14 = (int) (0.299D * (double) var14 + 0.587D * (double) var13 + 0.114D * (double) var12);//灰度的算法
                    if (var14 < 128) {
                        this.mDataArray[k] = 0;
                    } else {
                        this.mDataArray[k] = 1;
                    }
                    ++p;
                }

                if (this.mDataWidth > width) {
                    for (p = width; p < this.mDataWidth; ++k) {
                        this.mDataArray[k] = 1;
                        ++p;
                    }
                }
            }
        } catch (Exception var11) {
            Log.e("BitmapConvertor", var11.toString());
        }

    }

    private void createRawMonochromeData() {
        int length = 0;

        for (int i = 0; i < this.mDataArray.length; i += 8) {
            byte first = this.mDataArray[i];

            for (int j = 0; j < 7; ++j) {
                byte second = (byte) (first << 1 | this.mDataArray[i + j]);
                first = second;
            }

            this.mRawBitmapData[length] = first;
            ++length;
        }

    }

    private String saveImage(String fileName, int width, int height) {
        BMPFile bmpFile = new BMPFile();
        String PATH_LOGCAT = null;
        if (Environment.getExternalStorageState().equals("mounted")) {
            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Logs";
        } else {
            PATH_LOGCAT = this.mContext.getFilesDir().getAbsolutePath() + File.separator + "Logs";
        }

        File dir = new File(PATH_LOGCAT);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        this.file = new File(PATH_LOGCAT, fileName + ".bmp");
        Log.i("sprt", this.file.getPath());

        FileOutputStream fileOutputStream;
        try {
            this.file.createNewFile();
            fileOutputStream = new FileOutputStream(this.file);
        } catch (IOException var9) {
            var9.printStackTrace();
            return "Memory Access Denied";
        } catch (Exception var10) {
            var10.printStackTrace();
            return "Memory Access Denied";
        }

        bmpFile.saveBitmap(fileOutputStream, this.mRawBitmapData, width, height);
        return "Success";
    }

    public static Bitmap getmonoChromeImage(String filePath) {
        FileInputStream is = null;

        try {
            is = new FileInputStream(filePath);
        } catch (FileNotFoundException var3) {
            var3.printStackTrace();
        }

        return BitmapFactory.decodeStream(is);
    }

    class ConvertInBackground extends AsyncTask<Bitmap, String, Void> {
        ConvertInBackground() {

        }

        protected Void doInBackground(Bitmap... params) {
            BitmapConvertor.this.convertArgbToGrayscale(params[0], BitmapConvertor.this.mWidth, BitmapConvertor.this.mHeight);
            BitmapConvertor.this.createRawMonochromeData();
            BitmapConvertor.this.mStatus = BitmapConvertor.this.saveImage(BitmapConvertor.this.mFileName, BitmapConvertor.this.mWidth, BitmapConvertor.this.mHeight);
            Log.i("BitmapConvertor", "mStatus:" + BitmapConvertor.this.mStatus);
            return null;
        }

        protected void onPostExecute(Void result) {
            BitmapConvertor.this.mPd.dismiss();
            Toast.makeText(BitmapConvertor.this.mContext, "Monochrome bitmap created successfully. Please check in sdcard", Toast.LENGTH_SHORT).show();
            Bitmap bitmap = BitmapConvertor.getmonoChromeImage(BitmapConvertor.this.file.getPath());
            Log.i("BitmapConvertor", "bitmap:" + bitmap);
        }

        protected void onPreExecute() {
            BitmapConvertor.this.mPd = ProgressDialog.show(BitmapConvertor.this.mContext, "Converting Image", "Please Wait", true, false, (OnCancelListener) null);
        }
    }
}
