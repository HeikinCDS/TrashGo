package com.pinduoduo.trashgo.util;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageUtils {
    private ImageUtils() {
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

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

    public static String toBase64(byte[] imageBytes) {
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    public static String processImage(Bitmap bitmap) {
        Bitmap resizedBitmap =
                resizeBitmap(bitmap, 768);

        byte[] jpegBytes =
                compressToJpeg(resizedBitmap, 70);

        return toBase64(jpegBytes);
    }
}
