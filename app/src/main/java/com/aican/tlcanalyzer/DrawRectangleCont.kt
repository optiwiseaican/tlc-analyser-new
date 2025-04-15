package com.aican.tlcanalyzer

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.dataClasses.ManualContour
import com.aican.tlcanalyzer.databinding.ActivityDrawRectangleContBinding
import com.aican.tlcanalyzer.utils.Source
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.roundToInt


class DrawRectangleCont : AppCompatActivity() {
    lateinit var binding: ActivityDrawRectangleContBinding
    lateinit var imageBitmap: Bitmap
    lateinit var rgba: Mat
    private lateinit var originalImage: Mat

    private var horizontalLinesYCoordinates = ArrayList<Int>()
    private var userTaps = ArrayList<Int>()
    private var rectangles = mutableListOf<Pair<Int, Int>>()
    private var rectangleList = ArrayList<Rect>()

    private val manualContourArrayList = ArrayList<ManualContour>()
    private val onlyManualContourArrayList = ArrayList<ManualContour>()
    private var lastSeekBarProgress = 0
    private var imageHeight = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawRectangleContBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.back.setOnClickListener {
            finish()
        }

        rgba = Mat()

        loadOpenCV()

        for (manualCont in Source.manualContourArrayList) {
            manualContourArrayList.add(
                ManualContour(
                    manualCont.shape,
                    manualCont.roi,
                    manualCont.indexName,
                    manualCont.mainContIndex,
                    manualCont.rfIndex
                )
            )
        }


//        if (Source.contourUri != null) {
//            imageBitmap =
//                MediaStore.Images.Media.getBitmap(this.contentResolver, Source.contourUri)
//
//
//        } else {
//            Log.e("ItsNullvalue", "Null Uri" + "")
//            Toast.makeText(
//                this@DrawRectangleCont,
//                "Uri Null, Spot the contour once",
//                Toast.LENGTH_SHORT
//            ).show()
//            finish()
//        }

        if (Source.contourBitmap != null) {
            imageBitmap = Source.contourBitmap;

        } else {
            Log.e("ItsNullvalue", "Null Uri" + "")
            Toast.makeText(
                this@DrawRectangleCont,
                "Bitmap Null, Spot the contour once",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        binding.imageView.setImageBitmap(Source.contourBitmap)

        binding.imageView.setOnTouchListener { _, event ->
            handleTouch(event)
            true
        }

        binding.clearAll.setOnClickListener {

            rectangleList = ArrayList()
            userTaps = ArrayList()
            horizontalLinesYCoordinates = ArrayList()
            rectangles = ArrayList()
            binding.imageView.setImageBitmap(imageBitmap)

            for (i in onlyManualContourArrayList.indices) {
                manualContourArrayList.removeLast()
            }

            onlyManualContourArrayList.clear()

        }

        binding.saveRect.setOnClickListener {

            if (rectangleList != null && rectangleList.size > 0) {
                Source.rectangleList = ArrayList()
                Source.rectangleList.addAll(rectangleList)
                Source.rectangle = true
                finish()
            } else {
                Toast.makeText(this@DrawRectangleCont, "No spots", Toast.LENGTH_SHORT).show()
            }

        }
        binding.imageView.scaleX *= 2.1f
        binding.imageView.scaleY *= 2.1f
        binding.zoomIn.setOnClickListener {
            binding.imageView.scaleX *= 1.1f
            binding.imageView.scaleY *= 1.1f

        }
        binding.zoomOut.setOnClickListener {

            binding.imageView.scaleX *= 0.9f
            binding.imageView.scaleY *= 0.9f

        }

        binding.moveImageView.max = (imageBitmap.width - binding.imageView.width).coerceAtLeast(0)

        binding.moveImageView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Calculate the translation factor based on progress and current zoom level
                val scaleFactor = binding.imageView.scaleX
                val multiplier = (5 * scaleFactor).toInt() // Adjust this value as needed
                val translationFactor = (progress - seekBar!!.max / 2) * multiplier.toFloat()

                // Apply translation to the ImageView
                binding.imageView.translationX = translationFactor
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.undoButton.setOnClickListener {
            undoLastRectangle()
        }

        binding.lineSeekBar.visibility = View.GONE

        binding.lineSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateLastDrawnRectangleLines(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed in this case
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed in this case
            }
        })


        binding.slideThisLine.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateLastDrawnRectangleLines2(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })


    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun handleTouch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x.toInt()
            val touchY = event.y.toInt()


            imageHeight = binding.imageView.height

            // Calculate the percentage of the touched y-coordinate relative to the image height
            val topPer = (touchY.toDouble() / imageHeight) * 100

            binding.slideThisLine.progress = topPer.roundToInt()

            val originalImageX =
                (touchX.toFloat() / binding.imageView.getWidth() * originalImage.cols()).toInt()
            val originalImageY =
                (touchY.toFloat() / binding.imageView.getHeight() * originalImage.rows()).toInt()

            Log.e(
                "TopPer",
                "$topPer, touchY: $touchY, touchX: $touchX, ImageHeight: $imageHeight" +
                        " OriginalImageY: $originalImageY"
            )

            horizontalLinesYCoordinates.add(originalImageY)
            userTaps.add(originalImageY)
            drawHorizontalLinesOnImage()

            if (userTaps.size >= 2) {
                val y1 = userTaps[userTaps.size - 2] // top
                val y2 = userTaps[userTaps.size - 1] // bottom

                Log.e("userTaps", "$y1, $y2")
                //userTaps        E  526, 687
                if (userTaps.size % 2 == 0) {

                    rectangles.add(y1 to y2)

                    if (y1 < y2) {
                        val rect = Rect(0, y1, imageWithLines.cols(), y2)
                        rectangleList.add(rect)

                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
                        manualContourArrayList.add(ManualContour(0, rect, "i", "i", 0))


                    } else {
                        val rect = Rect(0, y2, imageWithLines.cols(), y1)

                        rectangleList.add(rect)



                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
                    }


                }
            }

        }
    }

    private fun updateLastDrawnRectangleLines2(progress: Int) {
        if (userTaps.isNotEmpty() && horizontalLinesYCoordinates.isNotEmpty()) {
            val lastUserTapIndex = userTaps.size - 1
            val lastUserTapIndexH = horizontalLinesYCoordinates.size - 1

            val lastVal = userTaps[lastUserTapIndex]
            val lastValH = horizontalLinesYCoordinates[lastUserTapIndexH]

            userTaps.removeLast()
            horizontalLinesYCoordinates.removeLast()

            imageHeight = binding.imageView.height

//            val topPer = (touchY.toDouble() / imageHeight) * 100


            val touchY = (progress.toDouble() / 100.toDouble()) * imageHeight

            val originalImageY =
                (touchY.toFloat() / binding.imageView.height * originalImage.rows()).toInt()

            Log.e(
                "Oriinal", "touchY: $touchY, originalY: $originalImageY, progress: $progress" +
                        " imageHeight: $imageHeight" +
                        ""
            )

            horizontalLinesYCoordinates.add(originalImageY)
            userTaps.add(originalImageY)


            drawHorizontalLinesOnImage()

            if (userTaps.size >= 2) {
                val y1 = userTaps[userTaps.size - 2]
                val y2 = userTaps[userTaps.size - 1]
                if (userTaps.size % 2 == 0) {

                    rectangles.removeLast()
                    rectangles.add(y1 to y2)

                    if (y1 < y2) {
                        val rect = Rect(0, y1, imageWithLines.cols(), y2)
                        rectangleList.removeLast()
                        rectangleList.add(rect)

                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
                        if (manualContourArrayList.isNotEmpty()) {
                            manualContourArrayList.removeLast()
                        }
                        manualContourArrayList.add(ManualContour(0, rect, "i", "i", 0))


                    } else {
                        val rect = Rect(0, y2, imageWithLines.cols(), y1)

                        rectangleList.removeLast()
                        rectangleList.add(rect)



                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
                    }


                }
            }


        }
    }

    private fun updateLastDrawnRectangleLines(progress: Int) {
        if (rectangleList.isNotEmpty()) {
            val lastRectangle = rectangleList.last()
            val deltaY = progress - lastSeekBarProgress

            // Update the Y coordinates of the last drawn rectangle's lines
            val updatedY1 = lastRectangle.top + deltaY
            val updatedY2 = lastRectangle.bottom + deltaY

            // Update the Y coordinates in rectangleList
            lastRectangle.top = updatedY1
            lastRectangle.bottom = updatedY2

            // Clear the image
            val imageWithLines = originalImage.clone()

            // Redraw all rectangles with the updated positions
            drawAllRectangles(imageWithLines)

            // Update the imageView with the modified image
            val bitmapWithLines = Bitmap.createBitmap(
                imageWithLines.cols(),
                imageWithLines.rows(),
                Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(imageWithLines, bitmapWithLines)
            binding.imageView.setImageBitmap(bitmapWithLines)

            // Update lastSeekBarProgress
            lastSeekBarProgress = progress
        }
    }

    private fun undoLastRectangle() {
        if (rectangleList.isNotEmpty()) {

            if (rectangleList.isNotEmpty())
                rectangleList.removeLast()

            if (rectangleList.isNotEmpty())
                rectangles.removeLast()

            if (rectangleList.isNotEmpty())
                horizontalLinesYCoordinates.removeLast()

            if (rectangleList.isNotEmpty())
                horizontalLinesYCoordinates.removeLast()

            if (rectangleList.isNotEmpty())
                userTaps.removeLast()

            if (rectangleList.isNotEmpty())
                userTaps.removeLast()


            drawHorizontalLinesOnImage()

            if (userTaps.size >= 2) {
                val y1 = userTaps[userTaps.size - 2]
                val y2 = userTaps[userTaps.size - 1]
                if (userTaps.size % 2 == 0) {

//                    rectangles.add(y1 to y2)

                    if (y1 < y2) {
                        val rect = Rect(0, y1, imageWithLines.cols(), y2)
//                        rectangleList.add(rect)

                        manualContourArrayList.removeLast()

                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
//                        manualContourArrayList.add(ManualContour(0, rect, "i", "i", 0))


                    } else {
                        val rect = Rect(0, y2, imageWithLines.cols(), y1)

//                        rectangleList.add(rect)


                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
                    }
                }
            }

        } else {
            rectangleList = ArrayList()
            userTaps = ArrayList()
            horizontalLinesYCoordinates = ArrayList()
            rectangles = ArrayList()
            binding.imageView.setImageBitmap(imageBitmap)

            for (i in onlyManualContourArrayList.indices) {
                manualContourArrayList.removeLast()
            }

            onlyManualContourArrayList.clear()
            Toast.makeText(this@DrawRectangleCont, "No rectangles to undo", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // Function to redraw the image with rectangles after modification


    fun generateUniqueIndexName(manualContourArrayList: ArrayList<ManualContour>): String {
        var index = 1
        var indexName = Source.manual_contour_prefix + "$index"
        while (this.isIndexNameInUse(indexName, manualContourArrayList)) {
            index++
            indexName = Source.manual_contour_prefix + "$index"
        }
        return indexName
    }

    fun isIndexNameInUse(
        indexName: String,
        manualContourArrayList: ArrayList<ManualContour>
    ): Boolean {
        return manualContourArrayList.any { it.indexName == indexName }
    }


    private fun drawRectangle(y1: Int, y2: Int, p1: Int, p2: Int, imageWithLines: Mat) {

        val color = Scalar(0.0, 0.0, 255.0)  // BGR color for the line (red in this case)
        val transparentRed = Scalar(255.0, 0.0, 0.0, 50.0)  // More transparent red color
        val mask = Mat.zeros(imageWithLines.size(), imageWithLines.type())

        Imgproc.rectangle(
            mask,
            Point(0.0, y1.toDouble()),
            Point(imageWithLines!!.cols().toDouble(), y2.toDouble()),
            Scalar(255.0, 0.0, 0.0, 50.0),
            -1
        )

        Core.addWeighted(imageWithLines, 1.0, mask, 0.5, 0.0, imageWithLines)

        val fontScale = 1.0
        val fontColor = Scalar(255.0, 255.0, 255.0)
        val fontThickness = 2
        val point = Point(p1.toDouble(), p2.toDouble()) // Adjust these coordinates as needed

//        val text = "Rectangle: ($p1, $p2)"
//        val text = generateUniqueIndexName(manualContourArrayList)
        val text = Source.manual_contour_prefix + rectangles.size.toString()
//        Toast.makeText(this@DrawRectangleCont, "" + text, Toast.LENGTH_SHORT).show()
//        Toast.makeText(this@DrawRectangleCont, "" + text, Toast.LENGTH_SHORT).show()

        Imgproc.putText(
            imageWithLines,
            text,
            point,
            Imgproc.FONT_HERSHEY_SIMPLEX,
            fontScale,
            fontColor,
            fontThickness
        )

        val bitmapWithLines = Bitmap.createBitmap(
            imageWithLines.cols(),
            imageWithLines.rows(),
            Bitmap.Config.ARGB_8888
        )

        Utils.matToBitmap(imageWithLines, bitmapWithLines)

        binding.imageView.setImageBitmap(bitmapWithLines)

        // Add text to the top left corner

    }


    private fun drawAllRectangles(imageWithLines: Mat) {

//        val imageWithLines = originalImage.clone()

        var bitmapWithLines: Bitmap? = Bitmap.createBitmap(
            imageWithLines.cols(),
            imageWithLines.rows(),
            Bitmap.Config.ARGB_8888
        )

        var i = 1


        for (rectangle in rectangles) {

            val color = Scalar(0.0, 0.0, 255.0)  // BGR color for the line (red in this case)
            val transparentRed = Scalar(255.0, 0.0, 0.0, 50.0)  // More transparent red color
            val mask = Mat.zeros(imageWithLines.size(), imageWithLines.type())

            Imgproc.rectangle(
                mask,
                Point(0.0, rectangle.first.toDouble()),
                Point(imageWithLines!!.cols().toDouble(), rectangle.second.toDouble()),
                Scalar(255.0, 0.0, 0.0, 50.0),
                -1
            )

            Core.addWeighted(imageWithLines, 1.0, mask, 0.5, 0.0, imageWithLines)

            val y1 = rectangle.first
            val y2 = rectangle.second

            if (y1 < y2) {
                val fontScale = 1.0
                val fontColor = Scalar(255.0, 255.0, 255.0)
                val fontThickness = 2
                val point = Point(
                    0.toDouble(),
                    y1.toDouble()
                ) // Adjust these coordinates as needed
//                val text = generateUniqueIndexName(manualContourArrayList)

                val text = Source.manual_contour_prefix + "$i"
                Imgproc.putText(
                    imageWithLines,
                    text,
                    point,
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    fontScale,
                    fontColor,
                    fontThickness
                )

            } else {
                val fontScale = 1.0
                val fontColor = Scalar(255.0, 255.0, 255.0)
                val fontThickness = 2
                val point = Point(
                    0.toDouble(),
                    y2.toDouble()
                ) // Adjust these coordinates as needed

                val text = Source.manual_contour_prefix + "$i"

//                val text = "Rectangle: (0, ${y2})"
                Imgproc.putText(
                    imageWithLines,
                    text,
                    point,
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    fontScale,
                    fontColor,
                    fontThickness
                )
            }



            bitmapWithLines = Bitmap.createBitmap(
                imageWithLines.cols(),
                imageWithLines.rows(),
                Bitmap.Config.ARGB_8888
            )
            i++

//            drawRectangle(rectangle.first, rectangle.second)
        }
        Utils.matToBitmap(imageWithLines, bitmapWithLines)

        binding.imageView.setImageBitmap(bitmapWithLines)

    }

    lateinit var imageWithLines: Mat

    private fun drawHorizontalLinesOnImage() {
        imageWithLines = originalImage.clone()

//        horizontalLinesYCoordinates.sort()

        val lineColor = Scalar(0.0, 0.0, 255.0) // Red color
        val lineThickness = 2

        for (y in horizontalLinesYCoordinates) {
            drawHorizontalLine(imageWithLines, y, lineColor, lineThickness)
        }

        val bitmapWithLines = Bitmap.createBitmap(
            imageWithLines.cols(),
            imageWithLines.rows(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(imageWithLines, bitmapWithLines)
        binding.imageView.setImageBitmap(bitmapWithLines)

        drawAllRectangles(imageWithLines)
    }

    private fun drawHorizontalLine(image: Mat, y: Int, color: Scalar, thickness: Int) {
        val start = Point(0.0, y.toDouble())
        val end = Point((image.cols() - 1).toDouble(), y.toDouble())
        Imgproc.line(image, start, end, color, thickness)
    }

    override fun onResume() {
        super.onResume()
        originalImage = Mat()
        Utils.bitmapToMat(imageBitmap, originalImage)
        drawAllRectangles(originalImage) // Redraw rectangles

    }

    private fun loadOpenCV() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(
                "OpenCV",
                "Internal OpenCV library not found. Using OpenCV Manager for initialization"
            )
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i("OpenCV", "OpenCV loaded successfully")
                    rgba = Mat()
                }

                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }
}