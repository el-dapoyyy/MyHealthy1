package com.example.myhealthy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class WeeklyBarChartView extends View {

    private Paint barPaint, barBgPaint, textPaint, valuePaint;
    private final String[] dayLabels = {"Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"};
    private final int[] values = new int[7];
    private int maxValue = 2500;

    public WeeklyBarChartView(Context context) {
        super(context);
        init();
    }

    public WeeklyBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeeklyBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(0xFF00FF85);

        barBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barBgPaint.setColor(0xFF0B3A1C);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFA9B5AC);
        textPaint.setTextSize(dp(10));
        textPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(0xFFFFFFFF);
        valuePaint.setTextSize(dp(8));
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);
    }

    public void setData(int[] data, int max) {
        this.maxValue = max > 0 ? max : 2500;
        for (int i = 0; i < 7; i++) {
            values[i] = i < data.length ? data[i] : 0;
        }
        invalidate();
    }

    public void setDayLabels(String[] labels) {
        for (int i = 0; i < 7 && i < labels.length; i++) {
            dayLabels[i] = labels[i];
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float barAreaH = h - dp(32); // space for labels + value text
        float topPad = dp(14);
        float bottomLabelY = h - dp(4);

        float gap = dp(8);
        float totalGap = gap * 8; // gaps between + sides
        float barW = (w - totalGap) / 7f;

        for (int i = 0; i < 7; i++) {
            float left = gap + i * (barW + gap);
            float cx = left + barW / 2f;

            // Background bar
            float barTop = topPad;
            float barBottom = topPad + barAreaH;
            RectF bgRect = new RectF(left, barTop, left + barW, barBottom);
            canvas.drawRoundRect(bgRect, dp(4), dp(4), barBgPaint);

            // Value bar
            float ratio = maxValue > 0 ? Math.min((float) values[i] / maxValue, 1f) : 0;
            float valTop = barBottom - (barAreaH * ratio);
            RectF valRect = new RectF(left, valTop, left + barW, barBottom);
            canvas.drawRoundRect(valRect, dp(4), dp(4), barPaint);

            // Value text above bar
            if (values[i] > 0) {
                canvas.drawText(String.valueOf(values[i]), cx, valTop - dp(3), valuePaint);
            }

            // Day label
            canvas.drawText(dayLabels[i], cx, bottomLabelY, textPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = dp(160);
        setMeasuredDimension(w, h);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
