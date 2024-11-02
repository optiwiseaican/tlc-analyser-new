package com.aican.tlcanalyzer

import android.content.ContextWrapper
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.dataClasses.ContourData
import com.aican.tlcanalyzer.dataClasses.ContourGraphSelModel
import com.aican.tlcanalyzer.dataClasses.RFvsArea
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.databinding.ActivityEditPixelBaselineBinding
import com.aican.tlcanalyzer.utils.Source
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.Float.max
import kotlin.math.roundToInt


class EditPixelBaseline : AppCompatActivity() {

    lateinit var binding: ActivityEditPixelBaselineBinding
    lateinit var rFvsAreaArrayList: ArrayList<RFvsArea>
    var mode: String = ""
    lateinit var information: ArrayList<Entry>
    lateinit var contourDataArrayListNew: java.util.ArrayList<ContourData>
    var lineDataSet = LineDataSet(null, null)
    var iLineDataSets = ArrayList<ILineDataSet>()
    lateinit var chart: LineChart
    var intensityGap = Source.PARTS_INTENSITY
    lateinit var lineData: LineData
    var spotId = ""

    var rfNewEdited = 0.0f
    var rfTopNewEdited = 0.0f
    var rfBottomNewEdited = 0.0f
    var firstTapIndex: Int? = null
    var secondTapIndex: Int? = null
    val highlightedRegions = ArrayList<Pair<Int, Int>>()
    private var rectangleList = ArrayList<Rect>()
    lateinit var arrayListCont: ArrayList<ContourData>
    lateinit var databaseHelper: DatabaseHelper
    lateinit var plotTableName: String
    var contourJsonFileName = "null"
    lateinit var dir: File
    lateinit var editedRect: Rect

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPixelBaselineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        chart = binding.chart

        databaseHelper = DatabaseHelper(this@EditPixelBaseline)

        binding.back.setOnClickListener {
            finish()
            overridePendingTransition(0, 0);
        }
        plotTableName = intent.getStringExtra("plotTableName").toString()
        spotId = intent.getStringExtra("spotId").toString()
        rfNewEdited = intent.getStringExtra("rf").toString().toFloat()
        rfBottomNewEdited = intent.getStringExtra("rfBottom").toString().toFloat()
        rfTopNewEdited = intent.getStringExtra("rfTop").toString().toFloat()
        contourJsonFileName = intent.getStringExtra("contourJsonFileName").toString()

        editedRect = convertRFToRect(
            rfTopNewEdited.toString(),
            rfBottomNewEdited.toString(),
            rfNewEdited.toString()
        )


        binding.projectName.text = "Edit Spot $spotId"
        binding.noteBoard.text =
            "Note: This process is for changing the Spot $spotId's baseline position"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getDataFromIntent()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showContoursList()
        }

        showData()



        chart.onChartGestureListener = object : OnChartGestureListener {
            override fun onChartGestureStart(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {
            }

            override fun onChartGestureEnd(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {
            }

            override fun onChartLongPressed(me: MotionEvent?) {
            }

            override fun onChartDoubleTapped(me: MotionEvent?) {
            }

            override fun onChartSingleTapped(me: MotionEvent?) {
                val xVal = chart.getHighlightByTouchPoint(me!!.x, me!!.y)?.x?.toInt()

                if (xVal != null) {
                    if (firstTapIndex == null) {
                        firstTapIndex = xVal
                    } else if (secondTapIndex == null) {
                        secondTapIndex = xVal

                        // Save the highlighted region
                        Log.e(
                            "HighPoints",
                            "firstTapIndex: $firstTapIndex, secondTapIndex: $secondTapIndex"
                        )
                        highlightedRegions.clear()
                        highlightedRegions.add(Pair(firstTapIndex!!, secondTapIndex!!))

                        // Highlight and display all stored regions
                        highlightAndDisplayAllRegions()

                        // Reset taps for the next selection
                        firstTapIndex = null
                        secondTapIndex = null
                    }
                }
            }

            override fun onChartFling(
                me1: MotionEvent?,
                me2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ) {
            }

            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
            }

            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
            }


        }

        binding.clearRegion.setOnClickListener {
            highlightedRegions.clear()
            highlightAndDisplayAllRegions()
        }

        dir = File(
            ContextWrapper(this).externalMediaDirs[0],
            resources.getString(R.string.app_name) + intent.getStringExtra("pId")
        )



        binding.saveThis.setOnClickListener {
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
                                roiData!!["bottom"] = editedRect.bottom.toDouble()
                                roiData!!["left"] = editedRect.left.toDouble()
                                roiData!!["right"] = editedRect.right.toDouble()
                                roiData!!["top"] = editedRect.top.toDouble()
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

                databaseHelper.updateDataTableDataById(
                    plotTableName, spotId,
                    rfNewEdited.toString(), rfTopNewEdited.toString(), rfBottomNewEdited.toString()
                )
                Toast.makeText(
                    this@EditPixelBaseline,
                    "Saving new baseline : rfTop = "
                            + rfTopNewEdited + ", rfBottom = " + rfBottomNewEdited + ", rf = " + rfNewEdited,
                    Toast.LENGTH_SHORT
                ).show()

                Source.contourBaselineEdited = true
                finish()

            } else {
                Toast.makeText(
                    this@EditPixelBaseline,
                    "Sorry, Only manual contours baseline can be change",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun highlightAndDisplayAllRegions() {
        val lineDataSets = ArrayList<ILineDataSet>()
        lineDataSets.add(lineDataSet)

        arrayListCont = ArrayList()
        rectangleList = ArrayList()


        val contourGraphSelModelArrayList = ArrayList<ContourGraphSelModel>()

//        //normal
        for (i in contourDataArrayListNew.indices) {
            contourGraphSelModelArrayList.add(
                ContourGraphSelModel(
                    contourDataArrayListNew[i].rfTop,
                    contourDataArrayListNew[i].rfBottom,
                    contourDataArrayListNew[i].rf,
                    contourDataArrayListNew[i].id,
                    contourDataArrayListNew[i].buttonColor
                )
            )
        }

        val shadedDataSets = ArrayList<LineDataSet>()
        for (i in contourGraphSelModelArrayList.indices) {
//        for (int i = contourDataArray.size() - 1; i >= 0; i--) {
            val mRFTop =
                String.format(
                    "%.0f",
                    contourGraphSelModelArrayList[i].rfTop.toFloat() * intensityGap
                ).toInt()
                    .toFloat()
            val mRFBottom =
                String.format(
                    "%.0f",
                    contourGraphSelModelArrayList[i].rfBottom.toFloat() * intensityGap
                ).toInt()
                    .toFloat()
            val mRF =
                String.format("%.0f", contourGraphSelModelArrayList[i].rf.toFloat() * intensityGap)
                    .toInt()
                    .toFloat()

            var newRfBottom = mRFBottom * Source.percentRFBottom
            var newRfTop = mRFTop * Source.percentRFTop

            val id: String = contourGraphSelModelArrayList.get(i).getId()
            if (id.contains("m")) {
                newRfTop = mRFTop
                newRfBottom = mRFBottom
            } else {
                newRfTop = mRFTop * Source.percentRFTop
                newRfBottom = mRFBottom * PixelGraph.adjustRfBottom(mRFTop - mRFBottom)
            }


            Log.e("CON_ID", id)
            Log.e("ThisIsNotAnErrorAAA", "Top : $newRfTop Bottom : $newRfBottom RF : $mRF")

            var int1 = information[0].y
            var int2 = 0.0f

            val shadedRegion = ArrayList<Entry>()
            for (entry in information) {
                val x = entry.x
                val y = entry.y
                //                if (x >= mRFTop && x <= mRFBottom) {
                if (x in newRfBottom..newRfTop) {
                    // Add the points within the range
                    Log.e("ThisIsNotAnErrorBBB", "Top : $x Bottom : $y RF : $mRF")

                    shadedRegion.add(Entry(x, y))

                    if (x == newRfBottom) {
                        int1 = y
                    }
                    if (x == newRfTop) {
                        int2 = y
                    }
                }
            }


            // Create a new LineDataSet for each shaded region
            val shadedDataSet = LineDataSet(shadedRegion, "Shaded Area " + (i + 1))
            shadedDataSet.setDrawCircles(false)

//            shadedDataSet.setColor(getColor(R.color.grey));
            shadedDataSet.color = contourGraphSelModelArrayList[i].buttonColor
            shadedDataSet.setDrawFilled(true)
            //            shadedDataSet.setFillColor(getColor(R.color.grey));
            shadedDataSet.fillColor = contourGraphSelModelArrayList[i].buttonColor
            shadedDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            shadedDataSet.lineWidth = 0f // Set the line width to zero for the shaded area

            val slopePoints2 = java.util.ArrayList<Entry>()
            slopePoints2.add(Entry(newRfBottom, int1))
            slopePoints2.add(Entry(newRfTop, int2))

            val baselineDataSet = LineDataSet(slopePoints2, "Baseline")
            baselineDataSet.setDrawCircles(false)
            baselineDataSet.color = Color.BLACK // Set the color for the baseline

            baselineDataSet.lineWidth = 1f // Set the line width for the baseline


//            shadedDataSets.add(baselineDataSet)

            // Add the shaded dataset to the list of shaded datasets
//            shadedDataSets.add(shadedDataSet)
        }

        // Add all shaded datasets to iLineDataSets
        lineDataSets.addAll(shadedDataSets)

        var bit = Source.contourBitmap


        // Add each stored region to shaded regions
        for ((im, region) in highlightedRegions.withIndex()) {
//            val shadedRegion = ArrayList<Entry>()


            val rfTop = (region.second / Source.PARTS_INTENSITY.toFloat()).toString()
            val rf =
                (((region.first + region.second) / 2) / Source.PARTS_INTENSITY.toFloat()).toString()

            val rfBottom =
                (region.first / Source.PARTS_INTENSITY.toFloat()).toString()

            val id = "g${im + 1}"

            rfNewEdited = rf.toFloat()
            rfTopNewEdited = rfTop.toFloat()
            rfBottomNewEdited = rfBottom.toFloat()

            editedRect = convertRFToRect(
                rfTopNewEdited.toString(),
                rfBottomNewEdited.toString(),
                rfNewEdited.toString()
            )


            val newRfTop = rfTop.toFloat() * Source.PARTS_INTENSITY
            val newRf = rf.toFloat() * Source.PARTS_INTENSITY
            val newRfBottom = rfBottom.toFloat() * Source.PARTS_INTENSITY
            if (rfBottom < rfTop) {

                arrayListCont.add(
                    ContourData(
                        id, rf,
                        rfTop, rfBottom,
                        "0",
                        "0",
                        "0",
                        "na"
                    )
                )

                val re = convertRFToRect(
                    rfTop.toFloat().toString(),
                    rfBottom.toFloat().toString(),
                    rf.toFloat().toString()
                )



                rectangleList.add(re)




                bit = RegionOfInterest.drawRectWithROI(
                    bit,
                    re.left,
                    re.top,
                    re.width(),
                    re.height()
                )


                val x: Int = re.left
                val y: Int = re.top
                val w: Int = re.width()
                val h: Int = re.height()

                val paint = Paint()
                paint.color = Color.RED
                paint.textSize = 30f
                paint.style = Paint.Style.FILL


                val canvas = Canvas(bit)
                canvas.drawText(id.toString(), x.toFloat(), y.toFloat(), paint)

//                binding.capturedImage.setImageBitmap(bit)

                Log.e(
                    "Love", "ID : " + id.toString() + "top " +
                            re.top + ", bottom " + re.bottom + ", left" + re.left + ", right " + re.right
                )
                Log.e("NotLove", "RfBottomReal : " + newRfBottom + "RfTopReal : " + newRfTop)

                ///////////////////////////////////////////////////////////////////////
                var int1 = information[0].y
                var int2 = 0.0f
                val shadedRegion = ArrayList<Entry>()
                for (entry in information) {
                    val x = entry.x
                    val y = entry.y
                    //                if (x >= mRFTop && x <= mRFBottom) {
                    if (x in (newRfBottom - 0)..(newRfTop + 0)) {
                        // Add the points within the range
                        Log.e("ThisIsNotAnErrorBBB", "Top : $x Bottom : $y RF : $rf")
                        if (isApproximatelyEqual(x, newRfBottom)) {
//                            Toast.makeText(this@PeakDetectionManually, "int1", Toast.LENGTH_SHORT)
//                                .show()
                            int1 = y
                        }
                        if (x == newRfTop) {
//                            Toast.makeText(this@PeakDetectionManually, "int2", Toast.LENGTH_SHORT)
//                                .show()
                            int2 = y
                        }
                        shadedRegion.add(Entry(x, y))

                    }
                }

                Log.e("NotAnErrorThis", "Int1 : $int1, Int2 : $int2")

                // Create a new LineDataSet for each shaded region
                val shadedDataSet = LineDataSet(shadedRegion, "Shaded Area ")
                shadedDataSet.setDrawCircles(false)

//            shadedDataSet.setColor(getColor(R.color.grey));
                shadedDataSet.color = Color.MAGENTA
                shadedDataSet.setDrawFilled(true)
                //            shadedDataSet.setFillColor(getColor(R.color.grey));
                shadedDataSet.fillColor = Color.MAGENTA
                shadedDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                shadedDataSet.lineWidth = 0f // Set the line width to zero for the shaded area

                val slopePoints2 = java.util.ArrayList<Entry>()
                slopePoints2.add(Entry(newRfBottom, int1))
                slopePoints2.add(Entry(newRfTop, int2))

                val baselineDataSet = LineDataSet(slopePoints2, "Baseline")
                baselineDataSet.setDrawCircles(false)
                baselineDataSet.color = Color.BLACK // Set the color for the baseline

                baselineDataSet.lineWidth = 1f // Set the line width for the baseline

                lineDataSets.add(baselineDataSet)

//                shadedDataSets.add(baselineDataSet)
                // Add the shaded dataset to the list of shaded datasets
                shadedDataSets.add(shadedDataSet)


                lineDataSets.add(shadedDataSet)
            } else {
                Toast.makeText(this@EditPixelBaseline, "Invalid Peak", Toast.LENGTH_SHORT)
                    .show()
            }
            //////


        }


        val lineData = LineData(lineDataSets)
        chart.data = lineData
        chart.invalidate()
    }

    fun isApproximatelyEqual(a: Float, b: Float, epsilon: Float = 1f): Boolean {
        return kotlin.math.abs(a - b) < epsilon
    }

    private fun convertRFToRect(rfTop: String, rfBottom: String, rf: String): Rect {

        val imageHeight = Source.contourBitmap.height
        val rfTopValue = rfTop.toDouble()
        val rfBottomValue = rfBottom.toDouble()
        val rfValue = rf.toDouble()

        val top = (1 - rfTopValue) * Source.contourBitmap.height
        val bottom = (1 - rfBottomValue) * Source.contourBitmap.height
        val width = Source.contourBitmap.width
        val height = imageHeight
        val left = 0
        val right = width

        val x = 0
        val y = 0
        val w = width
        val h = height

        return Rect(left, top.roundToInt(), right, bottom.roundToInt())
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun getDataFromIntent() {
        rFvsAreaArrayList = ArrayList<RFvsArea>()
        val intent = intent
        mode = intent.getStringExtra(resources.getString(R.string.modeKey)).toString()
        //        b = intent.getByteArrayExtra(getResources().getString(R.string.pixelsArrayKey));
//        intensities = Source.intensities
        //        rFvsAreaArrayList = Source.intensityVsRFArray;
        rFvsAreaArrayList = Source.rFvsAreaArrayList
//        contourSetArrayList = Source.contourSetArrayList
        for (i in rFvsAreaArrayList.indices) {
            println("Before sort")
            println(
                rFvsAreaArrayList.get(i).getRf().toString() + " , " + rFvsAreaArrayList.get(i)
                    .getArea()
            )
        }

        // Sort the list by the rf value in ascending order
        rFvsAreaArrayList.sortWith(Comparator.comparingDouble<RFvsArea> { obj: RFvsArea -> obj.rf })
        for (i in rFvsAreaArrayList.indices) {
            println("After sort")
            println(
                rFvsAreaArrayList.get(i).getRf().toString() + " , " + rFvsAreaArrayList.get(i)
                    .getArea()
            )
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showContoursList() {
        contourDataArrayListNew = java.util.ArrayList()
        for (i in Source.contourDataArrayList.indices) {
            var color = resources.getColor(R.color.grey)
            if (i == 0) {
                color = resources.getColor(R.color.grey)
            }
            if (i == 1) {
                color = resources.getColor(R.color.yellow)
            }
            if (i == 2) {
                color = resources.getColor(R.color.orange)
            }
            if (i == 3) {
                color = resources.getColor(R.color.blue)
            }
            if (i == 4) {
                color = getColor(R.color.yellow2)
            }
            if (i == 5) {
                color = getColor(R.color.teal)
            }
            if (i == 6) {
                color = getColor(R.color.purple)
            }
            if (i == 7) {
                color = getColor(R.color.green)
            }
            if (i == 8) {
                color = getColor(R.color.pink)
            }
            if (i == 9) {
                color = getColor(R.color.colorPrimary)
            }
            if (i == 10) {
                color = resources.getColor(R.color.blue2)
            }
            contourDataArrayListNew.add(
                ContourData(
                    Source.contourDataArrayList[i].id,
                    Source.contourDataArrayList[i].rf,
                    Source.contourDataArrayList[i].rfTop,
                    Source.contourDataArrayList[i].rfBottom,
                    Source.contourDataArrayList[i].cv,
                    Source.contourDataArrayList[i].area,
                    Source.contourDataArrayList[i].volume,
                    Source.contourDataArrayList[i].isSelected,
                    color
                )
            )
        }

//        contourDataArrayListNew = Source.contourDataArrayList;

    }

    private fun showData() {
        //normal
        val dataSets = ArrayList<ILineDataSet>()
        rFvsAreaArrayList.reverse()
        var mySTR = "Pixel Graph"
        information = ArrayList<Entry>()

        var tempA = ArrayList<Double>()

        for (i in rFvsAreaArrayList.indices) {
            tempA.add(Source.PARTS_INTENSITY - rFvsAreaArrayList[i].rf.toString().toDouble())
            information.add(
                Entry(
                    Source.PARTS_INTENSITY - rFvsAreaArrayList[i].rf.toString().toFloat(),
                    rFvsAreaArrayList[i].area.toString().toFloat()
                )
            )
            mySTR = "$mySTR[ rf: " + (Source.PARTS_INTENSITY - rFvsAreaArrayList[i].rf.toString()
                .toFloat()) + ", inten: " + rFvsAreaArrayList[i].area.toString().toFloat() + " ]"
        }
        println(mySTR)


        val contourGraphSelModelArrayList = ArrayList<ContourGraphSelModel>()

//        //normal
        for (i in contourDataArrayListNew.indices) {
            contourGraphSelModelArrayList.add(
                ContourGraphSelModel(
                    contourDataArrayListNew[i].rfTop,
                    contourDataArrayListNew[i].rfBottom,
                    contourDataArrayListNew[i].rf,
                    contourDataArrayListNew[i].id,
                    contourDataArrayListNew[i].buttonColor
                )
            )
        }
        showChart5Reverse(information, contourGraphSelModelArrayList)
    }

    private fun showChart5Reverse(
        dataVal: ArrayList<Entry>,
        contourDataArray: ArrayList<ContourGraphSelModel>
    ) {
        iLineDataSets.clear()
        lineDataSet.setValues(dataVal)
        lineDataSet.setDrawCircles(false)
        lineDataSet.setColor(getColor(R.color.purple_200))

        // Disable filling for the entire curve
        lineDataSet.setDrawFilled(false)
        iLineDataSets.add(lineDataSet)

        // Create an ArrayList to hold all shaded regions
        val shadedDataSets = ArrayList<LineDataSet>()
        for (i in contourDataArray.indices) {
//        for (int i = contourDataArray.size() - 1; i >= 0; i--) {
            val mRFTop =
                String.format("%.0f", contourDataArray[i].rfTop.toFloat() * intensityGap).toInt()
                    .toFloat()
            val mRFBottom =
                String.format("%.0f", contourDataArray[i].rfBottom.toFloat() * intensityGap).toInt()
                    .toFloat()
            val mRF = String.format("%.0f", contourDataArray[i].rf.toFloat() * intensityGap).toInt()
                .toFloat()


            var newRfBottom = mRFBottom * Source.percentRFBottom
            var newRfTop = mRFTop * Source.percentRFTop

            val id = contourDataArray[i].id
            if (id.contains("m")) {
                newRfTop = mRFTop
                newRfBottom = mRFBottom
            } else {
                newRfTop = mRFTop * Source.percentRFTop
                newRfBottom = mRFBottom * PixelGraph.adjustRfBottom(mRFTop - mRFBottom)
            }


            Log.e("CON_ID", id)

            Log.e("ThisIsNotAnErrorAAA", "Top : $newRfTop Bottom : $newRfBottom RF : $mRF")
            val shadedRegion = ArrayList<Entry>()


            var int1 = dataVal[0].y
            var int2 = 0.0f


            for (entry in dataVal) {
                val x = entry.x
                val y = entry.y
                //                if (x >= mRFTop && x <= mRFBottom) {
                if (x in newRfBottom..newRfTop) {
                    // Add the points within the range
                    Log.e("ThisIsNotAnErrorBBB", "Top : $x Bottom : $y RF : $mRF")

                    shadedRegion.add(Entry(x, y))

                    if (x == newRfBottom) {
                        int1 = y
                    }
                    if (x == newRfTop) {
                        int2 = y
                    }
                }
            }


            // Create a new LineDataSet for each shaded region
            val shadedDataSet = LineDataSet(shadedRegion, "Shaded Area " + (i + 1))
            shadedDataSet.setDrawCircles(false)

//            shadedDataSet.setColor(getColor(R.color.grey));
            shadedDataSet.color = contourDataArray[i].buttonColor
            shadedDataSet.setDrawFilled(true)
            //            shadedDataSet.setFillColor(getColor(R.color.grey));
            shadedDataSet.fillColor = contourDataArray[i].buttonColor
            shadedDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            shadedDataSet.lineWidth = 0f // Set the line width to zero for the shaded area

            val slopePoints2 = java.util.ArrayList<Entry>()
            slopePoints2.add(Entry(newRfBottom, int1))
            slopePoints2.add(Entry(newRfTop, int2))

            val baselineDataSet = LineDataSet(slopePoints2, "Baseline")
            baselineDataSet.setDrawCircles(false)
            baselineDataSet.color = Color.BLACK // Set the color for the baseline

            baselineDataSet.lineWidth = 1f // Set the line width for the baseline

            if (spotId == id) {

                if (id.contains("m")) {

                    shadedDataSets.add(baselineDataSet)
                }
                // Add the shaded dataset to the list of shaded datasets
                shadedDataSets.add(shadedDataSet)
            }
        }


        val rfTop =
            String.format(
                "%.0f",
                intent.getStringExtra("rfTop").toString().toFloat() * intensityGap
            ).toInt()
                .toFloat()
        val rfBottom =
            String.format(
                "%.0f",
                intent.getStringExtra("rfBottom").toString().toFloat() * intensityGap
            ).toInt()
                .toFloat()

// Set the buffer to show some parts outside the region
        val buffer = 5f // You can adjust this value based on your preference

// Calculate the start and end values for the visible range
        val startX = max(0f, rfBottom)
        val endX = rfTop

        Log.e("ZoomPart", "Start: $startX, EndX: $endX")

        // Add all shaded datasets to iLineDataSets
        iLineDataSets.addAll(shadedDataSets)
        lineData = LineData(iLineDataSets)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.isEnabled = false
        chart.clear()
        chart.data = lineData

//        chart.setVisibleXRange(startX, endX)

        chart.invalidate()


    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0);
//        overridePendingTransition(R.anim.up_to_down, R.anim.down_to_up);

    }


}