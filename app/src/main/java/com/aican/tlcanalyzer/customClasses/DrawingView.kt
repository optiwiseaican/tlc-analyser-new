package com.aican.tlcanalyzer.customClasses

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.lang.Math.PI

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val path = Path()
    private val cleanCirclePath = Path() // Path for clean circle
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private var radius: Float = 0f
    private var lastCircleCenter: PointF? = null
    private val cleanCirclePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }
    private var cleanCircleVisible = false


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        canvas.drawPath(path, paint) // Draw the user-drawn circle


//        if (cleanCircleVisible && !path.isEmpty) {
//            val saveCount = canvas.save()
//            canvas.clipPath(path, Region.Op.DIFFERENCE)
//            canvas.drawPath(cleanCirclePath, cleanCirclePaint)
//            canvas.restoreToCount(saveCount)
//        }

        if (cleanCircleVisible) {
            createCleanCircle()
            canvas.drawPath(cleanCirclePath, cleanCirclePaint) // Draw the clean circle
        }

        if (!path.isEmpty) {

            canvas.drawPath(path, paint)
// Draw the user-drawn contour
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!path.isEmpty) {
                    // Clear the previous circle if path is not empty
                    path.reset()
                    cleanCirclePath.reset()
                    lastCircleCenter = null
                    invalidate()
                }
                cleanCircleVisible = false

                path.moveTo(x, y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                calculateCircleProperties()
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                path.lineTo(x, y)
                calculateCircleProperties()
                createCleanCircle()
                lastCircleCenter = PointF(x, y)
                cleanCircleVisible = true

                invalidate()
                return true
            }
        }
        return false
    }

    private fun calculateCircleProperties() {
        val pathMeasure = PathMeasure(path, false)
        val pathLength = pathMeasure.length

        // Calculate the radius
        radius = pathLength / (2 * Math.PI).toFloat()
    }

    private fun createCleanCircle() {
        val bounds = RectF()

        path.computeBounds(bounds, true)

        cleanCirclePath.reset()

        if (!bounds.isEmpty) {
            val centerX = bounds.centerX()
            val centerY = bounds.centerY()
            val radius = Math.min(bounds.width(), bounds.height()) / 2f

            cleanCirclePath.addCircle(centerX, centerY, radius, Path.Direction.CW)
        }
    }

    fun undoLastDrawing() {
        if (!path.isEmpty) {
            // Remove the last drawn path segment
            path.reset()
            invalidate()
        }
        if (!cleanCirclePath.isEmpty) {
            // Remove the last drawn path segment
            cleanCirclePath.reset()
            invalidate()
        }
    }

    fun getRadius(): Float {
        return radius
    }

    fun getArea(): Float {
        return (PI * radius * radius).toFloat()
    }

    fun getCircumference(): Float {
        return (2 * PI * radius).toFloat()
    }
}




