package com.nyoka.soccer_442;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

/**
 * Draws a soccer pitch (grass + markings) as the lineup rows container's background.
 * A Drawable, not a View - a plain custom View here needs an explicit height (e.g.
 * match_parent), which is circular when its parent is wrap_content (sized by content,
 * i.e. the row chips). A background Drawable just paints within whatever bounds its
 * host view ends up with, so the rows container can size itself from its own content
 * with no competing sibling to fight over the measurement pass.
 */
public class PitchDrawable extends Drawable {
    private final Paint grassPaint = new Paint();
    private final Paint stripePaint = new Paint();
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PitchDrawable() {
        grassPaint.setColor(Color.parseColor("#2E7D32"));
        stripePaint.setColor(Color.parseColor("#33FFFFFF"));
        linePaint.setColor(Color.parseColor("#80FFFFFF"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect b = getBounds();
        int w = b.width();
        int h = b.height();
        canvas.drawRect(b, grassPaint);

        int stripes = 8;
        float stripeHeight = h / (float) stripes;
        for (int i = 0; i < stripes; i += 2) {
            canvas.drawRect(b.left, b.top + i * stripeHeight, b.right, b.top + (i + 1) * stripeHeight, stripePaint);
        }

        float margin = 12f;
        canvas.drawRect(b.left + margin, b.top + margin, b.right - margin, b.bottom - margin, linePaint);
        canvas.drawLine(b.left + margin, b.top + h / 2f, b.right - margin, b.top + h / 2f, linePaint);
        float radius = Math.min(w, h) * 0.11f;
        canvas.drawCircle(b.left + w / 2f, b.top + h / 2f, radius, linePaint);

        float boxWidth = w * 0.6f;
        float boxHeight = h * 0.12f;
        canvas.drawRect(b.left + (w - boxWidth) / 2f, b.top + margin, b.left + (w + boxWidth) / 2f, b.top + margin + boxHeight, linePaint);
        canvas.drawRect(b.left + (w - boxWidth) / 2f, b.bottom - margin - boxHeight, b.left + (w + boxWidth) / 2f, b.bottom - margin, linePaint);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
