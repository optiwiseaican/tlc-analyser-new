package com.aican.tlcanalyzer

import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.adapterClasses.ContourIntGraphAdapter
import com.aican.tlcanalyzer.customClasses.LegacyTableView
import com.aican.tlcanalyzer.dataClasses.ContourData
import com.aican.tlcanalyzer.dataClasses.ContourGraphSelModel
import com.aican.tlcanalyzer.dataClasses.ContourSet
import com.aican.tlcanalyzer.dataClasses.RFvsArea
import com.aican.tlcanalyzer.databinding.ActivityPeakDetectionManuallyBinding
import com.aican.tlcanalyzer.interfaces.OnClicksListeners
import com.aican.tlcanalyzer.utils.Source
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import kotlin.math.roundToInt


class PeakDetectionManually : AppCompatActivity(), OnClicksListeners {

    lateinit var binding: ActivityPeakDetectionManuallyBinding
    lateinit var legacyTableView: LegacyTableView
    lateinit var rFvsAreaArrayList: ArrayList<RFvsArea>
    lateinit var contourDataArrayListNew: java.util.ArrayList<ContourData>
    lateinit var contourSetArrayList: ArrayList<ContourSet>
    private lateinit var contourIntGraphAdapter: ContourIntGraphAdapter
    lateinit var chart: LineChart
    var intensityGap = Source.PARTS_INTENSITY
    lateinit var intensities: ArrayList<Double>
    lateinit var information: ArrayList<Entry>
    var lineDataSet = LineDataSet(null, null)
    var iLineDataSets = ArrayList<ILineDataSet>()
    var mode: String? = null
    lateinit var lineData: LineData

    var firstTapIndex: Int? = null
    var secondTapIndex: Int? = null
    val highlightedRegions = ArrayList<Pair<Int, Int>>()


    private var rectangleList = ArrayList<Rect>()
    lateinit var arrayListCont: ArrayList<ContourData>
    private lateinit var tableLayout: TableLayout

    private lateinit var tableRowHeader: TableRow
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPeakDetectionManuallyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        legacyTableView = binding.legacyTableView
        chart = binding.chart

        binding.back.setOnClickListener {
            finish()
        }
        tableLayout = binding.table
        arrayListCont = ArrayList()
        rectangleList = ArrayList()

        binding.showImagebtn.text = getString(R.string.show_image)
        binding.myImageCard.visibility = View.GONE

        binding.capturedImage.setImageBitmap(Source.contourBitmap)
        val kj = intArrayOf(0)
        binding.showImagebtn.setOnClickListener {
            if (kj[0] == 0) {
                binding.showImagebtn.text = getString(R.string.hide_image)
                binding.myImageCard.visibility = View.VISIBLE
                kj[0]++
            } else {
                binding.showImagebtn.text = getString(R.string.show_image)
                binding.myImageCard.visibility = View.GONE
                kj[0]--
            }
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getDataFromIntent()
        }
//        setupChart();

        //        setupChart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showContoursList()
        }

        showData()

//        plotTableRecView();

//        plotTableRecView();
        plotTable()

//        mains()

        binding.undoButton.setOnClickListener {
            handleUndo()
        }
        binding.zoomIn.setOnClickListener(View.OnClickListener { chart.zoomIn() })

        binding.zoomOut.setOnClickListener(View.OnClickListener { chart.zoomOut() })



        chart.setOnChartGestureListener(object : OnChartGestureListener {
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
                            "firstTapIndex: " + firstTapIndex + ", secondTapIndex: " + secondTapIndex
                        )
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


        })




        binding.saveThis.setOnClickListener {

            if (rectangleList != null && rectangleList.size > 0) {
                Source.rectangleList = ArrayList()
                Source.rectangleList.addAll(rectangleList)
                Source.rectangle = true
                Source.rectangleOneActivityToPixelActivity = true
                Source.shape = 0

                finish()
            } else {
                Toast.makeText(
                    this@PeakDetectionManually,
                    "No spots", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleUndo() {
        if (highlightedRegions.isNotEmpty()) {
            // Remove the last recorded region
            highlightedRegions.removeAt(highlightedRegions.size - 1)

            if (highlightedRegions.size == 0) {
                binding.capturedImage.setImageBitmap(Source.contourBitmap)
            }


            // Highlight and display all remaining regions
            highlightAndDisplayAllRegions()


        }
    }

    //on create end
    private fun createTableRow(
        ID: String,
        Rf: String,
        Cv: String,
        Area: String,
        pArea: String,
        volume: String,
        rfTop: String,
        rfBottom: String,
        index: Int
    ) {
        val tableRow = TableRow(this)
        val lp = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tableRow.layoutParams = lp
        val textViewID = TextView(this)
        val textViewRf = TextView(this)
        val textViewCv = TextView(this)
        val textViewArea = TextView(this)
        val textViewPArea = TextView(this)
        val textViewVolume = TextView(this)
        val textViewRfTop = TextView(this)
        val textViewRfBottom = TextView(this)


        textViewID.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT,
            0f
        )
        textViewRf.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT,
            0.3f
        )
        textViewCv.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT,
            1.5f
        )
        textViewArea.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT,
            0f
        )
        textViewPArea.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT,
            0.3f
        )
        textViewVolume.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT,
            0.3f
        )
        textViewRfTop.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT,
            0.3f
        )

        textViewRfBottom.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT,
            0.3f
        )


        textViewID.gravity = Gravity.CENTER
        textViewRf.gravity = Gravity.CENTER
        textViewCv.gravity = Gravity.CENTER
        textViewArea.gravity = Gravity.CENTER
        textViewPArea.gravity = Gravity.CENTER
        textViewVolume.gravity = Gravity.CENTER
        textViewRfTop.gravity = Gravity.CENTER
        textViewRfBottom.gravity = Gravity.CENTER

        textViewCv.maxLines = 3
        textViewRf.maxLines = 2
        textViewPArea.maxLines = 2
        textViewArea.maxLines = 2
        textViewVolume.maxLines = 2
        textViewRfTop.maxLines = 2
        textViewRfBottom.maxLines = 2

        textViewID.setPadding(5, 15, 5, 15)
        textViewRf.setPadding(5, 15, 5, 15)
        textViewCv.setPadding(5, 15, 5, 15)
        textViewArea.setPadding(5, 15, 5, 15)
        textViewPArea.setPadding(5, 15, 5, 15)
        textViewVolume.setPadding(5, 15, 5, 15)
        textViewRfTop.setPadding(5, 15, 5, 15)
        textViewRfBottom.setPadding(5, 15, 5, 15)

        textViewID.text = ID
        textViewRf.text = Rf
        textViewCv.text = Cv
        textViewArea.text = Area
        textViewPArea.text = pArea
        textViewVolume.text = volume
        textViewRfTop.text = rfTop
        textViewRfBottom.text = rfBottom

        textViewID.setTextColor(getColor(R.color.black))
        textViewRf.setTextColor(getColor(R.color.black))
        textViewCv.setTextColor(getColor(R.color.black))
        textViewArea.setTextColor(getColor(R.color.black))
        textViewPArea.setTextColor(getColor(R.color.black))
        textViewVolume.setTextColor(getColor(R.color.black))
        textViewRfTop.setTextColor(getColor(R.color.black))
        textViewRfBottom.setTextColor(getColor(R.color.black))

        textViewID.setBackgroundResource(R.drawable.cell_shape_white)
        textViewRf.setBackgroundResource(R.drawable.cell_shape_grey)
        textViewCv.setBackgroundResource(R.drawable.cell_shape_white)
        textViewArea.setBackgroundResource(R.drawable.cell_shape_grey)
        textViewPArea.setBackgroundResource(R.drawable.cell_shape_white)
        textViewVolume.setBackgroundResource(R.drawable.cell_shape_grey)
        textViewRfTop.setBackgroundResource(R.drawable.cell_shape_white)
        textViewRfBottom.setBackgroundResource(R.drawable.cell_shape_grey)

        if (index == -1) {
            tableRowHeader = tableRow
            textViewID.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_medium).toInt()
                    .toFloat()
            )
            textViewRf.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_medium).toInt()
                    .toFloat()
            )
            textViewCv.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_medium).toInt()
                    .toFloat()
            )
            textViewArea.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_medium).toInt()
                    .toFloat()
            )
            textViewPArea.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_medium).toInt()
                    .toFloat()
            )
            textViewVolume.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_medium).toInt()
                    .toFloat()
            )
            textViewRfTop.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_medium).toInt()
                    .toFloat()
            )
            textViewRfBottom.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_medium).toInt()
                    .toFloat()
            )

            textViewID.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewRf.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewCv.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewArea.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewPArea.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewVolume.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewRfTop.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewRfBottom.setBackgroundResource(R.drawable.cell_shape_blue)

        } else {
            textViewID.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_small).toInt()
                    .toFloat()
            )
            textViewRf.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_small).toInt()
                    .toFloat()
            )
            textViewCv.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_small).toInt()
                    .toFloat()
            )
            textViewArea.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_small).toInt()
                    .toFloat()
            )
            textViewPArea.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_small).toInt()
                    .toFloat()
            )
            textViewVolume.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_small).toInt()
                    .toFloat()
            )

            textViewRfTop.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_small).toInt()
                    .toFloat()
            )
            textViewRfBottom.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_small).toInt()
                    .toFloat()
            )
        }
        tableRow.addView(textViewID)
        tableRow.addView(textViewRf)
        tableRow.addView(textViewCv)
        tableRow.addView(textViewArea)
        tableRow.addView(textViewPArea)
        if (Source.SHOW_VOLUME_DATA)
            tableRow.addView(textViewVolume)
        tableRow.addView(textViewRfTop)
        tableRow.addView(textViewRfBottom)


        tableLayout.addView(tableRow, index + 1)
    }

    private fun createTableHeader() {
        tableLayout.removeAllViews()
        createTableRow("ID", "Rf", "Cv", "Area", "% area", "Volume", "rfTop", "rfBottom", -1)
    }

    private fun plotTable() {
        createTableHeader()
        LegacyTableView.insertLegacyTitle(
            "ID",
            "Rf",
            "Cv",
            "Area",
            "% area",
            "Volume",
            "rfTop",
            "rfBottom"
        )
        val contourDataArrayList = arrayListCont

        contourDataArrayList.addAll(Source.contourDataArrayList)

        var totalArea = 0f
        for (i in contourDataArrayList.indices) {
            totalArea += contourDataArrayList[i].area.toFloat()
        }
        for (i in contourDataArrayList.indices) {

            LegacyTableView.insertLegacyContent(
                contourDataArrayList[i].id, contourDataArrayList[i].rf,
                String.format(
                    "%.2f", (1.0 / contourDataArrayList[i].rf.toFloat()).toString().toFloat()
                ),

                if (contourDataArrayList[i].id.contains("g")) {
                    "later"
                } else {
                    contourDataArrayList[i].area

                },
                if (contourDataArrayList[i].id.contains("g")) {
                    "later"
                } else {
                    String.format(
                        "%.2f", contourDataArrayList[i].area.toFloat() / totalArea * intensityGap
                    ) + " %"
                },

                if (contourDataArrayList[i].id.contains("g")) {
                    "later"
                } else {
                    contourDataArrayList[i].volume

                },

                String.format(
                    "%.2f", (contourDataArrayList[i].rfTop.toFloat()).toString().toFloat()
                ),
                String.format(
                    "%.2f", (contourDataArrayList[i].rfBottom.toFloat()).toString().toFloat()
                )
            )


        }

        // data rows
        for (i in contourDataArrayList.indices) {
            createTableRow(
                contourDataArrayList[i].id, contourDataArrayList[i].rf,
                String.format(
                    "%.2f", (1.0 / contourDataArrayList[i].rf.toFloat()).toString().toFloat()
                ),

                if (contourDataArrayList[i].id.contains("g")) {
                    "later"
                } else {
                    contourDataArrayList[i].area

                },
                if (contourDataArrayList[i].id.contains("g")) {
                    "later"
                } else {
                    String.format(
                        "%.2f",
                        contourDataArrayList[i].area.toFloat() / totalArea * intensityGap
                    ) + " %"
                },

                if (contourDataArrayList[i].id.contains("g")) {
                    "later"
                } else {
                    contourDataArrayList[i].volume

                },

                String.format(
                    "%.2f", (contourDataArrayList[i].rfTop.toFloat()).toString().toFloat()
                ),
                String.format(
                    "%.2f", (contourDataArrayList[i].rfBottom.toFloat()).toString().toFloat()
                ),
                i
            )
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Adjust padding and text size for landscape mode
            legacyTableView.setTablePadding(10) // Set a smaller padding value
            legacyTableView.setContentTextSize(12) // Set a smaller content text size
        }
        legacyTableView.setTheme(LegacyTableView.CUSTOM)
        legacyTableView.setContent(LegacyTableView.readLegacyContent())
        legacyTableView.setTitle(LegacyTableView.readLegacyTitle())
//        legacyTableView.setBottomShadowVisible(true);
        //        legacyTableView.setBottomShadowVisible(true);
        legacyTableView.setHighlight(LegacyTableView.ODD)
        legacyTableView.setBottomShadowVisible(false)
        legacyTableView.setFooterTextAlignment(LegacyTableView.CENTER)
        legacyTableView.setTableFooterTextSize(5)
        legacyTableView.setTableFooterTextColor("#f0f0ff")
        legacyTableView.setTitleTextAlignment(LegacyTableView.CENTER)
        legacyTableView.setContentTextAlignment(LegacyTableView.CENTER)
        legacyTableView.setTablePadding(20)
        legacyTableView.setBackgroundOddColor("#F0F0FF")
        legacyTableView.setHeaderBackgroundLinearGradientBOTTOM("#F0F0FF")
        legacyTableView.setHeaderBackgroundLinearGradientTOP("#F0F0FF")
        legacyTableView.setBorderSolidColor("#f0f0ff")
        legacyTableView.setTitleTextColor("#212121")
        legacyTableView.setTitleFont(LegacyTableView.BOLD)
        legacyTableView.setZoomEnabled(false)
        legacyTableView.setShowZoomControls(false)

        legacyTableView.setContentTextColor("#000000")
        legacyTableView.build()
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
            if (id.contains(Source.manual_contour_prefix)) {
                newRfTop = mRFTop
                newRfBottom = mRFBottom
            } else {
                newRfTop = mRFTop * Source.percentRFTop
                newRfBottom = mRFBottom * PixelGraph.adjustRfBottom(mRFTop - mRFBottom)
            }


            Log.e("CON_ID", id)
            Log.e("ThisIsNotAnErrorAAA", "Top : $newRfTop Bottom : $newRfBottom RF : $mRF")

            var int1 = 0.0f
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


            shadedDataSets.add(baselineDataSet)

            // Add the shaded dataset to the list of shaded datasets
            shadedDataSets.add(shadedDataSet)
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

                binding.capturedImage.setImageBitmap(bit)

                Log.e(
                    "Love", "ID : " + id.toString() + "top " +
                            re.top + ", bottom " + re.bottom + ", left" + re.left + ", right " + re.right
                )
                Log.e("NotLove", "RfBottomReal : " + newRfBottom + "RfTopReal : " + newRfTop)

                ///////////////////////////////////////////////////////////////////////
                var int1 = 0.0f
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
                Toast.makeText(this@PeakDetectionManually, "Invalid Peak", Toast.LENGTH_SHORT)
                    .show()
            }
            //////


        }

        plotTable()

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
        contourIntGraphAdapter =
            ContourIntGraphAdapter(
                true,
                this, contourDataArrayListNew, 0,
                this, true, false, false
            )
        binding.contourListRecView.adapter = contourIntGraphAdapter
        contourIntGraphAdapter.notifyDataSetChanged()
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
            if (id.contains(Source.manual_contour_prefix)) {
                newRfTop = mRFTop
                newRfBottom = mRFBottom
            } else {
                newRfTop = mRFTop * Source.percentRFTop
                newRfBottom = mRFBottom * PixelGraph.adjustRfBottom(mRFTop - mRFBottom)
            }


            Log.e("CON_ID", id)

            Log.e("ThisIsNotAnErrorAAA", "Top : $newRfTop Bottom : $newRfBottom RF : $mRF")
            val shadedRegion = ArrayList<Entry>()


            var int1 = 0.0f
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

            if (id.contains(Source.manual_contour_prefix)) {

                shadedDataSets.add(baselineDataSet)
            }
            // Add the shaded dataset to the list of shaded datasets
            shadedDataSets.add(shadedDataSet)
        }

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
        chart.invalidate()

//        chart.clear()


//        detectAndMarkPeaks(chart, information)
//        fillPeaks(chart, information) // imp
//        fillHighIntensityPeaks(chart, information)

//        val peaks = findPeaksAndFill(chart, information)

//
//
//        // Print the peaks to Logcat
//        for ((start, end) in peaks) {
//            Log.d("Peak Info", "Start: (${start.x}, ${start.y}), End: (${end.x}, ${end.y})")
//        }


    }


    fun findPeaksStartEnd(signal: ArrayList<Double>): List<Pair<Int, Int>> {
        var slope = 0.0
        var start: Int? = null
        val peaks = mutableListOf<Pair<Int, Int>>()

        for (i in 1 until signal.size) {
            val currentSlope = signal[i] - signal[i - 1]

            if (currentSlope > 0 && slope <= 0) {
                start = i - 1
            }

            if (currentSlope < 0 && slope >= 0 && start != null) {
                peaks.add(Pair(start, i))
            }

            slope = currentSlope
        }

        return peaks
    }


// Call this function in your onCreate or wherever appropriate

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun getDataFromIntent() {
        rFvsAreaArrayList = ArrayList<RFvsArea>()
        val intent = intent
        mode = intent.getStringExtra(resources.getString(R.string.modeKey))
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


    override fun onClick(
        position: Int,
        parentPosition: Int,
        id: String,
        rfTop: String,
        rfBottom: String,
        rf: String,
        isSelected: Boolean
    ) {
        val mRFTop = rfTop.toFloat() * intensityGap
        val mRFBottom = rfBottom.toFloat() * intensityGap
        val mRF = rf.toFloat() * intensityGap

        Log.e("ThisIsNotAnError", "Top : $mRFTop Bottom : $mRFBottom RF : $mRF")

        val contourGraphSelModelArrayList = java.util.ArrayList<ContourGraphSelModel>()

        for (i in contourDataArrayListNew.indices) {
            if (contourDataArrayListNew[i].isSelected) {
                contourGraphSelModelArrayList.add(
                    ContourGraphSelModel(
                        contourDataArrayListNew[i].rfTop,
                        contourDataArrayListNew[i].rfBottom,
                        contourDataArrayListNew[i].rf,
                        contourDataArrayListNew[i].buttonColor
                    )
                )
            }
        }

        showChart5Reverse(information, contourGraphSelModelArrayList)
    }

    override fun newOnClick(position: Int) {
    }


}