package com.aican.tlcanalyzer

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.database.UsersDatabase
import com.aican.tlcanalyzer.databinding.ActivityCapturedImagePreviewBinding
import com.aican.tlcanalyzer.dialog.AuthDialog
import com.aican.tlcanalyzer.utils.SharedPrefData
import com.aican.tlcanalyzer.utils.Source
import com.aican.tlcanalyzer.utils.Subscription
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CapturedImagePreview : AppCompatActivity() {

    companion object {
        var splitBitmap: Bitmap? = null
        var originalBitmap: Bitmap? = null
    }

    lateinit var binding: ActivityCapturedImagePreviewBinding
    lateinit var img_uri: Uri
    lateinit var databaseHelper: DatabaseHelper
    lateinit var userDatabaseHelper: UsersDatabase
    lateinit var saturationSeekBar: SeekBar
    var sizeOfMainImageList = 0
    var sizeOfSplitImageList = 0
    private var id: String? = null
    private var projectImage: String? = null
    private var projectName: String? = null
    private var tableName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCapturedImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saturationSeekBar = binding.saturationSeekBar

        supportActionBar?.hide()

        binding.back.setOnClickListener { finish() }
        img_uri = Uri.parse(intent.getStringExtra("img_path"))
        id = intent.getStringExtra("id")
        projectImage = intent.getStringExtra("projectImage")
        projectName = intent.getStringExtra("projectName")
        tableName = intent.getStringExtra("tableName")

        databaseHelper = DatabaseHelper(this@CapturedImagePreview)
        userDatabaseHelper = UsersDatabase(this@CapturedImagePreview)

        binding.imageView.setImageURI(img_uri)
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, img_uri)

        binding.ivCrop.setImageToCrop(bitmap)

        Source.retake = false


        binding.btnRetake.setOnClickListener {
            Source.retake = true
            finish()
        }


        val dir = File(
            ContextWrapper(this).externalMediaDirs[0],
            resources.getString(R.string.app_name) + intent.getStringExtra("id").toString()
        )

        if (!SplitImage.addingMainImage) {

            binding.splitBtn.setOnClickListener {

                originalBitmap = binding.ivCrop.bitmap
                splitBitmap = binding.ivCrop.crop()

                splitBitmap = cropBitmapByPercentage(splitBitmap!!, 5f)


                //
//                val intwnt = Intent(
//                    this@CapturedImagePreview, CroppingTemp::class.java
//                )
                val intwnt = Intent(
                    this@CapturedImagePreview, NewCroppingTemp::class.java
                )
//            intwnt.putExtra("img_path", intent.getStringExtra("img_path"))
                intwnt.putExtra("p", "pixel")
                intwnt.putExtra("w", "new")
                intwnt.putExtra("type", "new")
                intwnt.putExtra("projectName", intent.getStringExtra("projectName"))
                intwnt.putExtra("projectDescription", intent.getStringExtra("projectDescription"))
                intwnt.putExtra("timeStamp", intent.getStringExtra("timeStamp"))
                intwnt.putExtra("projectImage", intent.getStringExtra("projectImage"))
                intwnt.putExtra("contourImage", intent.getStringExtra("contourImage"))
                intwnt.putExtra("id", intent.getStringExtra("id"))
                intwnt.putExtra("splitId", intent.getStringExtra("splitId"))
                intwnt.putExtra("imageSplitAvailable", intent.getStringExtra("imageSplitAvailable"))
                intwnt.putExtra("projectNumber", intent.getStringExtra("projectNumber"))
                intwnt.putExtra("thresholdVal", intent.getStringExtra("thresholdVal"))
                intwnt.putExtra("numberOfSpots", intent.getStringExtra("numberOfSpots"))
                intwnt.putExtra("tableName", intent.getStringExtra("tableName"))
                intwnt.putExtra("roiTableID", intent.getStringExtra("roiTableID"))
                intwnt.putExtra(
                    "volumePlotTableID", intent.getStringExtra("volumePlotTableID")
                )
                intwnt.putExtra(
                    "intensityPlotTableID", intent.getStringExtra("intensityPlotTableID")
                )
                intwnt.putExtra(
                    "plotTableID", intent.getStringExtra("plotTableID")
                )
                startActivity(intwnt)
            }


            binding.btnSave.setOnClickListener {
                val outFile: File = File(dir, intent.getStringExtra("projectImage").toString())
                var originalImageBit = binding.ivCrop.bitmap
                var sBit = binding.ivCrop.crop()
                sBit = cropBitmapByPercentage(sBit!!, 5f)

                var uriPath: Uri? = null

                if (!outFile.exists()) {

                    saveImageToDownloads(
                        originalImageBit,
                        intent.getStringExtra("projectName").toString(),
                        this
                    )

                    //original image
                    saveImageViewToFile(
                        originalImageBit, "ORG_" +
                                intent.getStringExtra("projectImage").toString()
                    )

                    saveImageViewToFile(sBit, intent.getStringExtra("projectImage").toString())


                    uriPath = Uri.fromFile(outFile)


                }

                SharedPrefData.saveData(
                    this,
                    SharedPrefData.PR_ACTUAL_LIMIT_KEY,
                    (Subscription.NO_OF_PROJECTS_MADE + 1).toString()
                )

                userDatabaseHelper.logUserAction(
                    AuthDialog.activeUserName,
                    AuthDialog.activeUserRole,
                    "Created Normal Project",
                    intent.getStringExtra("projectName").toString(),
                    intent.getStringExtra("id").toString(),
                    "normal"
                )

                val volumePlotTableID = "VOL_" + System.currentTimeMillis()
                val intensityPlotTableID = "INT_" + System.currentTimeMillis()
                val plotTableID = "TAB_" + System.currentTimeMillis()


                val i = databaseHelper.insertData(
                    intent.getStringExtra("id"),
                    intent.getStringExtra("projectName"),
                    intent.getStringExtra("projectDescription"),
                    intent.getStringExtra("timeStamp"),
                    intent.getStringExtra("projectImage"),
                    "na",
                    intent.getStringExtra("splitId"),
                    "false",
                    intent.getStringExtra("splitId"),
                    "100",
                    "1",
                    intent.getStringExtra("tableName"),
                    intent.getStringExtra("roiTableID"),
                    volumePlotTableID,
                    intensityPlotTableID,
                    plotTableID,
                    "-1000",
                    "-1000"
                )

                if (i) {

                    databaseHelper.createVolumePlotTable(volumePlotTableID)
                    databaseHelper.createRfVsAreaIntensityPlotTable(intensityPlotTableID)
                    databaseHelper.createAllDataTable(plotTableID)
                    databaseHelper.createSpotLabelTable("LABEL_$plotTableID")

                    val intwnt = Intent(
//                        this@CapturedImagePreview, ImageAnalysis::class.java
                        this@CapturedImagePreview, NewImageAnalysis::class.java
                    )
                    intwnt.putExtra("img_path", uriPath.toString())
//                intwnt.putExtra("img_path", intent.getStringExtra("img_path"))
                    intwnt.putExtra("p", "pixel")
                    intwnt.putExtra("w", "new")
                    intwnt.putExtra("type", "new")
                    intwnt.putExtra("rmSpot", "-1000")
                    intwnt.putExtra("finalSpot", "-1000")
                    intwnt.putExtra("projectName", intent.getStringExtra("projectName"))
                    intwnt.putExtra(
                        "projectDescription", intent.getStringExtra("projectDescription")
                    )
                    intwnt.putExtra("timeStamp", intent.getStringExtra("timeStamp"))
                    intwnt.putExtra("projectImage", intent.getStringExtra("projectImage"))
                    intwnt.putExtra("contourImage", intent.getStringExtra("contourImage"))
                    intwnt.putExtra("id", intent.getStringExtra("id"))
                    intwnt.putExtra("splitId", intent.getStringExtra("splitId"))
                    intwnt.putExtra("imageSplitAvailable", "false")
                    intwnt.putExtra("projectNumber", intent.getStringExtra("projectNumber"))
                    intwnt.putExtra("thresholdVal", "100")
                    intwnt.putExtra("numberOfSpots", "1")
                    intwnt.putExtra("tableName", intent.getStringExtra("tableName"))
                    intwnt.putExtra("roiTableID", intent.getStringExtra("roiTableID"))
                    intwnt.putExtra(
                        "volumePlotTableID", volumePlotTableID,
                    )
                    intwnt.putExtra(
                        "intensityPlotTableID", intensityPlotTableID,
                    )
                    intwnt.putExtra(
                        "plotTableID", plotTableID,
                    )
                    startActivity(intwnt)
                    finish()
                }

            }

        } else {

            binding.splitBtn.setOnClickListener {


                originalBitmap = binding.ivCrop.bitmap
                splitBitmap = binding.ivCrop.crop()

                splitBitmap = cropBitmapByPercentage(splitBitmap!!, 5f)


//                splitBitmap = binding.ivCrop.crop()
//                splitBitmap = cropBitmapByPercentage(splitBitmap!!, 5f)

                //
//                val intwnt = Intent(
//                    this@CapturedImagePreview, CroppingTemp::class.java
//                )
                val intwnt = Intent(
                    this@CapturedImagePreview, NewCroppingTemp::class.java
                )
//            intwnt.putExtra("img_path", intent.getStringExtra("img_path"))
                intwnt.putExtra("p", "pixel")
                intwnt.putExtra("w", "new")
                intwnt.putExtra("type", "new")
                intwnt.putExtra("projectName", intent.getStringExtra("projectName"))
                intwnt.putExtra("projectDescription", intent.getStringExtra("projectDescription"))
                intwnt.putExtra("timeStamp", intent.getStringExtra("timeStamp"))
                intwnt.putExtra("projectImage", intent.getStringExtra("projectImage"))
                intwnt.putExtra("contourImage", intent.getStringExtra("contourImage"))
                intwnt.putExtra("id", intent.getStringExtra("id"))
                intwnt.putExtra("splitId", intent.getStringExtra("splitId"))
                intwnt.putExtra("imageSplitAvailable", intent.getStringExtra("imageSplitAvailable"))
                intwnt.putExtra("projectNumber", intent.getStringExtra("projectNumber"))
                intwnt.putExtra("thresholdVal", intent.getStringExtra("thresholdVal"))
                intwnt.putExtra("numberOfSpots", intent.getStringExtra("numberOfSpots"))
                intwnt.putExtra("tableName", intent.getStringExtra("tableName"))
                intwnt.putExtra("roiTableID", intent.getStringExtra("roiTableID"))
                intwnt.putExtra(
                    "volumePlotTableID", intent.getStringExtra("volumePlotTableID")
                )
                intwnt.putExtra(
                    "intensityPlotTableID", intent.getStringExtra("intensityPlotTableID")
                )
                intwnt.putExtra(
                    "plotTableID", intent.getStringExtra("plotTableID")
                )
                startActivity(intwnt)

            }
            sizeOfMainImageList = SplitImage.sizeOfMainImagesList
            sizeOfSplitImageList = SplitImage.sizeOfSplitImageList
            // save image with single split and single main image
            binding.btnSave.text = "No Split"
            binding.btnSave.setOnClickListener {
                originalBitmap = binding.ivCrop.bitmap
                splitBitmap = binding.ivCrop.crop()

                splitBitmap = cropBitmapByPercentage(splitBitmap!!, 5f)


                if (splitBitmap != null) {
                    saveNoSplit(splitBitmap!!)
                } else {
                    Toast.makeText(this, "Image not found or loaded", Toast.LENGTH_SHORT).show()
                }

            }
        }

        saturationSeekBar.progress = 50

        saturationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val saturationValue = progress / 100f
                setSaturation(saturationValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed for this example
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed for this example
            }
        })


    }

    private fun saveNoSplit(imageBitmap: Bitmap) {
        saveMainImageViewToFile(
            imageBitmap!!,
            "${projectImage}_$sizeOfMainImageList",
            this@CapturedImagePreview
        )

        val tempFileName = "TEMP${projectImage}_$sizeOfMainImageList"
        val originalFileName = "ORG_${projectImage}_$sizeOfMainImageList"

        saveMainImageViewToFile(imageBitmap!!, projectName!!, this@CapturedImagePreview)

        saveMainImageViewToFile(imageBitmap!!, tempFileName, this@CapturedImagePreview)
        saveMainImageViewToFile(
            CapturedImagePreview.originalBitmap!!,
            originalFileName,
            this@CapturedImagePreview
        )


        val idm = "SPLIT_ID" + System.currentTimeMillis()
        val timestamp =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val volumePlotTableID = "VOL_" + System.currentTimeMillis()
        val intensityPlotTableID = "INT_" + System.currentTimeMillis()
        val plotTableID = "TAB_" + System.currentTimeMillis()

        val success = databaseHelper!!.insertSplitMainImage(
            "MAIN_IMG_$id", idm,
            "Main Image ${sizeOfMainImageList + 1}", "${projectImage}_$sizeOfMainImageList",
            timestamp, "100", "2",
            intent.getStringExtra("roiTableID"),
            volumePlotTableID,
            intensityPlotTableID,
            plotTableID,
            intent.getStringExtra("projectDescription"), "0", "-1000", "-1000"
        )

        databaseHelper!!.createVolumePlotTable(volumePlotTableID)
        databaseHelper!!.createRfVsAreaIntensityPlotTable(intensityPlotTableID)
        databaseHelper!!.createAllDataTable(plotTableID)
        databaseHelper!!.createSpotLabelTable("LABEL_$plotTableID")
        if (success) {


            val fileName = "MI${sizeOfMainImageList + 1} -> Image ${sizeOfSplitImageList + 1}"
            val filePath = "SPLIT_NAME" + System.currentTimeMillis() + ".jpg"
            saveMainImageViewToFile(imageBitmap, filePath, this@CapturedImagePreview)

            val roiTableIDF = "ROI_ID" + System.currentTimeMillis()
            val fileId = "SPLIT_ID" + System.currentTimeMillis()
            val volumePlotTableID = "VOL_" + System.currentTimeMillis()
            val intensityPlotTableID = "INT_" + System.currentTimeMillis()
            val plotTableID = "TAB_" + System.currentTimeMillis()
            val currentDate = Date(System.currentTimeMillis())

            val tempFileName = "TEMP$filePath"
            val mainImageBitmap = imageBitmap

            saveMainImageViewToFile(mainImageBitmap, tempFileName, this)


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

                val intent = Intent(this, SplitImage::class.java)
                intent.putExtra("img_path", getIntent().getStringExtra("img_path").toString())
                intent.putExtra("p", "pixel")
                intent.putExtra("w", "new")
                intent.putExtra("type", "new")
                intent.putExtra("projectName", getIntent().getStringExtra("projectName"))
                intent.putExtra(
                    "projectDescription",
                    getIntent().getStringExtra("projectDescription")
                )
                intent.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"))
                intent.putExtra("projectImage", getIntent().getStringExtra("projectImage"))
                intent.putExtra("contourImage", getIntent().getStringExtra("contourImage"))
                intent.putExtra("id", getIntent().getStringExtra("id"))
                intent.putExtra("splitId", getIntent().getStringExtra("splitId"))
                intent.putExtra(
                    "imageSplitAvailable",
                    getIntent().getStringExtra("imageSplitAvailable")
                )
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
        }
    }

    private fun saveMainImageViewToFile(bitmap: Bitmap, fileName: String, context: Context) {
        val dir = File(
            ContextWrapper(context).externalMediaDirs[0],
            context.getString(R.string.app_name) + id
        )
        dir.mkdirs()
        val outFile = File(dir, fileName)

        try {
            FileOutputStream(outFile).use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            }
            Log.d("TAG", "Image saved: ${outFile.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setSaturation(saturationValue: Float) {
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(saturationValue)
        val filter = ColorMatrixColorFilter(colorMatrix)
        binding.ivCrop.colorFilter = filter
    }

    fun cropBitmapByPercentage(originalBitmap: Bitmap, percentage: Float): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val left = (width * (percentage / 100)).toInt() // Percentage of the width
        val top = (height * (percentage / 100)).toInt() // Percentage of the height
        val right = width - left
        val bottom = height - top
        return Bitmap.createBitmap(originalBitmap, left, top, right - left, bottom - top)
    }

    fun saveImageToDownloads(
        originalBitmapImage: Bitmap,
        projectName: String,
        context: Context?
    ): String? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
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

    private fun saveImageViewToFile(originalBitmapImage: Bitmap, fileName: String?): String? {

//        if (originalBitmapImage.getWidth() != originalBitmapImage.getHeight()) {
//            originalBitmapImage = convertToSquareWithTransparentBackground(originalBitmapImage);
//        }
        var outStream: FileOutputStream? = null

        // Write to SD Card
        try {
            val sdCard = Environment.getExternalStorageDirectory()
            val dir = File(
                ContextWrapper(this).externalMediaDirs[0],
                resources.getString(R.string.app_name) + intent.getStringExtra("id")
            )

            dir.mkdirs()
            val outFile = File(dir, fileName)
            outStream = FileOutputStream(outFile)
            originalBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()
            Log.d("TAG", "onPictureTaken - wrote to " + outFile.absolutePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return fileName
    }


    override fun onResume() {
        super.onResume()

        if (Source.fileSaved) {
            Source.fileSaved = false
            finish()
        }
        if (SplitImage.completed) {
            finish()
        }


    }


}