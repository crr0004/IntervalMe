package io.github.crr0004.intervalme.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.ImageView

class IntervalClockView(context: Context?, attrs: AttributeSet?) : ImageView(context, attrs) {


    private var mCirclePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mOverlayPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDisplayMetrics = DisplayMetrics()
    //The base radius of our circle for the clock
    private var mCircleSize = 50.0f
    private var mCenter = PointF(0.0f,0.0f)
    private var mBounds = RectF()
    private val mIntervalPaddingDP = 8.0

    var mPercentageComplete = 0.0f
        get() = field
        set(value) {
            field = value
            this.invalidate()
        }

    init {
        mCirclePaint.color = Color.BLACK
        mCirclePaint.strokeWidth = 8.0f
        mCirclePaint.style = Paint.Style.STROKE

        mOverlayPaint.color = Color.BLUE
        mOverlayPaint.strokeWidth = 16.0f
        mOverlayPaint.style = Paint.Style.FILL

    }



    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     *
     * @param w Current width of this view.
     * @param h Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(mDisplayMetrics)
        val logicalDensity = mDisplayMetrics.density
        val intervalPaddingPx = Math.ceil((mIntervalPaddingDP * logicalDensity))

        //min so when we are in landscape it uses the smaller value
        mCircleSize = ((Math.min(w,h)/2) - intervalPaddingPx).toFloat()
        //Center is in the center of the view
        mCenter.set((w/2).toFloat(), (h/2).toFloat())
        mBounds.set(mCenter.x-mCircleSize,mCenter.y-mCircleSize,mCenter.x+mCircleSize,mCenter.y+mCircleSize)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //canvas!!.drawCircle((width/2).toFloat(), (height/2).toFloat(), (Math.min(width,height)/2).toFloat(), mCirclePaint)
        canvas!!.drawArc(mBounds,0.0f, 360f, false, mCirclePaint)
        //270 so it starts at the top
        canvas.drawArc(mBounds,270f, 360f * mPercentageComplete, true, mOverlayPaint)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Try for a width based on our minimum
        val minw = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = View.resolveSizeAndState(minw, widthMeasureSpec, 1)

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
       // val minh = View.MeasureSpec.getSize(w) + paddingBottom + paddingTop
        val h = View.resolveSizeAndState(View.MeasureSpec.getSize(w), heightMeasureSpec, 0)

        setMeasuredDimension(w, h)

    }
}