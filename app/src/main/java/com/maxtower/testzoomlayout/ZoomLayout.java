package com.maxtower.testzoomlayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by max on 2/13/17.
 */

public class ZoomLayout extends ViewGroup
    {

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetectorCompat mGestureDetector;
    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 4.0f;
    private float scale = 1f;
    // Parameters for zooming.
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float distanceX = 0f;
    private float distanceY = 0f;


    private RectF contentSize;


    private float[] mDispatchTouchEventWorkingArray = new float[2];
    private float[] mOnTouchEventWorkingArray = new float[2];


    // Matrices used to move and zoom image.
    private Matrix matrix = new Matrix();
    private Matrix matrixInverse = new Matrix();
    private Matrix savedMatrix = new Matrix();


    public ZoomLayout(Context context)
        {
        super(context);
        init(context);
        }

    public ZoomLayout(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        init(context);
        }

    public ZoomLayout(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        init(context);
        }


    private void init(Context context)
        {
        mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        }



    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
        {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++)
            {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE)
                {
                child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
                }
            }
        }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
        {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++)
            {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE)
                {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                }
            }
        }

    @Override
    protected void dispatchDraw(Canvas canvas)
        {
        float[] values = new float[9];
        matrix.getValues(values);
        canvas.save();
        //Log.i("MATRIX", values[Matrix.MTRANS_X] + "," + values[Matrix.MTRANS_Y] + " " +
        //        values[Matrix.MSCALE_X] + "," + values[Matrix.MSCALE_Y] );
        canvas.translate(values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y]);
        canvas.scale(values[Matrix.MSCALE_X], values[Matrix.MSCALE_Y]);
        super.dispatchDraw(canvas);
        canvas.restore();
        }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
        {
        //mDispatchTouchEventWorkingArray[0] = ev.getX();
        //mDispatchTouchEventWorkingArray[1] = ev.getY();
        //mDispatchTouchEventWorkingArray = screenPointsToScaledPoints(mDispatchTouchEventWorkingArray);
        //ev.setLocation(mDispatchTouchEventWorkingArray[0], mDispatchTouchEventWorkingArray[1]);
        return super.dispatchTouchEvent(ev);
        }

    @Override
    public boolean onTouchEvent(MotionEvent event)
        {
        matrix.set(savedMatrix);
        boolean retVal = mGestureDetector.onTouchEvent(event);
        if(event.getPointerCount() > 1)
            {
            retVal = mScaleGestureDetector.onTouchEvent(event) | retVal;
            if(checkScaleBounds())
                {
                matrix.postScale(scale, scale, mid.x, mid.y);
                }

            }
        matrix.postTranslate(distanceX, distanceY);
        matrix.invert(matrixInverse);
        savedMatrix.set(matrix);
        invalidate();
        return retVal;
        }

        /**
         * The scale listener, used for handling multi-finger scale gestures.
         */
    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener()
        {
        /**
         * This is the active focal point in terms of the viewport. Could be a local
         * variable but kept here to minimize per-frame allocations.
         */

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector)
            {
            oldDist = scaleGestureDetector.getCurrentSpan();
            if (oldDist > 10f)
                {
                savedMatrix.set(matrix);
                mid.set(scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
                }
            return true;
            }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector)
            {

            }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector)
            {
            scale = scaleGestureDetector.getScaleFactor();
            return true;
            }
        };

    /**
     * The gesture listener, used for handling simple gestures such as double touches, scrolls,
     * and flings.
     */
    private final GestureDetector.SimpleOnGestureListener mGestureListener
            = new GestureDetector.SimpleOnGestureListener()
        {
        @Override
        public boolean onDown(MotionEvent event)
            {
            savedMatrix.set(matrix);

            start.set(event.getX(), event.getY());

            return true;
            }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX, float dY)
            {
            setupTranslation(dX, dY);
            return true;
            }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
            {
            //fling((int) -velocityX, (int) -velocityY);
            return true;
            }
        };

    private boolean checkScaleBounds()
        {
        float[] values = new float[9];
        matrix.getValues(values);
        float sx = values[Matrix.MSCALE_X] * scale;
        float sy = values[Matrix.MSCALE_Y] * scale;
        if(sx > MIN_ZOOM && sx < MAX_ZOOM && sy > MIN_ZOOM && sy < MAX_ZOOM)
            {
            return true;
            }
        return false;
        }

    private float[] scaledPointsToScreenPoints(float[] a)
        {
        matrix.mapPoints(a);
        return a;
        }

    private float[] screenPointsToScaledPoints(float[] a)
        {
        matrixInverse.mapPoints(a);
        return a;
        }

    private void setupTranslation(float dX, float dY)
        {
        distanceX = -1 * dX;
        distanceY = -1 * dY;

        if(contentSize != null)
            {
            float[] values = new float[9];
            matrix.getValues(values);
            float totX = values[Matrix.MTRANS_X] + distanceX;
            float totY = values[Matrix.MTRANS_Y] + distanceY;
            float sx = values[Matrix.MSCALE_X];
            Rect viewableRect = new Rect();
            ZoomLayout.this.getDrawingRect(viewableRect);
            float offscreenWidth = contentSize.width() - (viewableRect.right - viewableRect.left);
            float offscreenHeight = contentSize.height() - (viewableRect.bottom - viewableRect.top);
            float maxDx = (contentSize.width() - (contentSize.width() / sx)) / 2 * sx;
            float maxDy = (contentSize.height() - (contentSize.height() / sx)) / 2 * sx;

            Log.i("NZL", offscreenWidth + "," + offscreenHeight + "  " + maxDx + "," + maxDy + "  " + totX + "," + totY);

            if(totX > 0 && distanceX > 0)
                {
                distanceX = 0;
                }
            if(totY > 0 && distanceY > 0)
                {
                distanceY = 0;
                }

            //totX = Math.min(Math.max(totX, -(maxDx + offscreenWidth)), maxDx);
            //totY = Math.min(Math.max(totY, -(maxDy + offscreenHeight)), maxDy);
            if(((int)totX) - ((int)values[Matrix.MTRANS_X] + distanceX) != 0)
                {
               // distanceX = 0;
                }
            if(((int)totY) - ((int)values[Matrix.MTRANS_Y] + distanceY) != 0)
                {
               // distanceY = 0;
                }
            }


        }

    public void setContentSize(float width, float height)
        {
        this.contentSize = new RectF(0, 0, width, height);
        }

    }
