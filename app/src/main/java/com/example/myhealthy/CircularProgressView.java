package com.example.myhealthy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CircularProgressView extends View {

    private Paint bgPaint, progressPaint, textPaint, labelPaint;
    private RectF arcRect;

    private int progress = 0;
    private int maxProgress = 100;
    private String label = "";
    private String unit = "";
    private int progressColor = 0xFF008B02;

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(dp(8));
        bgPaint.setStrokeCap(Paint.Cap.ROUND);
        bgPaint.setColor(0xFFE0E0E0);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(dp(8));
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(progressColor);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(0xFF222222);
        textPaint.setTextSize(dp(18));
        textPaint.setFakeBoldText(true);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(0xFF999999);
        labelPaint.setTextSize(dp(10));

        arcRect = new RectF();
    }

    public void setProgress(int progress, int max) {
        this.progress = Math.min(progress, max);
        this.maxProgress = max;
        invalidate();
    }

    public void setLabel(String label) {
        this.label = label;
        invalidate();
    }

    public void setUnit(String unit) {
        this.unit = unit;
        invalidate();
    }

    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float pad = dp(12);
        float size = Math.min(getWidth(), getHeight());
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float half = size / 2f - pad;

        arcRect.set(cx - half, cy - half, cx + half, cy + half);

        // Background arc
        canvas.drawArc(arcRect, -90, 360, false, bgPaint);

        // Progress arc
        float sweep = maxProgress > 0 ? (progress * 360f / maxProgress) : 0;
        canvas.drawArc(arcRect, -90, sweep, false, progressPaint);

        // Center text (value)
        String valueText = String.valueOf(progress);
        canvas.drawText(valueText, cx, cy + dp(4), textPaint);

        // Label below
        if (!label.isEmpty()) {
            canvas.drawText(label, cx, cy + dp(18), labelPaint);
        }

        // Unit above
        if (!unit.isEmpty()) {
            Paint unitPaint = new Paint(labelPaint);
            unitPaint.setTextSize(dp(9));
            canvas.drawText(unit, cx, cy - dp(14), unitPaint);
        }
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
