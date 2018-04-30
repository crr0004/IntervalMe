package io.github.crr0004.intervalme.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

class IntervalClockView(context: Context?, attrs: AttributeSet?) : ImageView(context, attrs) {


    private var mTextPaint: Paint

    init {
        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint.color = Color.YELLOW

    }


    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawCircle(0.0f, 0.0f, 1000.0f, mTextPaint)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Try for a width based on our minimum
        val minw = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = View.resolveSizeAndState(minw, widthMeasureSpec, 1)

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        val minh = View.MeasureSpec.getSize(w) - 100 + paddingBottom + paddingTop
        val h = View.resolveSizeAndState(View.MeasureSpec.getSize(w) - 100, heightMeasureSpec, 0)

        setMeasuredDimension(w, h)

    }
}