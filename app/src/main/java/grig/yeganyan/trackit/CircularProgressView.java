package grig.yeganyan.trackit;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Custom view that draws a circular progress ring matching the design in the screenshot.
 * Progress range: 0.0f (empty) → 1.0f (full).
 */
public class CircularProgressView extends View {

    private Paint trackPaint;   // background ring
    private Paint fillPaint;    // progress arc
    private RectF arcRect;

    private float progress = 0f;          // 0.0 – 1.0
    private float displayedProgress = 0f; // animated value

    private static final float STROKE_WIDTH_DP = 18f;
    private static final int   TRACK_COLOR     = 0xFFFFFFFF;      // white ring
    private static final int   FILL_COLOR      = 0xFF2D6A4F;      // forest green
    private static final float START_ANGLE     = -90f;            // top

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float strokePx = dpToPx(STROKE_WIDTH_DP);

        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokePx);
        trackPaint.setColor(TRACK_COLOR);
        // Soft shadow for the white ring
        trackPaint.setShadowLayer(dpToPx(8), 0, dpToPx(4), 0x22000000);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.STROKE);
        fillPaint.setStrokeWidth(strokePx);
        fillPaint.setColor(FILL_COLOR);
        fillPaint.setStrokeCap(Paint.Cap.ROUND);

        arcRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        float strokePx = dpToPx(STROKE_WIDTH_DP);
        float inset = strokePx / 2f + dpToPx(4);
        arcRect.set(inset, inset, w - inset, h - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Full ring (track)
        canvas.drawArc(arcRect, 0, 360, false, trackPaint);

        // Progress arc
        if (displayedProgress > 0f) {
            float sweep = 360f * displayedProgress;
            canvas.drawArc(arcRect, START_ANGLE, sweep, false, fillPaint);
        }
    }



    /** Set progress instantly (no animation). */
    public void setProgress(float progress) {
        this.progress = clamp(progress);
        this.displayedProgress = this.progress;
        invalidate();
    }


    public void animateProgress(float targetProgress) {
        this.progress = clamp(targetProgress);
        ValueAnimator animator = ValueAnimator.ofFloat(displayedProgress, this.progress);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(va -> {
            displayedProgress = (float) va.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }



    private float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
