package com.embrwave.embrwave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

public class MySeekBar extends android.support.v7.widget.AppCompatSeekBar
{
    private Paint paint = new Paint();
    public MySeekBar (Context context) {
        super(context);

        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
    }

    public MySeekBar (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MySeekBar (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int thumb_x = (int) (( (double)this.getProgress()/this.getMax() ) * (double)(this.getWidth() - 2 * 40));
        float middle = (float) (this.getHeight() + 5);

        Log.d("Seekbar", String.valueOf(this.getHeight()));
        Log.d("Seekbar", String.valueOf(this.getWidth()));

        c.drawText(""+(this.getProgress() - 9), thumb_x, middle, paint);
    }
}
