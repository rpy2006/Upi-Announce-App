package com.upiannounce.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Draws a simple connected-line chart (no external chart library required).
 * Values are normalized internally against their own min/max.
 */
public class SparklineView extends View {

    private float[] values = new float[0];
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    public SparklineView(Context context) { super(context); init(); }
    public SparklineView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public SparklineView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2.5f * density);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setColor(Color.parseColor("#3D8BFF"));

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(Color.parseColor("#3D8BFF"));
    }

    public void setValues(float[] newValues) {
        this.values = newValues != null ? newValues : new float[0];
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.length < 2) return;

        float density = getResources().getDisplayMetrics().density;
        float padding = 6f * density;
        float w = getWidth();
        float h = getHeight();

        float max = values[0], min = values[0];
        for (float v : values) { if (v > max) max = v; if (v < min) min = v; }
        float range = Math.max(max - min, 1f);

        float stepX = (w - padding * 2) / (values.length - 1);
        path.reset();

        float[] xs = new float[values.length];
        float[] ys = new float[values.length];

        for (int i = 0; i < values.length; i++) {
            float x = padding + stepX * i;
            float normalized = (values[i] - min) / range;
            float y = padding + (1f - normalized) * (h - padding * 2);
            xs[i] = x;
            ys[i] = y;
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }

        canvas.drawPath(path, linePaint);
        for (int i = 0; i < xs.length; i++) {
            canvas.drawCircle(xs[i], ys[i], 3.2f * density, dotPaint);
        }
    }
}
