package com.aican.tlcanalyzer.dragcircle.dragableline


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.min

class EditDraggableLineView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var topLineY: Float = 100f
    private var bottomLineY: Float = 400f
    private var selectedLine: SelectedLine? = null

    private val linePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
    }

    private val handlePaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    private var imageBitmap: Bitmap? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        imageBitmap?.let { bitmap ->
            val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            val dstRect = getFitCenterRect(bitmap)
            canvas.drawBitmap(bitmap, srcRect, dstRect, null)
        }

        val imageBounds = imageBitmap?.let { getFitCenterRect(it) } ?: return
        val extendedRight = imageBounds.right.toFloat() + (imageBounds.width() * 0.05f) // Extend by 5%

        drawLineWithHandle(canvas, topLineY, extendedRight)
        drawLineWithHandle(canvas, bottomLineY, extendedRight)
    }

    private fun drawLineWithHandle(canvas: Canvas, y: Float, rightLimit: Float) {
        val imageBounds = imageBitmap?.let { getFitCenterRect(it) } ?: return

        val constrainedY = y.coerceIn(imageBounds.top.toFloat(), imageBounds.bottom.toFloat())

        canvas.drawLine(imageBounds.left.toFloat(), constrainedY, rightLimit, constrainedY, linePaint)
        canvas.drawCircle(rightLimit - 10f, constrainedY, 10f, handlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedLine = when {
                    abs(event.y - topLineY) < 30 -> SelectedLine.TOP
                    abs(event.y - bottomLineY) < 30 -> SelectedLine.BOTTOM
                    else -> null
                }
                return selectedLine != null
            }

            MotionEvent.ACTION_MOVE -> {
                selectedLine?.let {
                    val newY = event.y.coerceIn(0f, height.toFloat())
                    when (it) {
                        SelectedLine.TOP -> topLineY = newY
                        SelectedLine.BOTTOM -> bottomLineY = newY
                    }
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                selectedLine = null
            }
        }
        return true
    }

    fun setInitialLines(topY: Float, bottomY: Float) {
        topLineY = topY
        bottomLineY = bottomY
        invalidate()
    }

    fun getUpdatedRectangle(): Rect {
        val originalTop = convertToOriginalImageScale(topLineY)
        val originalBottom = convertToOriginalImageScale(bottomLineY)

        return Rect(0, min(originalTop, originalBottom), width, maxOf(originalTop, originalBottom))
    }

    fun setImage(bitmap: Bitmap) {
        imageBitmap = bitmap
        invalidate()
    }

    private fun convertToOriginalImageScale(viewY: Float): Int {
        imageBitmap?.let { bitmap ->
            val fitCenterRect = getFitCenterRect(bitmap)
            return if (viewY in fitCenterRect.top.toFloat()..fitCenterRect.bottom.toFloat()) {
                val relativeY = viewY - fitCenterRect.top
                ((relativeY / fitCenterRect.height()) * bitmap.height).toInt()
            } else {
                -1
            }
        }
        return viewY.toInt()
    }

    private fun getFitCenterRect(bitmap: Bitmap): Rect {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val viewAspectRatio = width.toFloat() / height.toFloat()

        return if (aspectRatio > viewAspectRatio) {
            val newWidth = width
            val newHeight = (width / aspectRatio).toInt()
            val topOffset = (height - newHeight) / 2
            Rect(0, topOffset, newWidth, topOffset + newHeight)
        } else {
            val newHeight = height
            val newWidth = (height * aspectRatio).toInt()
            val leftOffset = (width - newWidth) / 2
            Rect(leftOffset, 0, leftOffset + newWidth, newHeight)
        }
    }

    private enum class SelectedLine { TOP, BOTTOM }
}
