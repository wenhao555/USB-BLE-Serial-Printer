package android_serialport_api;

/**
 * Created by Administrator on 2017/1/18.
 */

public class PrinterConstant {
    public PrinterConstant() {
    }

    public static class BarcodeType {
        public static final byte UPC_A = 0;
        public static final byte UPC_E = 1;
        public static final byte JAN13 = 2;
        public static final byte JAN8 = 3;
        public static final byte CODE39 = 4;
        public static final byte ITF = 5;
        public static final byte CODABAR = 6;
        public static final byte CODE93 = 72;
        public static final byte CODE128 = 73;
        public static final byte PDF417 = 100;
        public static final byte DATAMATRIX = 101;
        public static final byte QRCODE = 102;

        public BarcodeType() {
        }
    }

    public static class Command {
        public static final int INIT_PRINTER = 0;
        public static final int WAKE_PRINTER = 1;
        public static final int PRINT_AND_RETURN_STANDARD = 2;
        public static final int PRINT_AND_NEWLINE = 3;
        public static final int PRINT_AND_ENTER = 4;
        public static final int MOVE_NEXT_TAB_POSITION = 5;
        public static final int DEF_LINE_SPACING = 6;
        public static final int PRINT_AND_WAKE_PAPER_BY_LNCH = 0;
        public static final int PRINT_AND_WAKE_PAPER_BY_LINE = 1;
        public static final int CLOCKWISE_ROTATE_90 = 4;
        public static final int ALIGN = 13;
        public static final int ALIGN_LEFT = 0;
        public static final int ALIGN_CENTER = 1;
        public static final int ALIGN_RIGHT = 2;
        public static final int LINE_HEIGHT = 10;
        public static final int CHARACTER_RIGHT_MARGIN = 11;
        public static final int UNDERLINE = 15;
        public static final int UNDERLINE_OFF = 16;
        public static final int UNDERLINE_ONE_DOTE = 17;
        public static final int UNDERLINE_TWO_DOTE = 18;
        public static final int FONT_MODE = 16;
        public static final int FONT_SIZE = 17;

        public Command() {
        }
    }

    public static class Connect {
        public static final int SUCCESS = 101;
        public static final int FAILED = 102;
        public static final int CLOSED = 103;
        public static final int NODEVICE = 104;

        public Connect() {
        }
    }

    public static enum LableFontSize {
        Size_16,
        Size_24,
        Size_32,
        Size_48,
        Size_64,
        Size_72,
        Size_96;

        private LableFontSize() {
        }
    }

    public static enum LablePaperType {
        Size_58mm,
        Size_80mm,
        Size_100mm;

        private LablePaperType() {
        }
    }

    public static enum PAlign {
        START,
        CENTER,
        END,
        NONE;

        private PAlign() {
        }
    }

    public static enum PBarcodeType {
        JAN3_EAN13,
        JAN8_EAN8,
        CODE39,
        CODE93,
        CODE128,
        CODABAR,
        ITF,
        UPC_A,
        UPC_E,
        EAN13Plus2,
        EAN13Plus5,
        EAN8Plus2,
        EAN8Plus5,
        UPCAPlus2,
        UPCAPlus5,
        UPCEPlus2,
        UPCEPlus5,
        Postnet,
        MSI,
        QR;

        private PBarcodeType() {
        }
    }

    public static enum PRotate {
        Rotate_0,
        Rotate_90,
        Rotate_180,
        Rotate_270;

        private PRotate() {
        }
    }

    public static enum PrinterType {
        TIII,
        T7,
        T9,
        POS76;

        private PrinterType() {
        }
    }
}
