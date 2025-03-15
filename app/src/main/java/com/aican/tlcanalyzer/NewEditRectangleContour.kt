package com.aican.tlcanalyzer


import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.databinding.ActivityNewEditRectangleContourBinding
import com.aican.tlcanalyzer.dragcircle.dragableline.EditDraggableLineView
import com.aican.tlcanalyzer.utils.Source
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class NewEditRectangleContour : AppCompatActivity() {

    lateinit var binding: ActivityNewEditRectangleContourBinding
    lateinit var editDraggableLineView: EditDraggableLineView
    lateinit var imageBitmap: Bitmap
    lateinit var rectOfRectangle: Rect
    lateinit var databaseHelper: DatabaseHelper
    lateinit var dir: File

    lateinit var spotId: String
    lateinit var contourJsonFileName: String
    lateinit var plotTableName: String
    private var imageHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewEditRectangleContourBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        editDraggableLineView = binding.draggableLinesView
        databaseHelper = DatabaseHelper(this@NewEditRectangleContour)

        // Get Data from Intent
        spotId = intent.getStringExtra("spotId").toString()
        contourJsonFileName = intent.getStringExtra("contourJsonFileName").toString()
        plotTableName = intent.getStringExtra("plotTableName").toString()

        binding.projectName.text = "Edit Spot $spotId"

        dir = File(
            ContextWrapper(this).externalMediaDirs[0],
            resources.getString(R.string.app_name) + intent.getStringExtra("pId")
        )
        rectOfRectangle = Source.editRectangleContourRect
        imageBitmap = Source.originalBitmap

        if (imageBitmap == null) {
            Log.e("ItsNullvalue", "Null Uri")
            Toast.makeText(this, "Bitmap Null, Spot the contour once", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Initialize Image in Draggable View
        editDraggableLineView.setImage(imageBitmap)

        // Initialize Draggable Lines from Previous Contour Positions
        editDraggableLineView.setInitialLines(rectOfRectangle.top.toFloat(), rectOfRectangle.bottom.toFloat())

        // Back Button
        binding.back.setOnClickListener {
            finish()
        }

        // Save New Rectangle Data
        binding.saveRect.setOnClickListener {
            saveRectangle()
        }
    }

    private fun saveRectangle() {
        val updatedRect = editDraggableLineView.getUpdatedRectangle()
        saveRectangleData(updatedRect)
    }

    private fun saveRectangleData(rect: Rect) {
      val  savedRect = rect  // Save the updated rectangle reference

        if (spotId.contains("m")) {
            val myDir = File(dir, contourJsonFileName)
            if (myDir.exists()) {
                val gson = Gson()
                val bufferedReader = BufferedReader(FileReader(myDir))
                val mapType = object : TypeToken<Map<String?, List<Map<String?, Any?>?>?>?>() {}.type
                val dataMap: MutableMap<String, List<Map<String, Any>>> = gson.fromJson(bufferedReader, mapType)

                if (dataMap.containsKey("manualContour")) {
                    val manualContours: MutableList<Map<String, Any>> =
                        dataMap["manualContour"]?.toMutableList() ?: mutableListOf()

                    for (manualContourData in manualContours) {
                        val id = manualContourData["id"] as String?
                        val roiData = manualContourData["roi"] as MutableMap<String, Double>?

                        if (id == spotId && roiData != null) {
                            roiData["left"] = rect.left.toDouble()
                            roiData["top"] = rect.top.toDouble()
                            roiData["right"] = rect.right.toDouble()
                            roiData["bottom"] = rect.bottom.toDouble()
                        }
                    }

                    // Save updated data
                    val fileWriter = FileWriter(myDir)
                    gson.toJson(dataMap, fileWriter)
                    fileWriter.close()
                }

                bufferedReader.close()
            }

            val imageHeight = Source.contourBitmap.height
            val distanceFromTop: Double = (rect.top + rect.bottom) * 0.5
            val maxDistance = imageHeight.toDouble()
            val rfValue = 1.0 - distanceFromTop / maxDistance
            val rfValueTop = rfValue + rect.height() / 2 / imageHeight.toDouble()
            val rfValueBottom = rfValue - rect.height() / 2 / imageHeight.toDouble()

            databaseHelper.updateDataTableDataById(
                plotTableName, spotId,
                rfValue.toString(), rfValueTop.toString(), rfValueBottom.toString()
            )

            Toast.makeText(this, "Saving new rectangle spot", Toast.LENGTH_SHORT).show()
            finish()

        } else {
            Toast.makeText(this, "Sorry, Only manual contours baseline can be changed", Toast.LENGTH_SHORT).show()
        }
    }

}
