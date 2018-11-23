package mx.digitalcoaster.bbva_ingenico.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FingerDrawingPad extends View {
    
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private TextView hintTxt;
    private boolean moved = false;
    private List<PointF> pointList;
   
    
	public FingerDrawingPad(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mPaint = new Paint ();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF072146);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(2);
		
		mBitmap = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas (mBitmap);
        mPath = new Path ();

        pointList = new ArrayList<PointF> ();
	}

	public void setHintTxt(TextView hintTxt){
		this.hintTxt = hintTxt;
	}
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawColor(0xFFAAAAAA);
        
//        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        canvas.drawPath(mPath, mPaint);
        if(pointList.size() > 0){
        	for(PointF pointF:pointList){
        		canvas.drawPoint(pointF.x, pointF.y, mPaint);
        	}
        }
    }
    
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    
    private void touch_start(float x, float y) {
//        mPath.reset();
    	moved = false;
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        if(hintTxt != null){
        	hintTxt.setVisibility(View.GONE);
        }
        if(this.isDrawingCacheEnabled()){
        	this.setDrawingCacheEnabled(false);
        }
    }
    private void touch_move(float x, float y) {
    	moved = true;
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }
    private void touch_up() {
	    mPath.lineTo(mX, mY);
	       // commit the path to our offscreen
	    mCanvas.drawPath(mPath, mPaint);
	    if(!moved){
	    	PointF pointF = new PointF (mX, mY);
	    	pointList.add(pointF);
	    }
        // kill this so we don't double draw
//        mPath.reset();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }
	public void clearCanvas(){
		mPath = new Path ();
		pointList.clear();
		invalidate();
		if(hintTxt != null){
			hintTxt.setVisibility(View.VISIBLE);
		}
	}
}
