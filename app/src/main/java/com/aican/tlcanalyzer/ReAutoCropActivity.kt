package com.aican.tlcanalyzer

import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.database.UsersDatabase
import com.aican.tlcanalyzer.databinding.ActivityReAutoCropBinding
import com.aican.tlcanalyzer.utils.SharedPrefData
import com.aican.tlcanalyzer.utils.Subscription
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ReAutoCropActivity : AppCompatActivity() {

    lateinit var binding: ActivityReAutoCropBinding
    var id: String? = null
    var projectImage: String? = null

    lateinit var databaseHelper: DatabaseHelper
    lateinit var userDatabaseHelper: UsersDatabase
    var imageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReAutoCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this@ReAutoCropActivity)
        userDatabaseHelper = UsersDatabase(this@ReAutoCropActivity)


        id = intent.getStringExtra("id").toString()
        projectImage = intent.getStringExtra("projectImage").toString()


        val dir = File(
            ContextWrapper(this).externalMediaDirs[0],
            resources.getString(R.string.app_name) + id
        )

        imageName = "ORG_" + id?.replace("ID", "IMGAICAN").toString() + ".jpg"

        val outFile: File = File(dir, imageName)


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