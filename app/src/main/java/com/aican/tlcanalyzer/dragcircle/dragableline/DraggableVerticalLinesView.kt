package com.aican.tlcanalyzer.dragcircle.dragableline

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class DraggableVerticalLinesView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val lines = mutableListOf<Float>()  // Store X positions of vertical lines
    private var selectedLineIndex: Int? = null
    private var imageBounds: Rect? = null  // Image boundaries

    private val linePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
    }

    private val handlePaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    var imageBitmap: Bitmap? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Get image boundaries
        imageBounds = imageBitmap?.let { getFitCenterRect(it) }

        // Draw the image
        imageBitmap?.let { bitmap ->
            val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            val dstRect = getFitCenterRect(bitmap)
            canvas.drawBitmap(bitmap, srcRect, dstRect, null)
        }

        // Draw vertical lines within image boundaries
        imageBounds?.let { bounds ->
            for (x in lines) {
                val constrainedX = x.coerceIn(bounds.left.toFloat(), bounds.right.toFloat())

                // Draw vertical line
                canvas.drawLine(
                    constrainedX, bounds.top.toFloat(),
                    constrainedX, bounds.bottom.toFloat(),
                    linePaint
                )

                // Draw handle on the bottom of the line
                canvas.drawCircle(
                    constrainedX, bounds.bottom.toFloat() - 10f,
                    10f, handlePaint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if the user touched a handle
                selectedLineIndex = lines.indexOfFirst { abs(it - event.x) < 30 }
                return selectedLineIndex != -1
            }

            MotionEvent.ACTION_MOVE -> {
                // Move the selected line horizontally
                selectedLineIndex?.let { index ->
                    lines[index] = event.x.coerceIn(
                        imageBounds?.left?.toFloat() ?: 0f,
                        imageBounds?.right?.toFloat() ?: width.toFloat()
                    )
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                selectedLineIndex = null
            }
        }
        return true
    }

    // 🚀 Function to Add New Vertical Line
    fun addNewLine(x: Float) {
        lines.add(
            x.coerceIn(
                imageBounds?.left?.toFloat() ?: 0f,
                imageBounds?.right?.toFloat() ?: width.toFloat()
            )
        )
        invalidate()
    }

    // 🚀 Function to Clear All Lines
    fun clearAll() {
        lines.clear()
        invalidate()
    }

    // 🚀 Function to Undo Last Line
    fun undoLast() {
        if (lines.isNotEmpty()) {
            lines.removeAt(lines.size - 1)
            invalidate()
        }
    }

    // 🚀 Function to Get All Line Positions
    fun getAllLines(): ArrayList<Int> {
        val intLines = ArrayList<Int>()

        // Ensure image bounds are set
        if (imageBounds == null || imageBitmap == null) return intLines

        val scaleFactor = imageBitmap!!.width.toFloat() / imageBounds!!.width().toFloat()

        lines.forEach { viewX ->
            // Convert from View X to Bitmap X
            val bitmapX = ((viewX - imageBounds!!.left) * scaleFactor).toInt()
            intLines.add(bitmapX)
        }

        return intLines
    }


    fun setImage(bitmap: Bitmap) {
        imageBitmap = bitmap
        invalidate()
    }

    // 🚀 Function to Get Fit Center Rectangle for Image Scaling
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
}
