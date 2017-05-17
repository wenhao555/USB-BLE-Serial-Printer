package com.xkdx.serial_test;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/5.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = CrashHandler.class.getCanonicalName();
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例
    private static CrashHandler INSTANCE = new CrashHandler();
    //存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();
    //格式化日期作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyyMMd_HHmmss");
    private Context mContext;

    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {

        mContext = context;
        //获取系统默认的处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序默认的处理器
        Thread.setDefaultUncaughtExceptionHandler(this);

    }

    //当出现错误时
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);

        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //使用Toast来显示信息
        new Thread() {
            @Override
            public void run() {
                super.run();
                Looper.prepare();
                Toast.makeText(mContext, "程序出现异常", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        collectDeviceInfo(mContext);
        //保存日志文件
        String str = saveCrashInfo2File(ex);
        Log.e(TAG, str);
        return false;
    }

    public void collectDeviceInfo(Context ctx) {
        //包的管理者
        PackageManager pm = ctx.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionName", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + ":" + field.get(null));

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    //保存数据到文件
    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append("[" + key + "," + value + "]\n");
        }
        String time = formatter.format(new Date());
        TelephonyManager mTelePhonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = mTelePhonyMgr.getDeviceId();
        if (TextUtils.isEmpty(imei)) {
            imei = "unknownimei";

        }
        String fileName = "CRS_" + time + "_" + imei + ".txt";
        File sdDir = null;
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        File cacheDir = new File(sdDir + File.separator + "dPhoneLog");
        if (!cacheDir.exists())
            cacheDir.mkdir();
        File filePath = new File(cacheDir + File.separator + fileName);
        try {
            //文件流
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(stringBuffer.toString().getBytes());
            fos.close();
            return fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {

            return "";

        }
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {

                return "";

            }
            t = t.getCause();
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter pw = new PrintWriter(stringWriter);
        tr.printStackTrace(pw);
        return stringWriter.toString();
    }

}
