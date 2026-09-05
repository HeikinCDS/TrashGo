package com.pinduoduo.trashgo.ui.tour;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.pinduoduo.trashgo.R;

public class TourOverlayView extends View {
    private static final int SCRIM = 0xC4000000;

    private final Paint scrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint holePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF hole = new RectF();
    private float cornerRadius;
    private boolean hasHole = false;

    public TourOverlayView(Context context) {
        this(context, null);
    }

    public TourOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        scrimPaint.setColor(SCRIM);

        holePaint.setColor(Color.TRANSPARENT);
        holePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(2f * getResources().getDisplayMetrics().density);
        ringPaint.setColor(ContextCompat.getColor(context, R.color.trashgo_primary));

        cornerRadius = 14f * getResources().getDisplayMetrics().density;
    }

    public void setHole(float left, float top, float right, float bottom, float radiusPx) {
        hole.set(left, top, right, bottom);
        cornerRadius = radiusPx;
        hasHole = true;
        invalidate();
    }

    public void clearHole() {
        hasHole = false;
        invalidate();
    }

    @NonNull
    public RectF holeRect() {
        return hole;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), scrimPaint);
        if (hasHole) {
            canvas.drawRoundRect(hole, cornerRadius, cornerRadius, holePaint);
            canvas.drawRoundRect(hole, cornerRadius, cornerRadius, ringPaint);
        }
    }
}
