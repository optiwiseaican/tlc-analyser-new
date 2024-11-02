package com.aican.tlcanalyzer

import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.databinding.ActivityEditRectangleContourBinding
import com.aican.tlcanalyzer.utils.Source
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class EditRectangleContour : AppCompatActivity() {

    lateinit var binding: ActivityEditRectangleContourBinding
    lateinit var rgba: Mat
    lateinit var imageBitmap: Bitmap
    lateinit var rectOfRectangle: Rect

    lateinit var spotId: String
    private var leftSeekBarProgress = 0
    private var rightSeekBarProgress = 0

    private var initialLeftSeekBarProgress = 50
    private var initialRightSeekBarProgress = 50

    private var savedRect: Rect? = null
    lateinit var dir: File
    var contourJsonFileName = "null"
    lateinit var databaseHelper: DatabaseHelper

    lateinit var plotTableName: String
    private var imageHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRectangleContourBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.back.setOnClickListener {
            finish()
        }
        databaseHelper = DatabaseHelper(this@EditRectangleContour)

        spotId = intent.getStringExtra("spotId").toString()
        contourJsonFileName = intent.getStringExtra("contourJsonFileName").toString()
        plotTableName = intent.getStringExtra("plotTableName").toString()

        binding.projectName.text = "Edit Spot $spotId"

        rectOfRectangle = Source.editRectangleContourRect

        rgba = Mat()

        loadOpenCV()

        if (Source.originalBitmap != null) {
            imageBitmap = Source.originalBitmap;

        } else {
            Log.e("ItsNullvalue", "Null Uri" + "")
            Toast.makeText(
                this@EditRectangleContour,
                "Bitmap Null, Spot the contour once",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

        dir = File(
            ContextWrapper(this).externalMediaDirs[0],
            resources.getString(R.string.app_name) + intent.getStringExtra("pId")
        )



        binding.imageView.setImageBitmap(Source.originalBitmap)

        var y1 = rectOfRectangle.top
        var y2 = rectOfRectangle.bottom
        var p1 = rectOfRectangle.left
        var p2 = rectOfRectangle.right

        var imageWithLines = Mat()

        // Ensure that Source.originalBitmap and rgba are initialized
        Utils.bitmapToMat(Source.originalBitmap, imageWithLines)


//        binding.leftSeekbar.progress = initialLeftSeekBarProgress
//        binding.rightSeekbar.progress = initialRightSeekBarProgress


        imageHeight = Source.originalBitmap.height
        val rectHeight = rectOfRectangle.height()
        val topPercentage = (rectOfRectangle.top.toDouble() / imageHeight) * 100
        val bottomPercentage = (rectOfRectangle.bottom.toDouble() / imageHeight) * 100
        initialLeftSeekBarProgress = (100 - topPercentage).toInt()
        initialRightSeekBarProgress = (100 - (bottomPercentage)).toInt()

        // Set initial progress of SeekBars
        binding.leftSeekbar.progress = initialLeftSeekBarProgress
        binding.rightSeekbar.progress = initialRightSeekBarProgress

        // Call drawRectangle with the extracted coordinates
        drawRectangle(y1, y2, p1, p2, imageWithLines)

        binding.leftSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                leftSeekBarProgress = progress

                y1 = rectOfRectangle.top + (100 - progress) // Reverse the calculation for y1
                y2 = rectOfRectangle.bottom
                p1 = rectOfRectangle.left
                p2 = rectOfRectangle.right

                imageWithLines = Mat()

                Utils.bitmapToMat(Source.originalBitmap, imageWithLines)

                val newY1 = (100 - progress) * imageHeight / 100
                val newY2 = rectOfRectangle.bottom
                updateLines(
                    newY1, newY2, imageWithLines
                )

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.rightSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rightSeekBarProgress = progress

                y1 = rectOfRectangle.top
                y2 = rectOfRectangle.bottom - progress // Adjust the calculation for y2
                p1 = rectOfRectangle.left
                p2 = rectOfRectangle.right

                imageWithLines = Mat()
                Utils.bitmapToMat(Source.originalBitmap, imageWithLines)

                val newY1 = rectOfRectangle.top
                val newY2 = (100 - progress) * imageHeight / 100
//                val newY2 = rectOfRectangle.bottom + progress * imageHeight / 100
                updateLines(newY1, newY2, imageWithLines)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.saveRect.setOnClickListener {
            saveRectangle()
        }

    }

    private fun updateLines(newY1: Int, newY2: Int, imageWithLines: Mat) {
        // Update the lines based on new positions
        val p1 = rectOfRectangle.left
        val p2 = rectOfRectangle.right

        drawRectangle(newY1, newY2, p1, p2, imageWithLines)

        // Update saved rectangle coordinates
        savedRect = Rect(p1, newY1, p2, newY2)
    }

    private fun saveRectangle() {
        savedRect?.let { rect ->
            if (spotId.contains("m")) {
                val myDir: File = File(dir, contourJsonFileName)
                if (myDir.exists()) {
                    val gson = Gson()

                    val bufferedReader = BufferedReader(FileReader(File(dir, contourJsonFileName)))
                    val mapType =
                        object : TypeToken<Map<String?, List<Map<String?, Any?>?>?>?>() {}.type
                    val dataMap: MutableMap<String, List<Map<String, Any>>> =
                        gson.fromJson(bufferedReader, mapType)

                    if (dataMap.containsKey("manualContour")) {
                        val manualContours: MutableList<Map<String, Any>> =
                            dataMap["manualContour"]?.toMutableList() ?: mutableListOf()

                        for (manualContourData in manualContours) {
                            val id = manualContourData["id"] as String?
                            val shape = manualContourData["shape"] as String?
                            val roiData = manualContourData["roi"] as MutableMap<String, Double>?
                            val left = roiData!!["left"]!!.toInt()
                            val top = roiData!!["top"]!!.toInt()
                            val right = roiData!!["right"]!!.toInt()
                            val bottom = roiData!!["bottom"]!!.toInt()

                            // Check if the current id matches the target id for update
                            if (id == spotId) {
                                // Update roi data for the found object
                                roiData!!["bottom"] = rect.bottom.toDouble()
                                roiData!!["left"] = rect.left.toDouble()
                                roiData!!["right"] = rect.right.toDouble()
                                roiData!!["top"] = rect.top.toDouble()
                            }

                            val rect = Rect(left, top, right, bottom)
                            Log.e("left", left.toString() + "")
                            Log.e("top", top.toString() + "")
                            Log.e("right", right.toString() + "")
                            Log.e("bottom", bottom.toString() + "")
                        }

                        // Save the updated data back to the file
                        val fileWriter = FileWriter(File(dir, contourJsonFileName))
                        gson.toJson(dataMap, fileWriter)
                        fileWriter.close()
                    }

                    bufferedReader.close()
                }

                val imageHeight = Source.contourBitmap.height
                val distanceFromTop: Double = (rect.top + rect.bottom) * 0.5

                val maxDistance = imageHeight.toDouble()
                val rfValue4 = 1.0 - distanceFromTop / maxDistance

                val cv = 1 / rfValue4

                val rfValueTop: Double = rfValue4 + rect.height() / 2 / imageHeight.toDouble()
                val rfValueBottom: Double = rfValue4 - rect.height() / 2 / imageHeight.toDouble()


                databaseHelper.updateDataTableDataById(
                    plotTableName, spotId,
                    rfValue4.toString(), rfValueTop.toString(), rfValueBottom.toString()
                )
                Toast.makeText(
                    this@EditRectangleContour,
                    "Saving new rectangle spot",
                    Toast.LENGTH_SHORT
                ).show()

//                Source.rectangleContourEdited = true
                finish()

            } else {
                Toast.makeText(
                    this@EditRectangleContour,
                    "Sorry, Only manual contours baseline can be change",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateLines2(imageWithLines: Mat) {
        val y1 = rectOfRectangle.top - (initialLeftSeekBarProgress - leftSeekBarProgress)
        val y2 = rectOfRectangle.bottom + (rightSeekBarProgress - initialRightSeekBarProgress)
        val p1 = rectOfRectangle.left
        val p2 = rectOfRectangle.right

        drawRectangle(y1, y2, p1, p2, imageWithLines)

        savedRect = Rect(p1, y1, p2, y2)

    }

    private fun drawRectangle(y1: Int, y2: Int, p1: Int, p2: Int, imageWithLines: Mat) {

        val color = Scalar(0.0, 0.0, 255.0)  // BGR color for the line (red in this case)
        val transparentRed = Scalar(255.0, 0.0, 0.0, 50.0)  // More transparent red color
        val mask = Mat.zeros(imageWithLines.size(), imageWithLines.type())

        // Draw rectangle
        Imgproc.rectangle(
            mask,
            Point(0.0, y1.toDouble()),
            Point(imageWithLines.cols().toDouble(), y2.toDouble()),
            Scalar(255.0, 0.0, 0.0, 50.0),
            -1
        )

        // Draw horizontal lines above and below the rectangle
        val lineColor = Scalar(0.0, 0.0, 255.0)  // Green color for the line
        val lineThickness = 2

        // Draw line above the rectangle
        drawHorizontalLine(imageWithLines, y1, lineColor, lineThickness)
        // Draw line below the rectangle
        drawHorizontalLine(imageWithLines, y2, lineColor, lineThickness)

        // Combine the rectangle and lines
        Core.addWeighted(imageWithLines, 1.0, mask, 0.5, 0.0, imageWithLines)

        // Draw text
        val fontScale = 1.0
        val fontColor = Scalar(255.0, 255.0, 255.0)
        val fontThickness = 2
        val point = Point(p1.toDouble(), p2.toDouble())

        val text = spotId

        Imgproc.putText(
            imageWithLines,
            spotId,
            point,
            Imgproc.FONT_HERSHEY_SIMPLEX,
            fontScale,
            fontColor,
            fontThickness
        )

        // Convert image to bitmap and set to imageView
        val bitmapWithLines = Bitmap.createBitmap(
            imageWithLines.cols(),
            imageWithLines.rows(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(imageWithLines, bitmapWithLines)
        binding.imageView.setImageBitmap(bitmapWithLines)
    }

    private fun drawHorizontalLine(image: Mat, y: Int, color: Scalar, thickness: Int) {
        val start = Point(0.0, y.toDouble())
        val end = Point((image.cols() - 1).toDouble(), y.toDouble())
        Imgproc.line(image, start, end, color, thickness)
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