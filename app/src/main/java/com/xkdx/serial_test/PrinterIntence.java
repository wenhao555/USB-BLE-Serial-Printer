package com.xkdx.serial_test;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android_serialport_api.BasePrinterPort;
import android_serialport_api.MyBarCode;
import android_serialport_api.SerialPort;
import android_serialport_api.TextType;

/**
 * Created by Administrator on 2017/1/19.
 */

public class PrinterIntence {
    private String charsetName = "gbk";
    private static BasePrinterPort myPrinterPort;
    private SerialPort mSerialPort;
    public static PrinterIntence mPrinter = null;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    public PrinterIntence(File device, int baudrate, int flags) {
        try {
            mSerialPort = new SerialPort(device, baudrate, flags);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized PrinterIntence getPrinterIntence(File device, int baudrate, int flags) {
        if (mPrinter == null) {
            mPrinter = new PrinterIntence(device, baudrate, flags);

        }
        return mPrinter;
    }

    public void Open() {
        mOutputStream = mSerialPort.getOutputStream();
    }

    public void Read() {

        mInputStream = mSerialPort.getInputStream();
    }

    public void sendBytesData(byte[] srcData) {
        try {
            mOutputStream.write(srcData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
       * 读取打印机返回的数据（用于接收读到字节的数组） 返回为-1的时候未初始化打印机 返回为-2的时候数组里没有数据或者为空
       */
    public int read(byte[] buffer) throws IOException {

        return mInputStream == null ? -1 : (buffer != null && buffer.length != 0 ? mInputStream.read(buffer) : -2);

    }

    public void printText(String content) {
        byte[] data = null;
        if (this.charsetName != "") {
            try {
                data = content.getBytes(this.charsetName);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {

            data = content.getBytes();

        }
        this.sendBytesData(data);
    }


    // 读取打印机状态
    public byte getPrintStates() {

        byte[] val = new byte[30];

        for (int i = 0; i < 3; ++i) {
            this.sendBytesData(new byte[]{(byte) 27, (byte) 118});

            try {
                Thread.sleep(100L);
            } catch (InterruptedException var4) {
                var4.printStackTrace();
            }

            try {
                this.read(val);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return val[0];
    }

    //  初始化打印机
    public void initPrinter() {

        this.sendBytesData(new byte[]{(byte) 27, (byte) 64});

    }


    //  得到字节
    public static final byte[] getByteArray(int... array) {
        byte[] bytes = new byte[array.length];
        for (int i = 0; i < array.length; ++i) {
            bytes[i] = (byte) array[i];
        }

        return bytes;
    }

    //  得到Bitmap数据
    public int[] getBitmapData(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; ++i) {
            if (pixels[i] == -1) {
                pixels[i] = 0;
            } else {
                pixels[i] = 1;
            }
        }
        return this.get8BitData(pixels, width, height);
    }

    // 8位数据
    public int[] get8BitData(int[] source, int width, int height) {
        int[] targData = new int[width * height / 8];

        for (int i = 0; i < height / 8; ++i) {
            for (int j = 0; j < width; ++j) {
                int[] temp = new int[8];

                for (int k = 0; k < 8; ++k) {
                    temp[k] = source[(k + i * 8) * width + j];
                }

                targData[i * width + j] = this.binaryToDecimal(temp);
            }
        }

        return targData;
    }

    // // 得到图片数据
    public byte[] getImageCmd(byte[] CMD, int width) {
        String[] result = new String[2];
        String str = Integer.toHexString(width).toUpperCase();
        StringBuffer sbuffer = new StringBuffer();
        int olen = 4 - str.length();

        for (int end = 0; end < olen; ++end) {
            sbuffer.append("0");
        }

        sbuffer.append(str);
        result[0] = sbuffer.toString().substring(2, 4);
        result[1] = sbuffer.toString().substring(0, 2);
        int[] var8 = new int[]{Integer.parseInt(result[0], 16),
                Integer.parseInt(result[1], 16)};
        return concat(CMD, getByteArray(var8));
    }

    public static final byte[] IMAGECMD = getByteArray(new int[]{0, 27, 42, 1});
    public static final byte[] WRAP_PRINT = getByteArray(new int[]{10});

    //
    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    // 二进制,十进制
    public int binaryToDecimal(int[] src) {
        int result = 0;

        for (int i = 0; i < src.length; ++i) {
            result = (int) ((double) result + (double) src[i]
                    * Math.pow(2.0D, (double) (src.length - i - 1)));
        }

        return result;
    }
//******************************************打印图片*************************************//
    // 打印图片
    public void setPricture(Bitmap bitmap) throws IOException {
        byte[] add = this.getByteArray(0x00);
        int[] sourceData = this.getBitmapData(bitmap);
        byte[] data = this.getByteArray(sourceData);
        // 当没有打印机时直接kill
        // 行间距

        this.sendBytesData(new byte[]{0x1b, 0x31, 0x00});

        int sendLen = bitmap.getWidth();

        byte[] ImageCMD = this.getImageCmd(this.IMAGECMD, sendLen);

        for (int i = 0; i < data.length / sendLen; i++) {

            byte[] temp = Arrays.copyOfRange(data, i * sendLen, (i + 1)
                    * sendLen);

            byte[] stemp = this.concat(temp, this.WRAP_PRINT);

            byte[] printData = this.concat(ImageCMD, stemp);

            this.Write(printData);
        }
    }

    // 写大数据   将图片的数据读入到程序中
    @SuppressLint("NewApi")
    private void Write(byte[] command) throws IOException {
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
            this.sendBytesData(buffer);
        }
    }
//***********************************************************************************//
    public int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    // 回车
    public void setCarriage() {
        this.sendBytesData(new byte[]{0x0d, 0x0d});
    }

    // 二维码

    /**
     * start_l二维码位置
     * start_b倍数
     * start_v版本
     * start_r纠错等级
     * string       内容
     *
     * @throws UnsupportedEncodingException
     */
    public void setQrCode(int start_l, int start_b, int start_v, int start_r,
                          String string) {
        byte res[] = new byte[400];
        int i = 0;
        res[i++] = (byte) 0x1d;
        res[i++] = (byte) 0x51;
        res[i++] = (byte) (start_l % 256);
        res[i++] = (byte) (start_l / 256);

        res[i++] = (byte) 0x1d;
        res[i++] = (byte) 0x57;
        if (start_b < 1) {
            start_b = 1;
        } else if (start_b > 8) {
            start_b = 8;
        }

        res[i++] = (byte) start_b;
        res[i++] = (byte) 0x1d;
        res[i++] = (byte) 0x6b;
        res[i++] = (byte) 32;
        if (start_v < 1) {
            start_v = 1;
        } else if (start_v > 20) {
            start_v = 20;
        }

        res[i++] = (byte) start_v;
        if (start_r < 1) {
            start_r = 1;
        } else if (start_r > 4) {
            start_r = 4;
        }
        res[i++] = (byte) start_r;
        char[] len = string.toCharArray();
        for (int j = 0; j < len.length; j++) {
            res[i++] = (byte) len[j];
        }
        res[i++] = (byte) 0x00;
        this.sendBytesData(res);
    }

    public void cutPaper() {
        this.sendBytesData(new byte[]{0x1b, 0x69});
    }

    // 上下，左右,旋转对齐
    public void Alignment(int Top, int Light, int roate) {

        if ((byte) Top == 0) {
            this.sendBytesData(new byte[]{0x1c, 0x72, 0});
        } else {
            this.sendBytesData(new byte[]{0x1c, 0x72, 1});
        }

        if ((byte) Light == 1) {
            this.sendBytesData(new byte[]{0x1b, 0x61, 1});
        } else if ((byte) Light == 2) {
            this.sendBytesData(new byte[]{0x1b, 0x61, 2});
        } else {
            this.sendBytesData(new byte[]{0x1b, 0x61, 0});
        }

        if ((byte) roate == 1) {// 逆时针90°
            this.sendBytesData(new byte[]{0x1c, 0x49, 1});
        } else if ((byte) roate == 2) {// 逆时针180°
            this.sendBytesData(new byte[]{0x1c, 0x49, 2});
        } else if ((byte) roate == 3) {// 逆时针270°
            this.sendBytesData(new byte[]{0x1c, 0x49, 3});
        } else {// 0°
            this.sendBytesData(new byte[]{0x1c, 0x49, 0});
        }
    }

    // 设置字体

    /**
     * @param type   类型
     * @param Shx    上划线
     * @param Xhx    下划线
     * @param Bold   黑体
     * @param Bk     倍宽
     * @param Bg     倍高
     * @param Fanbai 反白
     */
    public void drawFont(byte type, boolean Shx, boolean Xhx, boolean Bold,
                         int Bk, int Bg, boolean Fanbai) {
        // 选择字体
        if (type == TextType.TYPE_ONE) {
            this.sendBytesData(new byte[]{0x1b, 0x36});
        } else if (type == TextType.TYPE_TWO) {
            this.sendBytesData(new byte[]{0x1b, 0x37});
        } else if (type == TextType.TYPE_THREE) {
            this.sendBytesData(new byte[]{0x1C, 0x2e});
        } else if (type == TextType.TYPE_FOUR) {
            this.sendBytesData(new byte[]{0x1C, 0x26});
        }
        // 上划线
        if (Shx == true) {
            this.sendBytesData(new byte[]{0x1b, 0x2d, (byte) 1});
        } else {
            this.sendBytesData(new byte[]{0x1b, 0x2d, (byte) 0});
        }
        // 下划线
        if (Xhx == true) {
            this.sendBytesData(new byte[]{0x1b, 0x2e, (byte) 1});
        } else {
            this.sendBytesData(new byte[]{0x1b, 0x2e, (byte) 0});
        }
        // 黑体
        if (Bold == true) {
            this.sendBytesData(new byte[]{0x1b, 0x21, (byte) 0x08});
        } else {
            this.sendBytesData(new byte[]{0x1b, 0x21, (byte) 0x00});
        }
        // 泛白
        if (Fanbai == true) {
            this.sendBytesData(new byte[]{0x1d, 0x42, (byte) 1});
        } else {
            this.sendBytesData(new byte[]{0x1d, 0x42, (byte) 0});
        }
        if (Bk < 0) {
            Bk = 1;
        }
        if (Bk > 8) {
            Bk = 8;
        }
        this.sendBytesData(new byte[]{0x1b, 0x55, (byte) Bk});
        if (Bg < 0) {
            Bg = 1;
        }
        if (Bg > 8) {
            Bg = 8;
        }
        this.sendBytesData(new byte[]{0x1b, 0x56, (byte) Bg});
    }

    /**
     * @param type    一维码类型
     * @param content
     */
    public void printBarcode(byte type, String content) {
        byte[] res = new byte[100];
        int i = 0;
        char k;
        res[i++] = 0x1d;
        res[i++] = 0x6b;
        res[i++] = type;
        if (type == MyBarCode.UPC_A) {
            char[] len = content.toCharArray();
            k = (char) content.length();
            if (k >= 12) {
                k = 12;
                res[i++] = (byte) 12;
                for (int j = 0; j < k; j++) {
                    if (len[j] < 48 && len[j] > 57) {
                        return;
                    }
                    res[i++] = (byte) len[j];

                }
            } else if (k == 11) {
                res[i++] = (byte) 11;
                for (int j = 0; j < k; j++) {
                    if (len[j] < 48 && len[j] > 57) {
                        return;
                    }
                    res[i++] = (byte) len[j];
                }
            } else {
                return;
            }

        }

        if (type == MyBarCode.UPC_E) {
            char[] len = content.toCharArray();
            k = (char) content.length();
            if (k >= 8) {
                k = 8;
                res[i++] = (byte) 8;
                byte buffer[] = new byte[7];
                for (int j = 0; j < k - 1; j++) {
                    if (len[j] < 48 && len[j] > 57) {
                        return;
                    }
                    buffer[j] = (byte) len[j];// 计算校验和

                }

                if (buffer[0] != 0x30) {
                    return;
                }
                for (int j = 0; j < k - 1; j++) {
                    res[i++] = (byte) buffer[j];
                }
                byte upcef = (byte) (10 - (((buffer[0] + buffer[2] + buffer[4] + buffer[6])
                        * 3 + buffer[1] + buffer[3] + buffer[5]) % 10));
                res[i++] = (byte) (upcef + 0x30);
            } else {
                return;
            }

        }
        if (type == MyBarCode.JAN13) {
            char[] len = content.toCharArray();
            k = (char) content.length();
            if (k > 13) {
                k = 13;
                res[i++] = (byte) 13;
                for (int j = 0; j < k; j++) {
                    if (len[j] < 48 && len[j] > 57) {
                        return;
                    }
                    res[i++] = (byte) len[j];

                }
            } else if (k == 12) {
                res[i++] = (byte) 12;
                for (int j = 0; j < k; j++) {
                    if (len[j] < 48 && len[j] > 57) {

                        return;
                    }
                    res[i++] = (byte) len[j];
                }
            } else {
                return;
            }

        }
        if (type == MyBarCode.JAN8) {
            char[] len = content.toCharArray();
            k = (char) content.length();
            if (k > 8) {
                k = 8;
                res[i++] = (byte) 8;
                for (int j = 0; j < k; j++) {
                    if (len[j] < 48 && len[j] > 57) {
                        return;
                    }
                    res[i++] = (byte) len[j];

                }
            } else if (k == 7) {
                res[i++] = (byte) 7;
                for (int j = 0; j < k; j++) {
                    if (len[j] < 48 && len[j] > 57) {
                        return;
                    }
                    res[i++] = (byte) len[j];
                }
            } else {
                return;
            }

        }
        if (type == MyBarCode.CODE39) {
            char[] len = content.toCharArray();
            k = (char) content.length();
            if (k > 0 && k < 100) {
                res[i++] = (byte) k;
                for (int j = 0; j < k; j++) {
                    if (((len[j] > 44) && (len[j] < 58))
                            || ((len[j] > 64) && (len[j] < 91)) || len[j] == 36
                            || len[j] == 37 || len[j] == 43 || len[j] == 32) {
                        res[i++] = (byte) len[j];
                    } else {
                        return;
                    }
                }
            } else {
                return;
            }

        }
        if (type == MyBarCode.CODABAR) {
            char[] len = content.toCharArray();
            k = (char) content.length();
            res[i++] = (byte) k;
            if (k > 0 && k < 100) {
                for (int j = 0; j < k; j++) {
                    if ((len[j] > 44 && len[j] < 58)
                            || (len[j] > 64 && len[j] < 91) || len[j] == 36
                            || len[j] == 37 || len[j] == 43 || len[j] == 32) {
                        res[i++] = (byte) len[j];
                    } else {
                        return;
                    }
                }
            } else {
                return;
            }

        }
        if (type == MyBarCode.ITF) {
            char[] len = content.toCharArray();
            k = (char) content.length();
            if (k % 2 == 0) {
                res[i++] = (byte) k;
                for (int j = 0; j < k; j++) {
                    if (len[j] < 48 && len[j] > 57) {
                        return;
                    }
                    res[i++] = (byte) len[j];

                }
            } else {
                return;
            }
        }
        if (type == MyBarCode.CODE93) {
            char[] len = content.toCharArray();
            k = (char) content.length();
            res[i++] = (byte) k;
            for (int j = 0; j < k; j++) {
                if (len[j] < 0 && len[j] > 127) {
                    return;
                }
                res[i++] = (byte) len[j];

            }
        }
        if (type == MyBarCode.CODE128) {
            char[] len = content.toCharArray();
            k = (char) content.length();

            res[i++] = (byte) (k + 2);
            res[i++] = (byte) 0x7b;
            res[i++] = (byte) 0x42;

            for (int j = 0; j < k; j++) {
                if (len[j] < 0 && len[j] > 127) {
                    return;
                }
                res[i++] = (byte) len[j];

            }
        }
        this.sendBytesData(res);
    }

    /**
     * 条形码打印
     *
     * @param codeWidth  宽度
     * @param codeHeight 高度
     * @param number     数字
     * @param start      起始位置
     */
    public void codebar_init(int codeWidth, int codeHeight, int number,
                             int start) { // '条码打印测试 ----初始化
        // '打印机初始化命令
        byte[] res = new byte[100];
        // '设置条码高度为20MM（值1-255）
        int i = 0;
        res[i++] = 0x1D;
        res[i++] = 0x68;
        if (codeHeight < 0) {
            codeHeight = 0;
        }
        if (codeHeight > 0x80) {
            codeHeight = 0x80;
        }
        res[i++] = (byte) codeHeight;
        // '设置条码宽度为2，越大条码的密度越低，易识别（值1-4）
        res[i++] = 0x1d;
        res[i++] = 0x77;
        if (codeWidth < 1) {
            codeWidth = 1;
        }
        if (codeWidth > 4) {
            codeWidth = 4;
        }
        res[i++] = (byte) codeWidth;
        // '设置条码字符位置在条码下方（值0-2）
        res[i++] = 0x1d;
        res[i++] = 0x48;
        if (number < 0) {
            number = 0;
        }
        if (number > 2) {
            number = 2;
        }
        res[i++] = (byte) number;
        // '设置条码水平位置，为最左侧
        res[i++] = 0x1d;
        res[i++] = 0x51;
        res[i++] = (byte) (start % 256);
        res[i++] = (byte) (start / 256);
        this.sendBytesData(res);
    }


    // 进纸n行
    public void Formfeed(int number) {
        this.sendBytesData(new byte[]{0x1b, 0x64, (byte) number});
    }

    // 是否反向打印************************************************************
    public void Reverse(boolean reverse) {
        if (reverse == true) {
            this.sendBytesData(new byte[]{0x1b, 0x63, 0});
        } else {
            this.sendBytesData(new byte[]{0x1b, 0x63, 0x01});
        }
    }

    // 放大倍数
    public void Magnify(int magnify) {
        this.sendBytesData(new byte[]{0x1b, 0x57, (byte) magnify});
    }

    // 标签打印走到下一行
    public void Label(boolean isLabel) {
        if (isLabel == true) {
            this.sendBytesData(new byte[]{0x1b, 0x2f, 0, 0x1b, 0x69});
        } else {
            this.sendBytesData(new byte[]{0x1b, 0x2f, 1});
        }
    }

    // 打印深度调节
    public void setDepth(int res, int depth) {
        if (res == 1) {// 加强深度
            if (depth < 0) {
                depth = 0;
            }
            if (depth > 255) {
                depth = 255;
            }
            this.sendBytesData(new byte[]{0x1b, 0x73, 0x2b, (byte) depth});
        } else if (res == 0) {// 减少深度
            if (depth < 0) {
                depth = 0;
            }
            if (depth > 255) {
                depth = 255;
            }
            this.sendBytesData(new byte[]{0x1b, 0x73, 0x2d, (byte) depth});
        } else {
            this.sendBytesData(new byte[]{0x1b, 0x73, 0x2b, (byte) 0});
            this.sendBytesData(new byte[]{0x1b, 0x73, 0x2d, (byte) 0});
        }

    }

    // 国际字符集
    public void setInternational(int international) {
        if (international < 0) {
            international = 0;
        }
        if (international > 15) {
            international = 15;
        }
        this.sendBytesData(new byte[]{0x1c, 0x2e, 0x1d, 0x74, 03, 0x1b, 0x52,
                (byte) international});
    }

    // 直线
    public void setisLine() {
        for (int i = 0; i < 32; i++) {
            this.sendBytesData(new byte[]{(byte) 0xc4, (byte) 0x00});

        }
        byte[] cr = new byte[1];
        cr[0] = (byte) 0x0d;
        this.sendBytesData(cr);
    }

    public void setisnoLine() {
        for (int i = 0; i < 32; i++) {
            this.printText("-");
        }
        byte[] cr = new byte[1];
        cr[0] = (byte) 0x0d;
        this.sendBytesData(cr);
    }

}
