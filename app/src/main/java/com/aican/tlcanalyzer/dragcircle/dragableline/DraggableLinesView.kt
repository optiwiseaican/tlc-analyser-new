package com.aican.tlcanalyzer.dragcircle.dragableline

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.min

class DraggableLinesView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    val lines = mutableListOf<Float>()  // Store Y positions of lines
    private var selectedLineIndex: Int? = null

    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
    }

    private val handlePaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    private val rectanglePaints = listOf(
        Paint().apply { color = Color.YELLOW; alpha = 100; style = Paint.Style.FILL },
        Paint().apply { color = Color.GREEN; alpha = 100; style = Paint.Style.FILL },
        Paint().apply { color = Color.CYAN; alpha = 100; style = Paint.Style.FILL }
    ) // Different colors to distinguish overlapping rectangles

    var imageBitmap: Bitmap? = null
    private var imageMatrix: Matrix = Matrix()  // Matrix for scaling the image

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Get image boundaries
        val imageBounds = imageBitmap?.let { getFitCenterRect(it) }

        // Draw image with fitCenter
        imageBitmap?.let { bitmap ->
            val srcRect = Rect(0, 0, bitmap.width, bitmap.height)  // Original image dimensions
            val dstRect = getFitCenterRect(bitmap)  // Scaled position

            canvas.drawBitmap(bitmap, srcRect, dstRect, null)
        }

        // Define percentage increase (e.g., 5% more from the right)
        val percentageIncrease = 0.05f  // 5% increment

        // Draw rectangles between every two consecutive lines
        for (i in 0 until lines.size - 1 step 2) {
            val top = lines[i]
            val bottom = lines[i + 1]

            val colorIndex = (i / 2) % rectanglePaints.size  // Cycle through colors

            // Ensure rectangles are inside image boundaries
            if (imageBounds != null) {
                val constrainedLeft = imageBounds.left.toFloat()
                val extendedRight = imageBounds.right.toFloat() + (imageBounds.width() * percentageIncrease) // Extend by 5%
                val constrainedTop = maxOf(imageBounds.top.toFloat(), top)
                val constrainedBottom = minOf(imageBounds.bottom.toFloat(), bottom)

                canvas.drawRect(constrainedLeft, constrainedTop, extendedRight, constrainedBottom, rectanglePaints[colorIndex])
            }
        }

        // Draw horizontal lines and handles with percentage extension
        for (y in lines) {
            if (imageBounds != null) {
                val constrainedY = y.coerceIn(imageBounds.top.toFloat(), imageBounds.bottom.toFloat())

                val extendedRight = imageBounds.right.toFloat() + (imageBounds.width() * percentageIncrease) // Extend by 5%

                // Draw line with increased right side
                canvas.drawLine(imageBounds.left.toFloat(), constrainedY, extendedRight, constrainedY, paint)

                // Draw handle slightly inside the right boundary
                canvas.drawCircle(extendedRight - 10f, constrainedY, 10f, handlePaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if user touched any handle
                selectedLineIndex = lines.indexOfFirst { abs(it - event.y) < 30 }
                return selectedLineIndex != -1
            }

            MotionEvent.ACTION_MOVE -> {
                // Move the selected line
                selectedLineIndex?.let { index ->
                    lines[index] = event.y.coerceIn(0f, height.toFloat())
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                selectedLineIndex = null
            }
        }
        return true
    }

    fun addNewLine(y: Float) {
        lines.add(y)
        invalidate()
    }

    fun setImage(bitmap: Bitmap) {
        imageBitmap = bitmap
        invalidate()
    }

    // 🚀 Function to Clear All Lines & Rectangles
    fun clearAll() {
        lines.clear()
        invalidate()
    }

    // 🚀 Function to Undo Last Added Line or Rectangle
    fun undoLast() {
        if (lines.isNotEmpty()) {
            if (lines.size % 2 == 0) {
                // If a rectangle exists, remove the last two lines (full rectangle)
                lines.removeAt(lines.size - 1)
                lines.removeAt(lines.size - 1)
            } else {
                // If an extra single line exists, remove only that line
                lines.removeAt(lines.size - 1)
            }
            invalidate()
        }
    }

    fun getAllRectangles(): List<Rect> {
        val rectangles = mutableListOf<Rect>()

        // If there's an unpaired line, remove it
        if (lines.size % 2 != 0) {
            lines.removeAt(lines.size - 1)
            invalidate()
        }

        // Convert lines to original image scale and create rectangles
        for (i in 0 until lines.size - 1 step 2) {
            val top = convertToOriginalImageScale(lines[i])
            val bottom = convertToOriginalImageScale(lines[i + 1])

            Log.e("userTapDraggable", " $top, $bottom") // Logs values in original image scale

            rectangles.add(Rect(0, minOf(top, bottom), width, maxOf(top, bottom)))
        }

        return rectangles
    }

    // 🚀 Helper function to convert View Y-coordinates to Original Image Y-coordinates
    private fun convertToOriginalImageScale(viewY: Float): Int {
        imageBitmap?.let { bitmap ->
            val fitCenterRect = getFitCenterRect(bitmap)  // Get image bounds within the View

            return if (viewY.toInt() in fitCenterRect.top..fitCenterRect.bottom) {
                val relativeY = viewY - fitCenterRect.top  // Adjust Y relative to the displayed image
                ((relativeY / fitCenterRect.height()) * bitmap.height).toInt()  // Scale to original image height
            } else {
                -1 // Out of bounds, should not happen
            }
        }
        return viewY.toInt()  // Default to view Y if imageBitmap is null
    }

    // 🚀 Function to Get Fit Center Rectangle for Image Scaling
    private fun getFitCenterRect(bitmap: Bitmap): Rect {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val viewAspectRatio = width.toFloat() / height.toFloat()

        return if (aspectRatio > viewAspectRatio) {
            // Image is wider than view
            val newWidth = width
            val newHeight = (width / aspectRatio).toInt()
            val topOffset = (height - newHeight) / 2
            Rect(0, topOffset, newWidth, topOffset + newHeight)
        } else {
            // Image is taller than view
            val newHeight = height
            val newWidth = (height * aspectRatio).toInt()
            val leftOffset = (width - newWidth) / 2
            Rect(leftOffset, 0, leftOffset + newWidth, newHeight)
        }
    }
}
