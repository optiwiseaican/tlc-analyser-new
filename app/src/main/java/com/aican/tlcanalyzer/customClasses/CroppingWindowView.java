package com.aican.tlcanalyzer.customClasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CroppingWindowView extends View {
    private Paint linePaint;
    private List<RectF> lines;
    private float touchStartX, touchStartY;
    private int handleSize = 30;
    private boolean isResizing = false;

    public CroppingWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.RED);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5);
        lines = new ArrayList<>();
    }

    public void addLine(float x1, float y1, float x2, float y2) {
        lines.add(new RectF(x1, y1, x2, y2));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (RectF line : lines) {
            canvas.drawLine(line.left, line.top, line.right, line.bottom, linePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check if the touch is on a line handle
                isResizing = false;
                for (RectF line : lines) {
                    if (isResizeHandleTouched(line, x, y)) {
                        isResizing = true;
                        touchStartX = x;
                        touchStartY = y;
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isResizing) {
                    resizeLine(x, y);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                isResizing = false;
                break;
        }
        return true;
    }

    private boolean isResizeHandleTouched(RectF line, float x, float y) {
        return (x >= line.right - handleSize && x <= line.right
                && y >= line.bottom - handleSize && y <= line.bottom);
    }

    private void resizeLine(float x, float y) {
        for (RectF line : lines) {
            if (isResizeHandleTouched(line, touchStartX, touchStartY)) {
                line.right = x;
                line.bottom = y;
                break;
            }
        }
    }

    public void clearLines() {
        lines.clear();
        invalidate();
    }

    public List<RectF> getLines() {
        return lines;
    }
}

