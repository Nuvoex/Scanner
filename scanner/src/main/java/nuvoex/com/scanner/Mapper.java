package nuvoex.com.scanner;

/**
 * Created by navratansoni on 02/01/17.
 */
public class Mapper {
    public static com.google.zxing.BarcodeFormat getZxingBarcodeFor(BarcodeFormat format) {
        switch (format) {
            case AZTEC:
                return com.google.zxing.BarcodeFormat.AZTEC;

            case CODABAR:
                return com.google.zxing.BarcodeFormat.CODABAR;

            case CODE_39:
                return com.google.zxing.BarcodeFormat.CODE_39;

            case CODE_93:
                return com.google.zxing.BarcodeFormat.CODE_93;

            case CODE_128:
                return com.google.zxing.BarcodeFormat.CODE_128;

            case DATA_MATRIX:
                return com.google.zxing.BarcodeFormat.DATA_MATRIX;

            case EAN_8:
                return com.google.zxing.BarcodeFormat.EAN_8;

            case EAN_13:
                return com.google.zxing.BarcodeFormat.EAN_13;

            case ITF:
                return com.google.zxing.BarcodeFormat.ITF;

            case MAXICODE:
                return com.google.zxing.BarcodeFormat.MAXICODE;

            case PDF_417:
                return com.google.zxing.BarcodeFormat.PDF_417;

            case QR_CODE:
                return com.google.zxing.BarcodeFormat.QR_CODE;

            case RSS_14:
                return com.google.zxing.BarcodeFormat.RSS_14;

            case RSS_EXPANDED:
                return com.google.zxing.BarcodeFormat.RSS_EXPANDED;

            case UPC_A:
                return com.google.zxing.BarcodeFormat.UPC_A;

            case UPC_E:
                return com.google.zxing.BarcodeFormat.UPC_E;

            case UPC_EAN_EXTENSION:
                return com.google.zxing.BarcodeFormat.UPC_EAN_EXTENSION;

            default:
                return null;
        }
    }
}
