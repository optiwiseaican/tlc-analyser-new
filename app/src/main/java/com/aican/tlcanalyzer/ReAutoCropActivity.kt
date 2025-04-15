package com.aican.tlcanalyzer

import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.database.UsersDatabase
import com.aican.tlcanalyzer.databinding.ActivityReAutoCropBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ReAutoCropActivity : AppCompatActivity() {

    lateinit var binding: ActivityReAutoCropBinding
    var id: String? = null
    var projectImage: String? = null
    var projectName: String? = null

    lateinit var databaseHelper: DatabaseHelper
    lateinit var userDatabaseHelper: UsersDatabase
    var newImageName: String? = null
    var imageName: String? = null
    var type = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReAutoCropBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        databaseHelper = DatabaseHelper(this@ReAutoCropActivity)
        userDatabaseHelper = UsersDatabase(this@ReAutoCropActivity)

        type = getIntent().getStringExtra("type").toString()
        id = intent.getStringExtra("id").toString()
        projectImage = intent.getStringExtra("projectImage").toString()
        projectName = intent.getStringExtra("projectName").toString()
        imageName = intent.getStringExtra("imageName").toString()

//        binding.projectName.text = imageName.toString()
        binding.back.setOnClickListener(View.OnClickListener { finish() })
        val dir = File(
            ContextWrapper(this).externalMediaDirs[0],
            resources.getString(R.string.app_name) + id
        )


        if (type == "multi") {
            newImageName = "ORG_" + projectImage?.replace("ID", "IMGAICAN").toString()

        } else if (type == "parts") {
            newImageName = id?.replace("ID", "IMGAICAN").toString() + ".jpg"

        } else {
            newImageName = "ORG_" + id?.replace("ID", "IMGAICAN").toString() + ".jpg"

        }

        val outFile: File = File(dir, newImageName)

        println("Image Full Path: " + outFile.path + "    -----   " + outFile.absolutePath)

        if (outFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)
            binding.ivCrop.setImageToCrop(myBitmap)

        }


        binding.reEditSaveBtn.setOnClickListener {
            val outFile: File = File(dir, projectImage?.toString())
            var sBit = binding.ivCrop.crop()

//
//            if (!outFile.exists()) {

            saveImageViewToFile(sBit, projectImage)


//            }


            finish()


        }


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
                resources.getString(R.string.app_name) + id
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

}