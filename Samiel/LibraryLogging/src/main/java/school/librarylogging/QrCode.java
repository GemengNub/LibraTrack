package school.librarylogging;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;

public class QrCode {
    // Class for handing of QR Codes
    public void QrReader(int width, int height, String filePath) throws WriterException, IOException, NotFoundException {
        if(width == 0 || height == 0){
            width = 250;
            height = 250;
        }
        String contents = ""; // combine different data/columns of the DB to one string line
        BitMatrix bit = new QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, width, height);
    }
}
