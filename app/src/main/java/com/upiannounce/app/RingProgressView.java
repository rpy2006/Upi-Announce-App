package com.upiannounce.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Lightweight ring progress indicator (no external chart library required).
 * Draws a muted track circle plus a rounded-cap progress arc on top.
 */
public class RingProgressView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private float progressFraction = 0f; // 0f..1f (can exceed 1f if goal is beaten)
    private float strokeWidthDp = 14f;
    private ValueAnimator animator;

    public RingProgressView(Context context) { super(context); init(); }
    public RingProgressView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public RingProgressView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        float strokeWidthPx = strokeWidthDp * density;

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidthPx);
        trackPaint.setColor(Color.parseColor("#17191C"));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidthPx);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(Color.parseColor("#F5F6F7"));
    }

    /** Sets the ring color (e.g. green once a target has been reached). */
    public void setRingColor(int color) {
        progressPaint.setColor(color);
        invalidate();
    }

    /** Animates the ring from its current value to the given fraction (0f..1f+). */
    public void animateProgressTo(float targetFraction) {
        if (animator != null && animator.isRunning()) animator.cancel();
        animator = ValueAnimator.ofFloat(progressFraction, targetFraction);
        animator.setDuration(600);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            progressFraction = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float stroke = trackPaint.getStrokeWidth();
        arcBounds.set(stroke / 2f, stroke / 2f, getWidth() - stroke / 2f, getHeight() - stroke / 2f);

        canvas.drawOval(arcBounds, trackPaint);

        float sweep = Math.min(progressFraction, 1f) * 360f;
        canvas.drawArc(arcBounds, -90f, sweep, false, progressPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) animator.cancel();
    }
}
