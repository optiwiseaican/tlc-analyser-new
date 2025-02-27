package com.aican.tlcanalyzer

import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.dataClasses.ContourData
import com.aican.tlcanalyzer.dataClasses.ContourSet
import com.aican.tlcanalyzer.dataClasses.RFvsArea
import com.aican.tlcanalyzer.dataClasses.XY
import com.aican.tlcanalyzer.utils.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.Collections
import kotlin.math.sqrt

class AllSpitAnalysis : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_spit_analysis)
    }


    private val coroutineScope = CoroutineScope(Dispatchers.Main)

//    private fun performImageAnalysis(imageFiles: List<File>): Bitmap {
//        coroutineScope.launch(Dispatchers.Default) {
//            for (imageFile in imageFiles) {
//                val processedImageData = spotContour(imageFile)
//                withContext(Dispatchers.Main) {
//                    myAdapter.addImageData(processedImageData)
//                }
//            }
//        }
//    }

    fun spotContour(
        noOfSpots: Int,
        threshVal: Float,
        dir: File,
        projectImage: String,
        id: String,
        contourImageFileName: String
    ): Bitmap {
        val contourList = ArrayList<Int>()
        val rFvsAreaArrayList = ArrayList<RFvsArea>()
        val contourSetArrayList = ArrayList<ContourSet>()

        var bitImage: Bitmap? = null
        val outFile: File = File(dir, projectImage)
        if (outFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)

//                captured_image.setImageBitmap(myBitmap);
            bitImage = myBitmap
        } else {
            Source.toast(this, "Image not available or deleted")
        }

        val firstImage = Mat()
        Utils.bitmapToMat(bitImage, firstImage)

        // grayscale
        val grayScaleImage = Mat()
        Imgproc.cvtColor(firstImage, grayScaleImage, Imgproc.COLOR_BGR2GRAY)
        val alpha = 1.5 // Contrast control
        val beta = 10.0 // Brightness control

//         adjustedImage = new Mat();
//        grayScaleImage.convertTo(adjustedImage, CvType.CV_8U, alpha, beta);
        val adjustedImage = grayScaleImage

        // Apply threshold to convert the grayscale image to a binary image
        val binary = Mat()
        Imgproc.threshold(adjustedImage, binary, threshVal.toDouble(), 255.0, 0)

        // Find contours in the binary image
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            binary,
            contours,
            hierarchy,
            Imgproc.RETR_TREE,
            Imgproc.CHAIN_APPROX_NONE
        )
        Log.d("ContourSize", contours.size.toString() + "")
        // Sort contours by volume in descending order
        Collections.sort(contours, object : Comparator<MatOfPoint?> {
            override fun compare(c1: MatOfPoint?, c2: MatOfPoint?): Int {
                val area1 = Imgproc.contourArea(c1)
                val area2 = Imgproc.contourArea(c2)
                return java.lang.Double.compare(area2, area1)
            }
        })
        Log.d("ContourSize", contours.size.toString() + "")

        // Draw the contours on the original image and label with contour number

//        for (int i = 0; i < contours.size(); i++) {
        return plotContour(
            noOfSpots,
            threshVal,
            contours,
            adjustedImage,
            contourList,
            bitImage!!,
            id,
            dir,
            contourImageFileName
        )
    }

    fun plotContour(
        numberCount: Int,
        threshVal: Float,
        contours: ArrayList<MatOfPoint>,
        adjustedImage: Mat,
        contourList: ArrayList<Int>,
        bitImage: Bitmap, id: String,
        dir: File, contourImageFileName: String
    ): Bitmap {

        var refinedContour: List<MatOfPoint> = ArrayList()
        val contourWithDataList = ArrayList<MatOfPoint>()
        val contourColor = Scalar(255.0, 244.0, 143.0)
        val contourThickness = 2
        val font = Imgproc.FONT_HERSHEY_SIMPLEX
        val fontScale = 1.0
        val fontThickness = 2
        if (numberCount != 0) {
            val range: Int = numberCount + 1
            println("1. This : " + range + " &" + contours.size)
            if (range > contours.size) {
                Toast.makeText(
                    this,
                    "Only " + contours.size
                            + " number of contours available, You can check with different threshold value",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                for (i in 1 until range) {
                    contourWithDataList.add(contours.get(i))
                    Imgproc.drawContours(adjustedImage, contours, i, contourColor, contourThickness)
                    // Get the bounding rectangle of the contour
                    val boundingRect = Imgproc.boundingRect(contours.get(i))
                    // Draw the contour number on the image
                    contourList.add(i)
                    Imgproc.putText(
                        adjustedImage, "" + i, Point(
                            boundingRect.x.toDouble(),
                            (boundingRect.y - 5).toDouble()
                        ),
                        font, fontScale, Scalar(0.0, 0.0, 255.0), fontThickness
                    )
                    //                }
//                for (int i = 1; i < range; i++) {
                    // Get the bounding box of the contour
//                    Rect boundingRect = Imgproc.boundingRect(contours.get(i));

                    // Calculate the center point of the bounding box
                    val centerPoint = Point(
                        (boundingRect.x + (boundingRect.width / 2)).toDouble(),
                        (boundingRect.y + (boundingRect.height / 2)).toDouble()
                    )

                    // Calculate the distance traveled by the center point
                    val contourDistance =
                        Math.sqrt(Math.pow(centerPoint.x, 2.0) + Math.pow(centerPoint.y, 2.0))

                    // Calculate the distance traveled by the solvent front (assuming a linear gradient)
                    val solventFrontDistance: Double = (adjustedImage.width()
                        .toDouble() / 2) * (1.0 - threshVal.toDouble() / 255.0)

                    // Calculate the RF value
                    val rfValue = (10 - (contourDistance / solventFrontDistance)) / 10
                    val height = boundingRect.height
                    // we assume there is only one contour in the image
                    val contour: MatOfPoint = contours.get(0)
                    //


                    // calculate the height of the contour
                    val contourHeight = Imgproc.boundingRect(contour).height.toDouble()
                    val area = Imgproc.contourArea(contours.get(i))

                    // assuming you have already found the contour and its height
                    val topY = boundingRect.y
                    val baseY = topY + boundingRect.height
                    val centerY = topY + boundingRect.height / 2

                    // assuming you have a Bitmap object called "imageBitmap"
                    val imageHeight: Int = bitImage.getHeight()


// assuming you have the image height stored in a variable called "imageHeight"
                    val normalizedTopY = 1 - (topY.toDouble() / imageHeight)
                    val normalizedBaseY = 1 - (baseY.toDouble() / imageHeight)
                    val normalizedCenterY =
                        String.format("%.2f", 1 - (centerY.toDouble() / imageHeight)).toDouble()
                    val cv = 1 / normalizedCenterY
                    val ar = ArrayList<RFvsArea>()
                    ar.add(RFvsArea(normalizedBaseY, 0.0))
                    ar.add(RFvsArea(normalizedCenterY, area))
                    ar.add(RFvsArea(normalizedTopY, 0.0))
                    println(
                        ("Height : " + height + ", Diameter : " + " - >>>>>>" + normalizedTopY + " , "
                                + normalizedBaseY + " , " + normalizedCenterY)
                    )
//                    rFvsAreaArrayList.addAll(ar)
                    val xyArrayList = ArrayList<XY>()
                    xyArrayList.add(XY(normalizedBaseY, 0.0))
                    xyArrayList.add(XY(normalizedCenterY, area))
                    xyArrayList.add(XY(normalizedTopY, 0.0))
                    val contourSet = ContourSet(xyArrayList)
//                    contourSetArrayList.add(contourSet)

//                     Calculate the area of the contour


                    // Calculate the volume of the contour (assuming a cylindrical shape)
//            double volume = area * (solventFrontDistance - contourDistance);
                    val df = DecimalFormat("0.00E0")
                    val number = area * Math.abs(solventFrontDistance - contourDistance)
                    println(df.format(number))
                    val volume = df.format(number).toDouble()
//                    volumeArrayList.add(volume)
                    val contourData = ContourData(
                        i.toString(),
                        normalizedCenterY.toString(),
                        normalizedTopY.toString(),
                        normalizedBaseY.toString(),
                        cv.toString(),
                        area.toString(),
                        volume.toString(),
                        "na"
                    )
//                    contourDataArrayList.add(contourData)

                    // Print the RF value, area, and volume of the contour
                    Log.d("Contour $i RF value", rfValue.toString())
                    Log.d("Contour $i area", area.toString())
                    Log.d("Contour $i volume", volume.toString())
                }
            }
            refinedContour = contourWithDataList
        } else {
            for (i in 1 until contours.size) {
                Imgproc.drawContours(adjustedImage, contours, i, contourColor, contourThickness)
                // Get the bounding rectangle of the contour
                val boundingRect = Imgproc.boundingRect(contours.get(i))
                // Draw the contour number on the image
                Imgproc.putText(
                    adjustedImage, "" + i, Point(
                        boundingRect.x.toDouble(),
                        (boundingRect.y - 5).toDouble()
                    ),
                    font, fontScale, contourColor, fontThickness
                )
                contourList.add(i)

//            }
//            for (int i = 1; i < contours.size(); i++) {
//                // Get the bounding box of the contour
//                Rect boundingRect = Imgproc.boundingRect(contours.get(i));

                // Calculate the center point of the bounding box
                val centerPoint = Point(
                    (boundingRect.x + (boundingRect.width / 2)).toDouble(),
                    (boundingRect.y + (boundingRect.height / 2)).toDouble()
                )

                // Calculate the distance traveled by the center point
                val contourDistance =
                    sqrt(Math.pow(centerPoint.x, 2.0) + Math.pow(centerPoint.y, 2.0))

                // Calculate the distance traveled by the solvent front (assuming a linear gradient)
                val solventFrontDistance: Double =
                    (adjustedImage.width().toDouble() / 2) * (1.0 - threshVal.toDouble() / 255.0)

                // Calculate the RF value
                val rfValue = contourDistance / solventFrontDistance

                // Calculate the area of the contour
                val area = Imgproc.contourArea(contours.get(i))

                // Calculate the volume of the contour (assuming a cylindrical shape)
//            double volume = area * (solventFrontDistance - contourDistance);
                val df = DecimalFormat("0.00E0")
                val number = area * Math.abs(solventFrontDistance - contourDistance)
                println(df.format(number))
                val volume = df.format(number).toDouble()
                Source.multiIntensities.add(volume)
                // calculate the height of the contour
                val contour: MatOfPoint = contours.get(0)
                //
                val contourHeight = Imgproc.boundingRect(contour).height.toDouble()

                // assuming you have already found the contour and its height
                val topY = boundingRect.y
                val baseY = topY + boundingRect.height
                val centerY = topY + boundingRect.height / 2

                // assuming you have a Bitmap object called "imageBitmap"
                val imageHeight: Int = bitImage.height


// assuming you have the image height stored in a variable called "imageHeight"
                val normalizedTopY = 1 - (topY.toDouble() / imageHeight)
                val normalizedBaseY = 1 - (baseY.toDouble() / imageHeight)
                val normalizedCenterY =
                    String.format("%.2f", 1 - (centerY.toDouble() / imageHeight)).toDouble()
                val cv = 1 / normalizedCenterY
                val rfArray = doubleArrayOf(normalizedBaseY, normalizedCenterY, normalizedTopY)
                for (j in rfArray.indices) {
                    var rFvsArea: RFvsArea? = null
                    if (j == 0) {
                        rFvsArea = RFvsArea(normalizedBaseY, 0.0)
                    }
                    if (j == 1) {
                        rFvsArea = RFvsArea(normalizedCenterY, area)
                    }
                    if (j == 2) {
                        rFvsArea = RFvsArea(normalizedTopY, 0.0)
                    }
//                    rFvsAreaArrayList.add(rFvsArea)
                }
//                volumeArrayList.add(volume)
                val contourData = ContourData(
                    i.toString(),
                    normalizedCenterY.toString(),
                    normalizedTopY.toString(),
                    normalizedBaseY.toString(),
                    cv.toString(),
                    area.toString(),
                    volume.toString(),
                    "na"
                )
//                contourDataArrayList.add(contourData)

                // Print the RF value, area, and volume of the contour
                Log.d("Contour $i RF value", rfValue.toString())
                Log.d("Contour $i area", area.toString())
                Log.d("Contour $i volume", volume.toString())
            }
            refinedContour = contours
        }
        val rf = ""
        // Calculate the RF value of all contours

        // Calculate the RF value, area, and volume of all contours
//        Source.volumeDATA = volumeArrayList
//        Source.rFvsAreaArrayList = rFvsAreaArrayList
//        Source.contourSetArrayList = contourSetArrayList
//        Source.contourDataArrayList = contourDataArrayList

//        contourDataAdapter = new ContourDataAdapter(ImageEvaluationProcess.this, contourDataArrayList);
//        recView.setAdapter(contourDataAdapter);
//        contourDataAdapter.notifyDataSetChanged();

//        rfValues.setText(rf);

        // Draw the contours on the original image
//        Imgproc.drawContours(grayScaleImage, contours, -1, new Scalar(0, 0, 255), 2);

        // Convert the Mat to a Bitmap
        val bitmap =
            Bitmap.createBitmap(adjustedImage.cols(), adjustedImage.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(adjustedImage, bitmap)
        Toast.makeText(this, "Contours Spotted", Toast.LENGTH_SHORT).show()
        Source.contourBitmap = bitmap
        saveImageViewToFile(bitmap, contourImageFileName, id)
        val outFile: File = File(dir, contourImageFileName)
        if (outFile.exists()) {
            Source.contourUri = Uri.fromFile(File(outFile.absolutePath))
        }

        // Set the Bitmap in the ImageView
//        captured_image.setImageBitmap(bitmap)
        return bitmap
//        removeContour();
    }


    private fun saveImageViewToFile(
        originalBitmapImage: Bitmap,
        fileName: String?,
        id: String
    ): String? {
        var outStream: FileOutputStream? = null

        // Write to SD Card
        try {
            val sdCard = Environment.getExternalStorageDirectory()
            val dir = File(
                ContextWrapper(this).externalMediaDirs[0],
                resources.getString(R.string.app_name) + id
            )

            dir.mkdirs()
            val outFile = File(dir, fileName!!)
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