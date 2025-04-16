package com.aican.tlcanalyzer

import android.content.ContextWrapper
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.adapterClasses.SplitAdapter
import com.aican.tlcanalyzer.adapterClasses.SplitMainImageAdtr
import com.aican.tlcanalyzer.cropper.CropImage
import com.aican.tlcanalyzer.cropper.CropImageView
import com.aican.tlcanalyzer.dataClasses.SplitData
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.database.UsersDatabase
import com.aican.tlcanalyzer.databinding.ActivitySplitImageBinding
import com.aican.tlcanalyzer.dialog.AuthDialog
import com.aican.tlcanalyzer.settingActivities.SplitSettings
import com.aican.tlcanalyzer.utils.SharedPrefData
import com.aican.tlcanalyzer.utils.Source
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SplitImage : AppCompatActivity(), AuthDialog.AuthCallback {

    lateinit var binding: ActivitySplitImageBinding
    lateinit var img_uri: Uri
    lateinit var id: String
    lateinit var databaseHelper: DatabaseHelper
    lateinit var arrayList: ArrayList<SplitData>
    lateinit var mainImageArrayList: ArrayList<SplitData>
    lateinit var duplicateArrayList: ArrayList<SplitData>
    lateinit var adapter: SplitAdapter
    lateinit var work: String
    var works = arrayOf("new", "existing")
    lateinit var tableName: String
    lateinit var projectDescription: String
    lateinit var projectName: String
    lateinit var projectImage: String
    lateinit var roiTableID: String
    lateinit var userDatabase: UsersDatabase

    lateinit var mainImageTableID: String
    var index = 0

    companion object {

        final var addingMainImage = false
        var sizeOfMainImagesList = 0
        var sizeOfSplitImageList = 0
        var completed = false

        var completedSplit = false
        var INTENSITY_PART_KEY = ""

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplitImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.back.setOnClickListener {
            finish()
        }

        databaseHelper = DatabaseHelper(this)
        img_uri = Uri.parse(intent.getStringExtra("img_path"))
        projectDescription = intent.getStringExtra("projectDescription").toString()
        roiTableID = intent.getStringExtra("roiTableID").toString()
        id = intent.getStringExtra("id").toString()
        projectImage = intent.getStringExtra("projectImage").toString()
        work = intent.getStringExtra("w").toString()
        tableName = intent.getStringExtra("tableName").toString()
        projectName = intent.getStringExtra("projectName").toString()
        binding.projectName.setText(projectName)

        userDatabase = UsersDatabase(this@SplitImage)


        mainImageTableID = "MAIN_IMG_$id"

        if (Source.cfrStatus) {

            AuthDialog.authDialog(this@SplitImage, false, false, userDatabase, this)
        } else {
            onAuthenticationSuccess()
        }
//        val newColumnNames = arrayOf("volumePlotTableID TEXT", "intensityPlotTableID TEXT", "plotTableID TEXT")

        val arrayOfNewColumns: ArrayList<String> = ArrayList()
        arrayOfNewColumns.add("volumePlotTableID TEXT")
        arrayOfNewColumns.add("intensityPlotTableID TEXT")
        arrayOfNewColumns.add("plotTableID TEXT")

//
//        volumePlotTableID" + " TEXT DEFAULT 'na';");
//        db.execSQL("ALTER TABLE " + "ProjectDetails" + " ADD COLUMN " + "intensityPlotTableID" + " TEXT DEFAULT 'na';");
//        db.execSQL("ALTER TABLE " + "ProjectDetails" + " ADD COLUMN " + "plotTableID"

//        if (Source.oldVersion < Source.newVersion) {
//            databaseHelper.addColumnsToTable(tableName, arrayOfNewColumns)
//        }

//        val dir = File(
//            ContextWrapper(this).externalMediaDirs[0], getString(R.string.app_name) + id
//        )

        var dir = Source.getSplitFolderFile(
            this,
            intent.getStringExtra("projectName"),
            intent.getStringExtra("id")
        )

        val file = File(dir, projectImage)
        if (!file.exists()) {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, img_uri)

            saveImageViewToFile(dir, bitmap, projectImage)
        } else {

        }
//        Source.checkInternet(this@SplitImage)

        if (work.equals(works[1])) {


            val outFile = File(dir, projectImage)

            if (outFile.exists()) {
                val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)

                binding.mainImage.setImageBitmap(myBitmap)
            }
        } else {
            binding.mainImage.setImageBitmap(CapturedImagePreview.splitBitmap)
        }

        binding.addSplit.setOnClickListener {
            if (work == works[0]) {
                CropImage.activity(img_uri).start(this@SplitImage)
            }
            if (work == works[1]) {


                val outFile = File(dir, projectImage)

                if (outFile.exists()) {

                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(outFile.path, options)
                    val imageWidth = options.outWidth
                    val imageHeight = options.outHeight
                    val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()


                    CropImage.activity(Uri.fromFile(outFile))
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this@SplitImage)

                }
            }
        }



        setArrayList(dir)
//        Source.toast(this, tableName)


        // save data end
        Log.e("Vol", getIntent().getStringExtra("volumePlotTableID").toString())
        Log.e("int", getIntent().getStringExtra("intensityPlotTableID").toString())
        Log.e("plotTableID", getIntent().getStringExtra("plotTableID").toString())

        binding.checkAnalysis.setOnClickListener {

            val cursor: Cursor = databaseHelper.getSplitTableData(tableName)

            cursor.moveToPosition(index)

            if (duplicateArrayList.size >= 0 && cursor != null) {

                AuthDialog.projectType = "Split"
                AuthDialog.projectName = projectName + " -> " + cursor.getString(1).toString()
                AuthDialog.projectID = id


//                val intent = Intent(this@SplitImage, ImageAnalysis::class.java)
                val intent = Intent(this@SplitImage, NewImageAnalysis::class.java)

                intent.putExtra("w", "split")
                intent.putExtra("projectName", projectName)
                intent.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"))
                intent.putExtra("img_path", file.path)
                intent.putExtra("type", "mainImg")
                intent.putExtra("projectDescription", cursor.getString(11))
                intent.putExtra("projectImage", cursor.getString(1))
                intent.putExtra("imageName", cursor.getString(1))
                intent.putExtra("roiTableID", roiTableID)
                intent.putExtra("tableName", tableName)
                intent.putExtra("thresholdVal", cursor.getString(4))
                intent.putExtra("numberOfSpots", cursor.getString(5))
                intent.putExtra("id", id)
                intent.putExtra("pid", cursor.getString(0))
                intent.putExtra(
                    "volumePlotTableID", cursor.getString(8),
                )
                intent.putExtra(
                    "intensityPlotTableID",
                    cursor.getString(9),
                )
                intent.putExtra(
                    "plotTableID", cursor.getString(10),
                )
                intent.putExtra(
                    "hour", cursor.getString(12),
                )
                intent.putExtra(
                    "rmSpot", cursor.getString(13),
                )
                intent.putExtra(
                    "finalSpot", cursor.getString(14),
                )
                startActivity(intent)
            }
        }


        binding.floatingActionBtn!!.setOnClickListener {

            if (!arrayList.isNullOrEmpty()) {
                PlotMultipleIntensity.splitImageArrayList = arrayList


                val i = Intent(this@SplitImage, PlotMultipleIntensity::class.java)
                i.putExtra("w", "split")
                i.putExtra("img_path", intent.getStringExtra("img_path"))
                i.putExtra("projectName", intent.getStringExtra("projectName"))
                i.putExtra("projectDescription", intent.getStringExtra("projectDescription"))
                i.putExtra("projectImage", intent.getStringExtra("projectImage"))
                i.putExtra("projectNumber", intent.getStringExtra("projectNumber"))
                i.putExtra("imageName", intent.getStringExtra("imageName"))
                i.putExtra("splitId", intent.getStringExtra("splitId"))
                i.putExtra("tableName", tableName)
                i.putExtra("roiTableID", intent.getStringExtra("roiTableID"))
                i.putExtra("thresholdVal", intent.getStringExtra("thresholdVal"))
                i.putExtra("numberOfSpots", intent.getStringExtra("numberOfSpots"))
                i.putExtra("id", intent.getStringExtra("id"))
                i.putExtra("pid", intent.getStringExtra("pid"))
                i.putExtra(
                    "volumePlotTableID", intent.getStringExtra("volumePlotTableID"),
                )
                i.putExtra(
                    "intensityPlotTableID", intent.getStringExtra("intensityPlotTableID"),
                )
                i.putExtra(
                    "plotTableID", intent.getStringExtra("plotTableID"),
                )
                startActivity(i)

            } else {

            }
        }


        setMainImageArrayList(dir)

        binding.addMainImage.setOnClickListener {

            addingMainImage = true
            Source.retake = true
            completedSplit = true

            val i = Intent(this@SplitImage, CameraActivity::class.java)
            i.putExtra("w", "split")
            i.putExtra("img_path", intent.getStringExtra("img_path"))
            i.putExtra("projectName", intent.getStringExtra("projectName"))
            i.putExtra("projectDescription", intent.getStringExtra("projectDescription"))
            i.putExtra("projectImage", intent.getStringExtra("projectImage"))
            i.putExtra("projectNumber", intent.getStringExtra("projectNumber"))
            i.putExtra("imageName", intent.getStringExtra("imageName"))
            i.putExtra("splitId", intent.getStringExtra("splitId"))
            i.putExtra("tableName", tableName)
            i.putExtra("roiTableID", intent.getStringExtra("roiTableID"))
            i.putExtra("thresholdVal", intent.getStringExtra("thresholdVal"))
            i.putExtra("numberOfSpots", intent.getStringExtra("numberOfSpots"))
            i.putExtra("id", intent.getStringExtra("id"))
            i.putExtra("pid", intent.getStringExtra("pid"))
            i.putExtra(
                "volumePlotTableID", intent.getStringExtra("volumePlotTableID"),
            )
            i.putExtra(
                "intensityPlotTableID", intent.getStringExtra("intensityPlotTableID"),
            )
            i.putExtra(
                "plotTableID", intent.getStringExtra("plotTableID"),
            )
            startActivity(i)

        }

        binding.splitSettings.setOnClickListener {
            val inet = Intent(this@SplitImage, SplitSettings::class.java)
            inet.putExtra("id", intent.getStringExtra("id"))
            inet.putExtra("projectName", intent.getStringExtra("projectName"))

            startActivity(inet)
        }

    }

    private fun setMainImageArrayList(dir: File) {
        mainImageArrayList = ArrayList()
        val cursor: Cursor = databaseHelper.getSplitTableData(mainImageTableID)
        if (cursor.moveToFirst()) {
            do {
                val note = SplitData(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    roiTableID,
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getString(11),
                    cursor.getString(12),
                    cursor.getString(13),
                    cursor.getString(14)
                )
                mainImageArrayList.add(note)
            } while (cursor.moveToNext())
        }

        sizeOfMainImagesList = mainImageArrayList.size

        var splitMainAdapter = SplitMainImageAdtr(
            dir,
            this,
            mainImageArrayList,
            databaseHelper,
            id,
            projectName,
            projectDescription,
            mainImageTableID,
            "na",
            getIntent().getStringExtra("timeStamp").toString(),
            getIntent().getStringExtra("projectNumber").toString(),
            getIntent().getStringExtra("splitId").toString()
        )
        binding.mainImageRecView.adapter = splitMainAdapter
        splitMainAdapter.notifyDataSetChanged()


    }

    private fun setArrayList(dir: File) {
        arrayList = ArrayList()
        duplicateArrayList = ArrayList()


        val cursor: Cursor = databaseHelper.getSplitTableData(tableName)

        if (cursor.moveToFirst()) {
            do {
                val note = SplitData(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    roiTableID,
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getString(11),
                    cursor.getString(12),
                    cursor.getString(13),
                    cursor.getString(14)
                )
                arrayList.add(note)
            } while (cursor.moveToNext())
        }

        duplicateArrayList = arrayList

        var isSourceImg = false;
        var i = 0
        for (a in arrayList) {
            if (a.imageName.equals(projectImage)) {
                isSourceImg = true
                index = i
                break
            }
            i++
        }

        if (!isSourceImg) {
            println("Main image not found hello ")
            val filePath = "SPLIT_NAME" + System.currentTimeMillis() + ".jpg"
            val fileId = "SPLIT_ID" + System.currentTimeMillis()
            val fileName = projectImage

            val currentTimeMillis = System.currentTimeMillis()

            val currentDate = Date(currentTimeMillis)

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val formattedDate: String? = sdf.format(currentDate)

            val timeStamp = formattedDate
            val volumePlotTableID = "VOL_" + System.currentTimeMillis()
            val intensityPlotTableID = "INT_" + System.currentTimeMillis()
            val plotTableID = "TAB_" + System.currentTimeMillis()

            val s = databaseHelper.insertSplitImage(
                tableName,
                fileId,
                fileName,
                filePath,
                timeStamp,
                "100",
                "2",
                roiTableID,
                volumePlotTableID,
                intensityPlotTableID,
                plotTableID,
                projectDescription,
                "0",
                "0",
                "0"
            )

            val r = databaseHelper.insertSplitMainImage(
                mainImageTableID,
                fileId,
                "Main Image 1",
                projectImage,
                timeStamp,
                "100",
                "2",
                roiTableID,
                volumePlotTableID,
                intensityPlotTableID,
                plotTableID,
                projectDescription,
                "0",
                "0",
                "0"
            )

            if (s && r) {
                databaseHelper.createVolumePlotTable(volumePlotTableID)
                databaseHelper.createRfVsAreaIntensityPlotTable(intensityPlotTableID)
                databaseHelper.createAllDataTable(plotTableID)
                databaseHelper.createSpotLabelTable("LABEL_$plotTableID")
            }


        } else {
            arrayList.removeAt(index)
        }

        Source.splitDataArrayList = ArrayList()

        Source.splitDataArrayList = arrayList

        adapter = SplitAdapter(
            dir,
            this,
            arrayList,
            databaseHelper,
            id,
            projectName,
            projectDescription,
            tableName,
            "na",
            getIntent().getStringExtra("timeStamp").toString(),
            getIntent().getStringExtra("projectNumber").toString(),
            getIntent().getStringExtra("splitId").toString()
        )
        sizeOfSplitImageList = arrayList.size
        binding.recView.adapter = adapter
        adapter.notifyDataSetChanged()


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri


                var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)



                openDialog(bitmap)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    fun saveImageViewToFile(dir: File, originalBitmapImage: Bitmap, fileName: String): String? {

        var outStream: FileOutputStream? = null

        try {
            val sdCard = Environment.getExternalStorageDirectory()

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
        } finally {
        }
        return fileName
    }

    private fun openDialog(bitmap: Bitmap) {

        val dialogView = layoutInflater.inflate(R.layout.split_img_name, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)

        var dir = Source.getSplitFolderFile(
            this,
            intent.getStringExtra("projectName"),
            intent.getStringExtra("id")
        )

        val alertDialog = builder.create()

        val imageNameD = dialogView.findViewById<EditText>(R.id.imageNameD)

        imageNameD.setText("Image " + (arrayList.size + 1))

        dialogView.findViewById<MaterialButton>(R.id.submitBtnD).setOnClickListener {

            val roiTableIDF = "ROI_ID" + System.currentTimeMillis().toString()


            val filePath = "SPLIT_NAME" + System.currentTimeMillis() + ".jpg"
            val fileId = "SPLIT_ID" + System.currentTimeMillis()
            val fileName = imageNameD.text.toString()

            val currentTimeMillis = System.currentTimeMillis()

            val currentDate = Date(currentTimeMillis)

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val formattedDate: String? = sdf.format(currentDate)

            val timeStamp = formattedDate

            val volumePlotTableID = "VOL_" + System.currentTimeMillis()
            val intensityPlotTableID = "INT_" + System.currentTimeMillis()
            val plotTableID = "TAB_" + System.currentTimeMillis()


            saveImageViewToFile(dir, bitmap, filePath)

            databaseHelper.insertSplitImage(
                tableName,
                fileId,
                fileName,
                filePath,
                timeStamp,
                "100",
                "2",
                roiTableIDF,
                volumePlotTableID,
                intensityPlotTableID,
                plotTableID,
                projectDescription,
                "0",
                "0",
                "0"

            )

            setArrayList(dir)
            alertDialog.dismiss()
        }

        alertDialog.show()


    }

    override fun onResume() {
        super.onResume()
//        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        INTENSITY_PART_KEY = "INTENSITY_PART_KEY_" + id
        var dir = Source.getSplitFolderFile(
            this,
            intent.getStringExtra("projectName"),
            intent.getStringExtra("id")
        )

        if (SharedPrefData.getSavedData(
                this@SplitImage,
                SplitImage.INTENSITY_PART_KEY
            ) != null && SharedPrefData.getSavedData(
                this@SplitImage,
                SplitImage.INTENSITY_PART_KEY
            ) != ""
        ) {
            val data =
                SharedPrefData.getSavedData(this@SplitImage, SplitImage.INTENSITY_PART_KEY)

            Source.PARTS_INTENSITY = data.toInt()

        } else {
            SharedPrefData.saveData(this@SplitImage, SplitImage.INTENSITY_PART_KEY, "1000")
            Source.PARTS_INTENSITY = 1000
        }

//        if (ImageAnalysis.refreshMainSplitImage == 1) {
        if (NewImageAnalysis.refreshMainSplitImage == 1) {
//            ImageAnalysis.refreshMainSplitImage = 0
            NewImageAnalysis.refreshMainSplitImage = 0
            var dir = Source.getSplitFolderFile(
                this,
                intent.getStringExtra("projectName"),
                intent.getStringExtra("id")
            )

            val outFile = File(dir, projectImage)

            if (outFile.exists()) {
                val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)

                binding.mainImage.setImageBitmap(myBitmap)
            }
        }

        AuthDialog.projectType = "Split"

        setArrayList(dir)
        setMainImageArrayList(dir)


        if (completedSplit && completed) {
            completedSplit = false
            completed = false
            finish()
        }

//        Source.toast(this@SplitImage,Source.)
//        Source.intensityArrayList = ArrayList()
    }

    override fun onAuthenticationSuccess() {

        userDatabase.logUserAction(
            AuthDialog.activeUserName,
            AuthDialog.activeUserRole,
            "Open all splits activity page",
            intent.getStringExtra("projectName").toString(),
            intent.getStringExtra("id").toString(),
            AuthDialog.projectType

        )

    }

}