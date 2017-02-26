package com.lxy.line.broken.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class LineView extends View {

    private int mDefaultWidth = 100;
    private int mDefaultHeight = 100;

    private Paint mAxisPaint;
    private Paint mAvgLinePaint;
    private Paint mCrossLinePaint;
    private Paint mBgColorPaint;//背景色块画笔

    private float mXscale;//x轴刻度
    private float mYscale;//y轴刻度
    // private float mStartX;
    //private float mStartY;
    private float mXlength;
    private float mYlength;
    private float mScaleTextSize;//坐标轴上刻度文字的大小
    private Rect mTextBounds = new Rect();

    private String[] mXlabels = {"9:30", "10:30", "11:30/13:00", "14:00", "15:00"};
    private String[] mYlabels = {"2.99", "2.98", "2.97", "2.96", "2.95"};
    private float[][] mAvgData = new float[6][2];
    private String mDesc = "title";

    private boolean mCrossLineIsShowing;
    private GestureDetector mGestureDetector;
    private float mCrossX;
    private float mCrossY;

    private Runnable mLongPressRunnable = new Runnable() {
        @Override
        public void run() {
            String name = Thread.currentThread().getName();
            System.out.println("lineview=======mLongPressRunnable====" + name);//main


        }
    };


    public LineView(Context context) {
        this(context, null);
    }

    public LineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LineView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LineView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    public void init() {
        this.setBackgroundColor(Color.parseColor("#ffffff"));
        mAxisPaint = new Paint();
        mAvgLinePaint = new Paint();
        mCrossLinePaint = new Paint();
        mBgColorPaint = new Paint();

        mAxisPaint.setColor(Color.parseColor("#000000"));
        mCrossLinePaint.setColor(Color.parseColor("#ff0000"));
        mAvgLinePaint.setColor(Color.parseColor("#0000ff"));
        mBgColorPaint.setColor(Color.parseColor("#eeeeee"));

        mAxisPaint.setAntiAlias(true);
        mCrossLinePaint.setAntiAlias(true);
        mAvgLinePaint.setAntiAlias(true);
        mBgColorPaint.setAntiAlias(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //处理wrapcontent 的情况
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);//测量模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);//测量宽高，一般情况下等于控件真是宽高，最终宽高在layout方法中确定

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mDefaultWidth, mDefaultHeight);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mDefaultWidth, heightSize);

        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, mDefaultHeight);
        }

        initData();
        initGesture();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackGround(canvas);//绘制view的背景
        drawYandXscale(canvas);//绘制Y轴和x刻度值
        drawXandYscale(canvas);//绘制x轴和y刻度值
        drawAvgLine(canvas);
        drawDesc(canvas);
        drawCrossLine(canvas);
    }

    public void initGesture() {
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {

                if (mCrossLineIsShowing) {
                    System.out.println("lineview=======隐藏cross");
                    mCrossLineIsShowing = false;
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (mCrossLineIsShowing) {
                    System.out.println("lineview=======隐藏cross");
                    mCrossLineIsShowing = false;
                } else {
                    System.out.println("lineview=======显示cross");
                    mCrossLineIsShowing = true;
                }

                //postDelayed(mLongPressRunnable, 100);
                mCrossY = e.getY();
                mCrossX = e.getX();

                postInvalidate();

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }


    private void initData() {

        int viewWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int viewHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

        // System.out.println("lineview=========view_width====" + viewWidth);
        // System.out.println("lineview=========view_height====" + viewHeight);

        mXscale = viewWidth / 4.0f;
        mYscale = viewHeight / 4.0f;

        // mStartX = mXscale / 2.0f;
        // mStartY = mYscale / 2.0f;

        String text = mXlabels[0];
        mAxisPaint.getTextBounds(text, 0, text.length(), mTextBounds);

        mXlength = mXscale * 4.0f;
        mYlength = mYscale * 4.0f - mTextBounds.height();

        mScaleTextSize = mXscale / 5.0f;


        mAxisPaint.setTextSize(mScaleTextSize);
        mAxisPaint.setStrokeWidth(1f);
        mCrossLinePaint.setStrokeWidth(2f);
        mAvgLinePaint.setStrokeWidth(2f);

    }


    //画view 的背景
    public void drawBackGround(Canvas canvas) {
        for (int i = 0; i < 4; i++) {

            if (i % 2 != 0) {
                float v = i * mXscale;
                System.out.println("lineview=========draw_bg====" + v);
                //canvas.drawRect((i * mXscale) + mStartX, mStartY, mStartX * (i + 1), mYlength, mBgColorPaint);
                canvas.drawRect(mXscale * i, 0, mXscale * (i + 1), mYlength, mBgColorPaint);
            }
        }
    }

    //绘制x坐标刻度值 和Y轴
    public void drawYandXscale(Canvas canvas) {

        for (int i = 0; i < mXlabels.length; i++) {

            canvas.drawLine(i * mXscale, 0, i * mXscale, mYlength, mAxisPaint);// y轴

            String text = mXlabels[i];
            mAxisPaint.getTextBounds(text, 0, text.length(), mTextBounds);

            if (i < mXlabels.length - 1) {
                if (i == 0) {
                    canvas.drawText(text, i * mXscale, mYlength + mTextBounds.height(), mAxisPaint);
                } else {
                    canvas.drawText(text, i * mXscale - mTextBounds.width() / 2, mYlength + mTextBounds.height(), mAxisPaint);
                }
            } else {
                canvas.drawText(text, i * mXscale - mTextBounds.width() - 10, mYlength + mTextBounds.height(), mAxisPaint);
            }
        }
    }

    //绘制Y坐标刻度值 和X轴
    public void drawXandYscale(Canvas canvas) {
        for (int i = 0; i < mYlabels.length; i++) {

            String text = mYlabels[i];
            mAxisPaint.getTextBounds(text, 0, text.length(), mTextBounds);

            if (i == 0) {
                canvas.drawLine(0, i * mYscale, mXlength, i * mYscale, mAxisPaint);
                canvas.drawText(text, 0, mTextBounds.height(), mAxisPaint);
            } else if (i == 4) {
                canvas.drawLine(0, mYlength, mXlength, mYlength, mAxisPaint);
                canvas.drawText(text, 0, mYlength, mAxisPaint);
            } else {
                canvas.drawLine(0, i * mYscale, mXlength, i * mYscale, mAxisPaint);
                canvas.drawText(text, 0, mYscale * i, mAxisPaint);
            }
        }
    }

    //
    public void drawAvgLine(Canvas canvas) {

        getAvgData();

        for (int i = 0; i < mYlabels.length; i++) {
            canvas.drawLine(i * mXscale, mAvgData[i][1], (i + 1) * mXscale, mAvgData[i + 1][1], mAvgLinePaint);
        }
    }

    //
    public void drawCrossLine(Canvas canvas) {
        canvas.drawLine(0,0,mCrossX,mCrossY,mCrossLinePaint);
    }

    // 获取avg数据
    public void getAvgData() {
        float startx = 0;
        float starty = mYlength;

        for (int i = 0; i < mXlabels.length; i++) {
            mAvgData[i][0] = i * mXscale;
        }
        mAvgData[0][1] = 3 * mYscale;
        mAvgData[1][1] = 2 * mYscale;
        mAvgData[2][1] = 1 * mYscale;
        mAvgData[3][1] = 2 * mYscale;
        mAvgData[4][1] = 1 * mYscale;

    }

    // drawTitle
    public void drawDesc(Canvas canvas) {
        mAxisPaint.getTextBounds(mDesc, 0, mDesc.length(), mTextBounds);
        float x = getWidth() / 2.0f - mTextBounds.width() / 2.0f;
        float y = getHeight();
        canvas.drawText(mDesc, x, y, mAxisPaint);
    }

    public void setDesc(String desc) {

    }


    private onViewClickListener mViewClickLisftener;

    public void setOnViewClickListener(onViewClickListener listener) {
        mViewClickLisftener = listener;
    }


    public interface onViewClickListener {
        void onClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //touch事件委托给手势识别器
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event);
        }
        return true;
    }
}
