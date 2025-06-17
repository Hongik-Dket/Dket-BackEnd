package com.example.demo.global.infra.image;

import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QrCodeGenerator {

    public MultipartFile generateQrCodeFile(Long ticketId) {
        BufferedImage qrImage = generateQrCodeImage("%s".formatted(ticketId), 300, 300);
        byte[] qrBytes = bufferedImageToBytes(qrImage, "png");

        return new ByteArrayMultipartFile(qrBytes, "qrcode.png", "image/png");
    }

    private static BufferedImage generateQrCodeImage(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            throw new CustomException(ErrorStatus.IMAGE_GENERATE_QRCODE_FAILED);
        }
    }

    private static byte[] bufferedImageToBytes(BufferedImage image, String format) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorStatus.IMAGE_CONVERT_FAILED);
        }

    }
}
