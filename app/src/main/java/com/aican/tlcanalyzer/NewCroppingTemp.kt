package com.aican.tlcanalyzer

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.database.UsersDatabase
import com.aican.tlcanalyzer.databinding.ActivityNewCroppingTempBinding
import com.aican.tlcanalyzer.dialog.AuthDialog
import com.aican.tlcanalyzer.utils.SharedPrefData
import com.aican.tlcanalyzer.utils.Source
import com.aican.tlcanalyzer.utils.Subscription
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewCroppingTemp : AppCompatActivity() {

    lateinit var dir: File
    private var loadedBitmap: Bitmap? = null
    private var databaseHelper: DatabaseHelper? = null
    private var usersDatabase: UsersDatabase? = null
    private var id: String? = null
    private var projectImage: String? = null
    private var projectName: String? = null
    private var tableName: String? = null
    private var verticalLinesXCoordinates = ArrayList<Int>()
    private lateinit var binding: ActivityNewCroppingTempBinding
    private var sizeOfMainImageList: Int = 0
    private var sizeOfSplitImageList: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCroppingTempBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()
        binding.back.setOnClickListener { finish() }

        usersDatabase = UsersDatabase(this)
        databaseHelper = DatabaseHelper(this)

        id = intent.getStringExtra("id")
        projectImage = intent.getStringExtra("projectImage")
        projectName = intent.getStringExtra("projectName")

        tableName = intent.getStringExtra("tableName")

        dir = Source.getSplitFolderFile(
            this,
            intent.getStringExtra("projectName"),
            intent.getStringExtra("id")
        )
        loadImage()

        binding.addLineButton.setOnClickListener {
            val randomX = (50..950).random().toFloat()
            binding.draggableLinesView.addNewLine(randomX)
        }

        binding.undoLastButton.setOnClickListener {
            binding.draggableLinesView.undoLast()
        }

        binding.clearAllButton.setOnClickListener {
            binding.draggableLinesView.clearAll()
        }

        binding.btnFinalSlicing.setOnClickListener {
            verticalLinesXCoordinates =
                binding.draggableLinesView.getAllLines().toMutableList() as ArrayList<Int>
            if (verticalLinesXCoordinates.isNotEmpty()) {
                processFinalSlicing()
            } else {
                Toast.makeText(this@NewCroppingTemp, "No split added", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadImage() {
        if (!SplitImage.addingMainImage) {
//            val outFile = File(dir, projectImage!!)
//            if (outFile.exists()) {
//                loadedBitmap = BitmapFactory.decodeFile(outFile.absolutePath)
//            } else {
            loadedBitmap = CapturedImagePreview.splitBitmap

//            }
        } else {
            sizeOfMainImageList = SplitImage.sizeOfMainImagesList
            sizeOfSplitImageList = SplitImage.sizeOfSplitImageList
//
//            val outFile = File(dir, "${projectImage}_$sizeOfMainImageList")
//            if (outFile.exists()) {
//                loadedBitmap = BitmapFactory.decodeFile(outFile.absolutePath)
//            } else {
            loadedBitmap = CapturedImagePreview.splitBitmap

//            }
        }

        if (loadedBitmap != null) {
            binding.draggableLinesView.setImage(loadedBitmap!!)
        } else {
            Toast.makeText(this, "Image not found or loaded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processFinalSlicing() {
        if (!SplitImage.addingMainImage) {
            databaseHelper!!.createSplitTable(tableName)

            SharedPrefData.saveData(
                this@NewCroppingTemp,
                SharedPrefData.PR_ACTUAL_LIMIT_KEY,
                (Subscription.NO_OF_PROJECTS_MADE + 1).toString()
            )

            saveImageViewToFile(dir, loadedBitmap!!, projectImage!!, this)

            val tempFileName = "TEMP$projectImage"
            val originalFileName = "ORG_$projectImage"
            val splitMarkingName = "MARK_$projectImage"

            // here main image is saving
            saveImageToDownloads(loadedBitmap!!, projectName!!, this)

            saveImageViewToFile(dir, loadedBitmap!!, tempFileName, this, true)
            saveImageViewToFile(dir, CapturedImagePreview.originalBitmap!!, originalFileName, this)

            val mainImageTableID = "MAIN_IMG_$id"
            databaseHelper!!.createSplitMainImageTable(mainImageTableID)

            val success = databaseHelper!!.insertData(
                id, intent.getStringExtra("projectName"),
                intent.getStringExtra("projectDescription"),
                intent.getStringExtra("timeStamp"), projectImage,
                "na", intent.getStringExtra("splitId"),
                "true", intent.getStringExtra("splitId"),
                "0", "0", tableName,
                intent.getStringExtra("roiTableID"),
                "na", "na", "na",
                "-1000", "-1000"
            )

            if (success) {
                usersDatabase!!.logUserAction(
                    AuthDialog.activeUserName, AuthDialog.activeUserRole,
                    "Created Split Project", intent.getStringExtra("projectName")!!, id!!, "Split"
                )
                sliceImage(dir, splitMarkingName)
            }
        } else {
            val idm = "SPLIT_ID" + System.currentTimeMillis()
            val timestamp =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            saveImageViewToFile(dir, loadedBitmap!!, "${sizeOfMainImageList}_$projectImage", this)

            val tempFileName = "TEMP${sizeOfMainImageList}_$projectImage"
            val originalFileName = "ORG_${sizeOfMainImageList}_$projectImage"
            val splitMarkingName = "MARK_${sizeOfMainImageList}_$projectImage"

            saveImageToDownloads(loadedBitmap!!, projectName!!, this)

            saveImageViewToFile(dir, loadedBitmap!!, tempFileName, this, true)
            saveImageViewToFile(dir, CapturedImagePreview.originalBitmap!!, originalFileName, this)


            val volumePlotTableID = "VOL_" + System.currentTimeMillis()
            val intensityPlotTableID = "INT_" + System.currentTimeMillis()
            val plotTableID = "TAB_" + System.currentTimeMillis()
            val success = databaseHelper!!.insertSplitMainImage(
                "MAIN_IMG_$id", idm,
                "Main Image ${sizeOfMainImageList + 1}", "${sizeOfMainImageList}_$projectImage",
                timestamp, "100", "2",
                intent.getStringExtra("roiTableID"), volumePlotTableID,
                intensityPlotTableID,
                plotTableID,
                intent.getStringExtra("projectDescription"), "0", "-1000", "-1000"
            )
            databaseHelper!!.createVolumePlotTable(volumePlotTableID)
            databaseHelper!!.createRfVsAreaIntensityPlotTable(intensityPlotTableID)
            databaseHelper!!.createAllDataTable(plotTableID)
            databaseHelper!!.createSpotLabelTable("LABEL_$plotTableID")
            if (success) {
                sliceImage(dir, splitMarkingName)
                SplitImage.addingMainImage = false
            }
        }
    }

    private fun sliceImage(dir: File, splitMarkingName: String, splitImageCounts: Int = 0) {
        val slicedImagesBitmap = ArrayList<Bitmap>()

        if (loadedBitmap == null) {
            Toast.makeText(this, "Image not loaded", Toast.LENGTH_SHORT).show()
            return
        }

        val imageWidth = loadedBitmap!!.width
        val imageHeight = loadedBitmap!!.height

        if (!verticalLinesXCoordinates.contains(0)) verticalLinesXCoordinates.add(0)
        if (!verticalLinesXCoordinates.contains(imageWidth)) verticalLinesXCoordinates.add(
            imageWidth
        )

        verticalLinesXCoordinates.sort()

        // Create a mutable copy of the bitmap to draw on
        val markedBitmap = loadedBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(markedBitmap)
        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }

        // Draw vertical lines on the image
        for (x in verticalLinesXCoordinates) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), imageHeight.toFloat(), paint)
        }

        // Save the marked image before slicing
        saveImageViewToFile(dir, markedBitmap, splitMarkingName, this)

        for (i in 1 until verticalLinesXCoordinates.size) {
            val startX = verticalLinesXCoordinates[i - 1]
            val endX = verticalLinesXCoordinates[i]
            val sliceWidth = endX - startX

            if (sliceWidth <= 0) continue

            val slicedBitmap =
                Bitmap.createBitmap(loadedBitmap!!, startX, 0, sliceWidth, imageHeight)
            slicedImagesBitmap.add(slicedBitmap)

            val fileName = "${sizeOfMainImageList + 1}_${splitImageCounts + i}"
            val filePath = "SPLIT_NAME" + System.currentTimeMillis() + ".jpg"
            saveImageViewToFile(dir, slicedBitmap, filePath, this)


            val roiTableIDF = "ROI_ID" + System.currentTimeMillis()
            val fileId = "SPLIT_ID" + System.currentTimeMillis()
            val volumePlotTableID = "VOL_" + System.currentTimeMillis()
            val intensityPlotTableID = "INT_" + System.currentTimeMillis()
            val plotTableID = "TAB_" + System.currentTimeMillis()
            val currentDate = Date(System.currentTimeMillis())

            val tempFileName = "TEMP$filePath"
            val mainImageBitmap = slicedBitmap

            saveImageViewToFile(dir, mainImageBitmap, tempFileName, this, true)


            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val formattedDate = sdf.format(currentDate)

            val timeStamp = formattedDate


            val s = databaseHelper!!.insertSplitImage(
                tableName, fileId, fileName,
                filePath, timeStamp, "100", "2",
                roiTableIDF, volumePlotTableID, intensityPlotTableID,
                plotTableID, intent.getStringExtra("projectDescription").toString(),
                "0", "-1000", "-1000"
            )

            if (s) {
                databaseHelper!!.createVolumePlotTable(volumePlotTableID)
                databaseHelper!!.createRfVsAreaIntensityPlotTable(intensityPlotTableID)
                databaseHelper!!.createAllDataTable(plotTableID)
                databaseHelper!!.createSpotLabelTable("LABEL_$plotTableID")
            }
        }

        Source.croppedArrayList = slicedImagesBitmap
        val intent = Intent(this, SplitImage::class.java)
        intent.putExtra("img_path", getIntent().getStringExtra("img_path").toString())
        intent.putExtra("p", "pixel")
        intent.putExtra("w", "new")
        intent.putExtra("type", "new")
        intent.putExtra("projectName", getIntent().getStringExtra("projectName"))
        intent.putExtra("projectDescription", getIntent().getStringExtra("projectDescription"))
        intent.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"))
        intent.putExtra("projectImage", getIntent().getStringExtra("projectImage"))
        intent.putExtra("contourImage", getIntent().getStringExtra("contourImage"))
        intent.putExtra("id", getIntent().getStringExtra("id"))
        intent.putExtra("splitId", getIntent().getStringExtra("splitId"))
        intent.putExtra("imageSplitAvailable", getIntent().getStringExtra("imageSplitAvailable"))
        intent.putExtra("projectNumber", getIntent().getStringExtra("projectNumber"))
        intent.putExtra("thresholdVal", getIntent().getStringExtra("thresholdVal"))
        intent.putExtra("numberOfSpots", getIntent().getStringExtra("numberOfSpots"))
        intent.putExtra("tableName", getIntent().getStringExtra("tableName"))
        intent.putExtra("roiTableID", getIntent().getStringExtra("roiTableID"))
        intent.putExtra("volumePlotTableID", "na")
        intent.putExtra("intensityPlotTableID", "na")
        intent.putExtra("plotTableID", "na")
        startActivity(intent)
        Source.fileSaved = true
        SplitImage.completed = true
        finish()
    }

    private fun saveImageViewToFile(
        dir: File,
        bitmap: Bitmap,
        fileName: String,
        context: Context,
        temp: Boolean = false
    ) {
        // Ensure the base directory exists
        if (!dir.exists()) {
            dir.mkdirs()
        }

        // Determine final output path
        val outFile: File = if (temp) {
            val tempDir = File(dir, "TEMP")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            File(tempDir, fileName)
        } else {
            File(dir, fileName)
        }

        try {
            FileOutputStream(outFile).use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            }
            Log.d("TAG", "Image saved: ${outFile.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("TAG", "Failed to save image: ${e.message}")
        }
    }

    fun saveImageToDownloads(
        originalBitmapImage: Bitmap,
        projectName: String,
        context: Context?
    ): String? {
        val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(Date())
        val fileName = projectName + "_" + timeStamp + ".jpg"

        // Get the Downloads directory
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Create the TLC_IMAGES directory inside Downloads
        val tlcImagesDir = File(downloadsDir, "TLC_IMAGES")
        if (!tlcImagesDir.exists()) {
            if (tlcImagesDir.mkdirs()) {
                Log.d("TAG", "TLC_IMAGES directory created successfully")
            } else {
                Log.e("TAG", "Failed to create TLC_IMAGES directory")
                return null
            }
        }

        // Save the image inside TLC_IMAGES directory
        val outFile = File(tlcImagesDir, fileName)

        var outStream: FileOutputStream? = null
        try {
            outStream = FileOutputStream(outFile)
            originalBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()
            Log.d("TAG", "Image saved to: " + outFile.absolutePath)

            // Show a Toast message with the file path
            Toast.makeText(
                context,
                "Saved to TLC_IMAGES: " + outFile.absolutePath,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (outStream != null) {
                try {
                    outStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return outFile.absolutePath
    }

}
