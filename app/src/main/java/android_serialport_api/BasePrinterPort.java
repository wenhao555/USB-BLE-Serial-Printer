package android_serialport_api;

/**
 * Created by Administrator on 2017/1/18.
 */

public interface BasePrinterPort {
    // 判断打开
    boolean open();

    // 关闭的方法
    void close();

    // 向打印机发送数据
    int write(byte[] var1);

    // 从打印机中读取数据
    int read(byte[] var1);
}
