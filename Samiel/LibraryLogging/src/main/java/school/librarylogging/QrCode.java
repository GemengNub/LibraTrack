package school.librarylogging;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

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

    public BufferedImage createQrImage(String contents, int size) throws WriterException {
        if (contents == null || contents.isBlank()) {
            throw new IllegalArgumentException("QR contents cannot be empty");
        }
        int targetSize = size > 0 ? size : 250;
        BitMatrix matrix = new QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, targetSize, targetSize);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    public void saveQrImage(BufferedImage image, Path destination) throws IOException {
        if (image == null) {
            throw new IllegalStateException("QR image is not generated");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination path cannot be null");
        }
        Path parent = destination.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        ImageIO.write(image, "PNG", destination.toFile());
    }
}
