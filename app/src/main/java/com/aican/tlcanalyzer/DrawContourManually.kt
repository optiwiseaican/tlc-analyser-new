package com.aican.tlcanalyzer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.aican.tlcanalyzer.cropper.CropImage
import com.aican.tlcanalyzer.cropper.CropImageView
import com.aican.tlcanalyzer.dataClasses.ContourData
import com.aican.tlcanalyzer.dataClasses.RFvsArea
import com.aican.tlcanalyzer.utils.Source
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.Stack


class DrawContourManually : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var undo: Button
    private lateinit var findAll: Button
    private lateinit var done: Button
    private lateinit var drawSpot: Button
    private lateinit var dataOf: TextView
    var x = 0
    var y = 0
    var w = 0
    var h = 0
    lateinit var imageFileName: String
    lateinit var id: String
    lateinit var dir: File
    private val drawnShapesStack: Stack<Drawable> = Stack()
    lateinit var uri: Uri
    lateinit var bit: Bitmap
    var k = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw_contour_manually)
//        createBitmap()

        imageView = findViewById<ImageView>(R.id.imageView)
        drawSpot = findViewById(R.id.drawSpot)
        undo = findViewById(R.id.undo)
        dataOf = findViewById(R.id.dataOf)
        findAll = findViewById(R.id.findAll)
        done = findViewById(R.id.done)
        imageView.setImageURI(Source.contourUri)

        uri = Source.contourUri
        bit = Source.contourBitmap

        drawSpot.text = "Draw spot no. ${Source.contourDataArrayList.size + 1}"

        id = intent.getStringExtra("id").toString()
        imageFileName = intent.getStringExtra("imageFileName").toString()

        dir = File(
            ContextWrapper(this).externalMediaDirs[0],
            resources.getString(R.string.app_name) + id
        )


        drawSpot.setOnClickListener {

            val dialogView = layoutInflater.inflate(R.layout.crop_options, null)
            val builder = AlertDialog.Builder(this@DrawContourManually)
                .setView(dialogView)


            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

            val rectangleCont = dialogView.findViewById<MaterialButton>(R.id.rectangleCont)
            val circleCont = dialogView.findViewById<MaterialButton>(R.id.circleCont)

            rectangleCont.setOnClickListener {
                Source.shape = 0
                alertDialog.dismiss()
                CropImage.activity(Source.contourUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)

                    .start(this@DrawContourManually)
            }

            circleCont.setOnClickListener {
                Source.shape = 1
                alertDialog.dismiss()

                CropImage.activity(Source.contourUri)
                    .setGuidelines(CropImageView.Guidelines.ON)

                    .setCropShape(CropImageView.CropShape.OVAL)

                    .start(this@DrawContourManually)
            }


            alertDialog.show()


        }




        undo.setOnClickListener {
            undoLastDrawnShape()
        }



        done.setOnClickListener {
            Source.manual = true
            finish()
        }

    }

    @SuppressLint("DefaultLocale")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val roi = result.cropRect

                lateinit var bit: Bitmap

                if (Source.shape == 1) {

                    bit = RegionOfInterest.drawOvalWithROI(
                        Source.contourBitmap,
                        roi.left,
                        roi.top,
                        roi.width(),
                        roi.height()
                    )
                }

                if (Source.shape == 0) {

                    bit = RegionOfInterest.drawRectWithROI(
                        Source.contourBitmap,
                        roi.left,
                        roi.top,
                        roi.width(),
                        roi.height()
                    )
                }

                x = roi.left
                y = roi.top
                w = roi.width()
                h = roi.height()

                val regionLeft = x
                val regionTop = y
4
                val paint = Paint().apply {
                    color = Color.RED
                    textSize = 30f
                    style = Paint.Style.FILL
                }

                val canvas = Canvas(bit)

                canvas.drawText(
                    " " + (Source.contourDataArrayList.size + 1),
                    regionLeft.toFloat(),
                    regionTop.toFloat(),
                    paint
                )

                val df = DecimalFormat("0.00E0")


                val idOf = Source.contourDataArrayList.size + 1

                val area = RegionOfInterest.calculateOvalArea(w, h)

                val solventFrontDistance =
                    w.toDouble() / 2 * (1.0 - 100.0 / 255.0)

                val contourDistance =
                    Math.sqrt(Math.pow(x.toDouble(), 2.0) + Math.pow(y.toDouble(), 2.0))

                val number = area * Math.abs(solventFrontDistance - contourDistance)
                println(df.format(number))
                // print

                val volume = df.format(number).toDouble()

                val imageHeight = Source.contourBitmap.height
                val distanceFromTop = (y + roi.bottom) / 2

                val maxDistance = imageHeight.toDouble()
                val rfValue4 = 1.0 - (distanceFromTop.toDouble() / maxDistance)


                val rfValueTop = rfValue4 + roi.height() / 2 / imageHeight.toDouble()
                val rfValueBottom = rfValue4 - roi.height() / 2 / imageHeight.toDouble()

                val cv = 1 / rfValue4

                Source.contourDataArrayList.add(
                    ContourData(
                        idOf.toString(),
                        String.format(
                            "%.2f",
                            rfValue4
                        ),
                        String.format(
                            "%.2f",
                            rfValueTop
                        ),
                        String.format(
                            "%.2f",
                            rfValueBottom
                        ),
                        cv.toString(),
                        String.format(
                            "%.2f",
                            area
                        ),
                        volume.toString(),
                        "na"
                    )
                )


                Source.rFvsAreaArrayList.add(RFvsArea(roi.bottom.toDouble(), 0.0))
                Source.rFvsAreaArrayList.add(RFvsArea(rfValue4, area))
                Source.rFvsAreaArrayList.add(RFvsArea(roi.top.toDouble(), 0.0))

                Source.volumeDATA.add(volume)

                saveImageViewToFile(bit, imageFileName)

                val outFile: File = File(dir, imageFileName)
                if (outFile.exists()) {
                    Source.contourUri = Uri.fromFile(File(outFile.absolutePath))
                }

                Source.contourBitmap = bit

                val drawable = BitmapDrawable(resources, bit)
                drawnShapesStack.push(drawable)

//                Source.toast(this@DrawContourManually, drawnShapesStack.size.toString())

                k++

                imageView.setImageBitmap(bit)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }


    private fun undoLastDrawnShape() {
        if (!drawnShapesStack.isEmpty()) {
            drawnShapesStack.pop() // Remove the last drawn shape from the stack

            k--
            if (k > -1) {
                Source.contourDataArrayList.removeLast()
                Source.rFvsAreaArrayList.removeLast()
                Source.volumeDATA.removeLast()
                drawSpot.text = "Draw spot no. ${Source.contourDataArrayList.size + 1}"

            }

            if (drawnShapesStack.isEmpty()) {

                Source.toast(this@DrawContourManually, "No manual contours available")
                // No more shapes to undo, clear the image view
//                imageView.setImageDrawable(null)
//                imageView.setImageURI(uri)
                Source.contourBitmap = bit

                imageView.setImageBitmap(bit)
                saveImageViewToFile(bit, imageFileName)


            } else {
//                Source.toast(this@DrawContourManually, drawnShapesStack.size.toString())


                // Get the previous shape from the stack and set it as the image drawable
                val previousShape = drawnShapesStack.peek()
                imageView.setImageDrawable(previousShape)

                val icon: Bitmap = previousShape.toBitmap()
                Source.contourBitmap = icon

                saveImageViewToFile(icon, imageFileName)

            }
        }
    }


    fun saveImageViewToFile(originalBitmapImage: Bitmap, fileName: String): String? {

        // Get the Drawable from the ImageView
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
        } finally {
        }
        return fileName
    }


    override fun onResume() {
        super.onResume()
        drawSpot.text = "Draw spot no. ${Source.contourDataArrayList.size + 1}"

    }

    override fun onBackPressed() {
        super.onBackPressed()
//        Source.showContourImg = true
    }

}