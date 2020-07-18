package com.example.paras.emotionrecognizer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.FontsContract;

import com.microsoft.projectoxford.emotion.contract.FaceRectangle;

/**
 * Created by Paras on 19-08-2017.
 */

public class ImageHelper {

    public static Bitmap drawRectOnBitmap1(Bitmap mbitmap, FaceRectangle faceRectangle,String status){

        Bitmap bitmap = mbitmap.copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.TRANSPARENT);
        paint.setStrokeWidth(5);

       canvas.drawRect(faceRectangle.left,faceRectangle.top,faceRectangle.width,faceRectangle.height,paint);

        int cX = faceRectangle.left + faceRectangle.width;
        int cY = faceRectangle.top + faceRectangle.height;

        drawTextOnBitmap(canvas,70,/*cX/2+cX/5*/cX/8+cX/10,cY+50/*cY+70*/,Color.RED,status);
        
        return bitmap;

    }

    private static void drawTextOnBitmap(Canvas canvas, int textSize, int cX, int cY, int color, String status) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(textSize);

        canvas.drawText(status,cX,cY,paint);

    }
}