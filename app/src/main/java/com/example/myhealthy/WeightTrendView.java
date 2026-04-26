package com.example.myhealthy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WeightTrendView extends View {

    private Paint linePaint, dotPaint, textPaint, labelPaint, gridPaint;
    private final List<Float> weights = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();

    public WeightTrendView(Context context) {
        super(context);
        init();
    }

    public WeightTrendView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeightTrendView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xFF00FF85);
        linePaint.setStrokeWidth(dp(3));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(0xFF00FF85);
        dotPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(dp(10));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(0xFFA9B5AC);
        labelPaint.setTextSize(dp(9));
        labelPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(0xFF0B3A1C);
        gridPaint.setStrokeWidth(dp(1));
    }

    public void setData(List<Float> weights, List<String> labels) {
        this.weights.clear();
        this.labels.clear();
        this.weights.addAll(weights);
        this.labels.addAll(labels);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (weights.isEmpty()) {
            Paint emptyPaint = new Paint(labelPaint);
            emptyPaint.setTextSize(dp(12));
            canvas.drawText("Belum ada data berat badan", getWidth() / 2f, getHeight() / 2f, emptyPaint);
            return;
        }

        float w = getWidth();
        float h = getHeight();
        float padL = dp(16);
        float padR = dp(16);
        float padT = dp(24);
        float padB = dp(24);

        float chartW = w - padL - padR;
        float chartH = h - padT - padB;

        // Find min/max
        float minW = Float.MAX_VALUE, maxW = Float.MIN_VALUE;
        for (float v : weights) {
            minW = Math.min(minW, v);
            maxW = Math.max(maxW, v);
        }
        float range = maxW - minW;
        if (range < 1f) range = 2f; // Prevent division by zero

        // Add padding to range
        minW -= range * 0.15f;
        maxW += range * 0.15f;
        range = maxW - minW;

        int n = weights.size();
        float stepX = n > 1 ? chartW / (n - 1) : 0;

        Path path = new Path();
        float[] xPoints = new float[n];
        float[] yPoints = new float[n];

        for (int i = 0; i < n; i++) {
            float x = padL + (i * stepX);
            float y = padT + chartH - ((weights.get(i) - minW) / range * chartH);
            xPoints[i] = x;
            yPoints[i] = y;

            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }

        // Draw line
        canvas.drawPath(path, linePaint);

        // Draw dots, values, labels
        for (int i = 0; i < n; i++) {
            canvas.drawCircle(xPoints[i], yPoints[i], dp(5), dotPaint);

            // Weight value above dot
            String valText = String.format("%.1f", weights.get(i));
            canvas.drawText(valText, xPoints[i], yPoints[i] - dp(10), textPaint);

            // Date label below
            if (i < labels.size()) {
                canvas.drawText(labels.get(i), xPoints[i], h - dp(4), labelPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = dp(140);
        setMeasuredDimension(w, h);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
