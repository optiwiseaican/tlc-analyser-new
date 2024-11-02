package com.aican.tlcanalyzer.customClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.aican.tlcanalyzer.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

public class DrawingImageView extends androidx.appcompat.widget.AppCompatImageView {
    private Paint linePaint;
    private float lineX = -1;

    public DrawingImageView(Context context) {
        super(context);
        init();
    }

    public DrawingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(getResources().getColor(R.color.your_line_color));
        linePaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.your_line_width));
    }

    public void setLineX(float lineX) {
        this.lineX = lineX;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (lineX != -1) {
            canvas.drawLine(lineX, 0, lineX, canvas.getHeight(), linePaint);
        }
    }
}
