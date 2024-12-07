package com.example.geominder.utils;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;


import com.caverock.androidsvg.SVG;


import java.io.InputStream;


public class BitmapUtils {


    public static Bitmap createCustomMarkerIcon(InputStream svgInputStream, String name, float density, int iconWidth, int iconHeight) throws Exception {
        // Load the SVG from input stream
        SVG svg = SVG.getFromInputStream(svgInputStream);


        // Render the SVG to a Picture
        Picture picture = svg.renderToPicture();
        Bitmap bitmap = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(picture);


        // Resize the bitmap to fit the desired icon size
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, false);


        // Measure the text height based on the name length
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(12 * density);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float textWidth = textPaint.measureText(name);
        float textHeight = textPaint.descent() - textPaint.ascent();


        // Create a new bitmap to combine the icon and the name with background
        Bitmap combinedBitmap = Bitmap.createBitmap(
                (int) Math.max(iconWidth, textWidth + 16),  // Added extra space for padding around the text
                (int) (iconHeight + textHeight + 20), Bitmap.Config.ARGB_8888);


        Canvas combinedCanvas = new Canvas(combinedBitmap);


        // Set background color for the text
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        float backgroundPadding = 8;


        // Draw the background rectangle behind the text
        combinedCanvas.drawRect(
                (combinedBitmap.getWidth() - textWidth) / 2 - backgroundPadding,
                5,
                (combinedBitmap.getWidth() + textWidth) / 2 + backgroundPadding,
                textHeight + 10,
                backgroundPaint);


        // Draw the text above the icon (with some padding)
        combinedCanvas.drawText(name, combinedBitmap.getWidth() / 2, textHeight + 5, textPaint);


        // Draw the resized marker icon below the name
        combinedCanvas.drawBitmap(resizedBitmap, (combinedBitmap.getWidth() - iconWidth) / 2, textHeight + 10, null);


        return combinedBitmap;
    }
}



