package com.pinduoduo.trashgo.util;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageUtils {

    private ImageUtils() {
        // Utility class - no instances needed
    }

    /**
     * Resize a bitmap so that its longest side
     * is no larger than maxSize.
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Image is already small enough
        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        float scale;

        if (width > height) {
            scale = (float) maxSize / width;
        } else {
            scale = (float) maxSize / height;
        }

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(
                bitmap,
                newWidth,
                newHeight,
                true
        );
    }

    /**
     * Compress bitmap into JPEG format.
     *
     * @param bitmap Bitmap to compress
     * @param quality JPEG quality from 0 to 100
     * @return JPEG byte array
     */
    public static byte[] compressToJpeg(
            Bitmap bitmap,
            int quality) {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                outputStream
        );

        return outputStream.toByteArray();
    }

    /**
     * Convert image bytes to Base64.
     */
    public static String toBase64(byte[] imageBytes) {
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    /**
     * Resize → JPEG compress → Base64.
     */
    public static String processImage(Bitmap bitmap) {

        Bitmap resizedBitmap =
                resizeBitmap(bitmap, 1024);

        byte[] jpegBytes =
                compressToJpeg(resizedBitmap, 80);

        return toBase64(jpegBytes);
    }
}