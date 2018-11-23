package mx.digitalcoaster.bbva_ingenico.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by rzertuche on 6/22/16.
 */
public class Circle2 extends View {

    private static final int START_ANGLE_POINT = 90;

    private Paint paint;
    private RectF rect;

    private float angle;
    private float size;

    public Circle2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(rect, START_ANGLE_POINT, angle, false, paint);
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void setSize(float size){
        this.size = size;

        final int strokeWidth = 14;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        //Circle color
        paint.setColor(Color.rgb(42, 134, 202));

        rect = new RectF(strokeWidth, strokeWidth, size - strokeWidth/2, size - strokeWidth/2);

        angle = 360;
    }
}