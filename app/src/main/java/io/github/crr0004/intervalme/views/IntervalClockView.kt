package io.github.crr0004.intervalme.views

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import io.github.crr0004.intervalme.interval.IntervalController
import io.github.crr0004.intervalme.R
import java.lang.UnsupportedOperationException
import java.util.concurrent.TimeUnit


class IntervalClockView(context: Context?, attrs: AttributeSet?) : ImageView(context, attrs) {


    private var mCirclePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mOverlayPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mDisplayMetrics = DisplayMetrics()
    //The base radius of our circle for the clock
    private var mCircleSize = 50.0f
    private var mCenter = PointF(0.0f,0.0f)
    private var mBounds = RectF()
    private val mIntervalPaddingDP = 8.0
    private val mHandler: Handler = Handler()

    // Convenience property so that the reference to IntervalController isn't lost
    var mController: IntervalController? = null

    var mPercentageComplete = 0.0f
        get() = field
        set(value) {
            field = value
            this.invalidate()
        }

    var mClockText: StringBuilder = StringBuilder(8)
        get() = field
        set(value) {
            field = value
        }

    init {
        (context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(mDisplayMetrics)

        mCirclePaint.color = Color.BLACK
        mCirclePaint.strokeWidth = 8.0f
        mCirclePaint.style = Paint.Style.STROKE

        mOverlayPaint.color = Color.BLUE
        mOverlayPaint.strokeWidth = 16.0f
        mOverlayPaint.style = Paint.Style.FILL

        mTextPaint.textAlign = Paint.Align.CENTER
        mTextPaint.textSize = mDisplayMetrics.scaledDensity * 14f

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.IntervalClock,
                0, 0)

        try {
            mCirclePaint.color = a.getColor(R.styleable.IntervalClock_background_clock_colour, Color.BLACK)
            mCirclePaint.strokeWidth = a.getFloat(R.styleable.IntervalClock_background_clock_stroke_width, 8.0f)
            mOverlayPaint.color = a.getColor(R.styleable.IntervalClock_overlay_clock_colour, Color.BLUE)
            mOverlayPaint.strokeWidth = a.getFloat(R.styleable.IntervalClock_overlay_clock_stroke_width, 16.0f)

            mTextPaint.color = a.getColor(R.styleable.IntervalClock_clock_text_colour, Color.BLACK)
            mTextPaint.textSize = a.getDimension(R.styleable.IntervalClock_clock_text_size, 8.0f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mTextPaint.typeface = a.getFont(R.styleable.IntervalClock_clock_text_typeface)
            }
            mClockText.append(a.getText(R.styleable.IntervalClock_clock_text))

        }catch (e: UnsupportedOperationException){

        }finally {
            a.recycle()
        }

    }

    /**
     * Call this view's OnClickListener, if it is defined.  Performs all normal
     * actions associated with clicking: reporting accessibility event, playing
     * a sound, etc.
     *
     * @return True there was an assigned OnClickListener that was called, false
     * otherwise is returned.
     */
    override fun performClick(): Boolean {
        return super.performClick()
    }

    /**
     * @param time Time in milliseconds
     */
    fun setClockTime(time: Long){
        mClockText.replace(0,mClockText.length,String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))//,
                //time - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(time))
        ))
        this.invalidate()
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

        canvas.drawText(mClockText.toString(),mCenter.x,mCenter.y,mTextPaint)

    }



    /**
     * @return A handler associated with the thread running the View. This
     * handler can be used to pump events in the UI events queue.
     */
    override fun getHandler(): Handler {
        return mHandler
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