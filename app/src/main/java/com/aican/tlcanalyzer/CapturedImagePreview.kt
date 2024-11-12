package com.aican.tlcanalyzer

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCapturedImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saturationSeekBar = binding.saturationSeekBar

        supportActionBar?.hide()

        binding.back.setOnClickListener { finish() }
        img_uri = Uri.parse(intent.getStringExtra("img_path"))

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
                val intwnt = Intent(
                    this@CapturedImagePreview, CroppingTemp::class.java
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

                splitBitmap = binding.ivCrop.crop()
                splitBitmap = cropBitmapByPercentage(splitBitmap!!, 5f)

                //
                val intwnt = Intent(
                    this@CapturedImagePreview, CroppingTemp::class.java
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


            binding.btnSave.text = "No Split"
            binding.btnSave.setOnClickListener {
                Toast.makeText(this@CapturedImagePreview, "Ok", Toast.LENGTH_SHORT).show()
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

    private fun setSaturation(saturationValue: Float) {
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(saturationValue)
        val filter = ColorMatrixColorFilter(colorMatrix)
        binding.ivCrop.setColorFilter(filter)
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


    fun saveImageViewToFile(originalBitmapImage: Bitmap, fileName: String?): String? {

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