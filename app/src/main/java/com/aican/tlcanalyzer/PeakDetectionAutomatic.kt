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
import android.view.View
import android.widget.SeekBar
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
import com.aican.tlcanalyzer.databinding.ActivityPeakDetectionAutoBinding
import com.aican.tlcanalyzer.interfaces.OnClicksListeners
import com.aican.tlcanalyzer.utils.Source
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlin.math.roundToInt


class PeakDetectionAutomatic : AppCompatActivity(), OnClicksListeners {

    lateinit var binding: ActivityPeakDetectionAutoBinding
    lateinit var legacyTableView: LegacyTableView
    lateinit var rFvsAreaArrayList: ArrayList<RFvsArea>
    private lateinit var contourDataArrayListNew: java.util.ArrayList<ContourData>
    lateinit var contourSetArrayList: ArrayList<ContourSet>
    private lateinit var contourIntGraphAdapter: ContourIntGraphAdapter
    lateinit var chart: LineChart
    private var intensityGap = Source.PARTS_INTENSITY
    lateinit var intensities: ArrayList<Double>
    private lateinit var information: ArrayList<Entry>
    private var lineDataSet = LineDataSet(null, null)
    private var iLineDataSets = ArrayList<ILineDataSet>()
    var mode: String? = null
    private lateinit var lineData: LineData

    var firstTapIndex: Int? = null
    var secondTapIndex: Int? = null
    private val highlightedRegions = ArrayList<Pair<Int, Int>>()
    var lag = 100
    var threshold = 0.5
    var influence = 0.3

    private var rectangleList = ArrayList<Rect>()
    private lateinit var arrayListCont: ArrayList<ContourData>

    private lateinit var tableLayout: TableLayout

    private lateinit var tableRowHeader: TableRow
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPeakDetectionAutoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        legacyTableView = binding.legacyTableView
        chart = binding.chart

        binding.back.setOnClickListener {
            finish()
        }

        arrayListCont = ArrayList()
        rectangleList = ArrayList()

        binding.showImagebtn.text = getString(R.string.show_image)
        binding.myImageCard.visibility = View.GONE
        tableLayout = binding.table

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



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showContoursList()
        }

        showData()

        plotTable()

//        mains()

        binding.undoButton.setOnClickListener {
            handleUndo()
        }

//        highlightAndDisplayAllRegions()
        binding.zoomIn.setOnClickListener { chart.zoomIn() }

        binding.zoomOut.setOnClickListener { chart.zoomOut() }


        binding.saveThis.setOnClickListener {

            if (rectangleList != null && rectangleList.size > 0) {
                Source.rectangleList = ArrayList()
                Source.rectangleList.addAll(rectangleList)
                Source.rectangle = true
                Source.shape = 0
                Source.rectangleOneActivityToPixelActivity = true

                finish()


            } else {
                Toast.makeText(
                    this@PeakDetectionAutomatic,
                    "No spots", Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.influenceSeek.max = 100
        binding.lagSeek.max = 500
        binding.thresholdSeek.max = 100

        var mess = "Lag : $lag, Influence : $influence, Threshold : $threshold"

        binding.allValuesTxt.text = mess

        binding.influenceSeek.progress = 20
        binding.thresholdSeek.progress = 20
        binding.lagSeek.progress = 100

        highlightAndDisplayAllRegions()


        binding.influenceSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                influence = (progress.toDouble() / 100) * 2

                var mess = "Lag : $lag, Influence : $influence, Threshold : $threshold"

                binding.allValuesTxt.text = mess

                chart.clear()
                highlightAndDisplayAllRegions()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        binding.lagSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lag = progress

                var mess = "Lag : $lag, Influence : $influence, Threshold : $threshold"

                binding.allValuesTxt.text = mess

                chart.clear()
                highlightAndDisplayAllRegions()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        binding.thresholdSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                threshold = (progress.toDouble() / 100) * 2

                var mess = "Lag : $lag, Influence : $influence, Threshold : $threshold"

                binding.allValuesTxt.text = mess


                chart.clear()
                highlightAndDisplayAllRegions()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.lagDown.setOnClickListener {
            if (lag <= 0) {
                Toast.makeText(this@PeakDetectionAutomatic, "Min value reached", Toast.LENGTH_SHORT)
                    .show()
            } else {
                lag -= 2

                binding.lagSeek.progress = lag


                var mess = "Lag : $lag, Influence : $influence, Threshold : $threshold"

                binding.allValuesTxt.text = mess

                chart.clear()
                highlightAndDisplayAllRegions()
            }
        }
        binding.lagUp.setOnClickListener {

            if (lag >= 500) {
                Toast.makeText(this@PeakDetectionAutomatic, "Max value reached", Toast.LENGTH_SHORT)
                    .show()
            } else {
                lag += 2

                binding.lagSeek.progress = lag


                var mess = "Lag : $lag, Influence : $influence, Threshold : $threshold"

                binding.allValuesTxt.text = mess

                chart.clear()
                highlightAndDisplayAllRegions()
            }
        }
        binding.thesDown.setOnClickListener {
            if (threshold <= 0) {
                Toast.makeText(this@PeakDetectionAutomatic, "Min value reached", Toast.LENGTH_SHORT)
                    .show()
            } else {

                threshold -= 0.1

                binding.thresholdSeek.progress = ((threshold / 2) * 100).toInt()

                var mess = "Lag : $lag, Influence : $influence, Threshold : $threshold"

                binding.allValuesTxt.text = mess

                chart.clear()
                highlightAndDisplayAllRegions()
            }

        }
        binding.threUp.setOnClickListener {

            if (threshold >= 2) {
                Toast.makeText(this@PeakDetectionAutomatic, "Max value reached", Toast.LENGTH_SHORT)
                    .show()
            } else {

                threshold += 0.1

                binding.thresholdSeek.progress = ((threshold / 2) * 100).toInt()

                var mess = "Lag : $lag, Influence : $influence, Threshold : $threshold"

                binding.allValuesTxt.text = mess

                chart.clear()
                highlightAndDisplayAllRegions()
            }
        }

        binding.influenceDown.setOnClickListener {

            if (influence <= 0) {
                Toast.makeText(this@PeakDetectionAutomatic, "Min value reached", Toast.LENGTH_SHORT)
                    .show()
            } else {
                influence -= 0.1

                binding.influenceSeek.progress = ((influence / 2) * 100).toInt()


                var mess = "Lag : $lag, Influence : $influence, Threshold : $threshold"

                binding.allValuesTxt.text = mess
                chart.clear()
                highlightAndDisplayAllRegions()
            }
        }
        binding.influenceUp.setOnClickListener {

            if (influence >= 2) {
                Toast.makeText(this@PeakDetectionAutomatic, "Max value reached", Toast.LENGTH_SHORT)
                    .show()
            } else {
                influence += 0.1

                binding.influenceSeek.progress = ((influence / 2) * 100).toInt()


                var mess = "Lag : $lag, Influence : $influence, Threshold : $threshold"

                binding.allValuesTxt.text = mess

                chart.clear()
                highlightAndDisplayAllRegions()
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
        createTableHeader()
//        contourDataArrayList.addAll(Source.contourDataArrayList)

        var totalArea = 0f
        for (i in contourDataArrayListNew.indices) {
            if (contourDataArrayListNew[i].isSelected) {
                totalArea += contourDataArrayListNew[i].area.toFloat()
            }
        }
        for (i in contourDataArrayListNew.indices) {

            if (contourDataArrayListNew[i].isSelected) {

                LegacyTableView.insertLegacyContent(
                    contourDataArrayListNew[i].id, contourDataArrayListNew[i].rf,
                    String.format(
                        "%.2f", (1.0 / contourDataArrayListNew[i].rf.toFloat()).toString().toFloat()
                    ),

                    if (contourDataArrayListNew[i].id.contains("g")) {
                        "later"
                    } else {
                        contourDataArrayListNew[i].area
                    },
                    if (contourDataArrayListNew[i].id.contains("g")) {
                        "later"
                    } else {
                        String.format(
                            "%.2f",
                            contourDataArrayListNew[i].area.toFloat() / totalArea * intensityGap
                        ) + " %"
                    },

                    if (contourDataArrayListNew[i].id.contains("g")) {
                        "later"
                    } else {
                        contourDataArrayListNew[i].volume
                    },

                    String.format(
                        "%.2f", (contourDataArrayListNew[i].rfTop.toFloat()).toString().toFloat()
                    ),
                    String.format(
                        "%.2f", (contourDataArrayListNew[i].rfBottom.toFloat()).toString().toFloat()
                    )
                )
            }
        }
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

        highlightedRegions.clear()
        rectangleList.clear()

        arrayListCont = ArrayList()
        rectangleList = ArrayList()


        var points = DoubleArray(information.size)

        for ((i, inf) in information.withIndex()) {

            println("InforMotion " + inf.y.toDouble())
            points[i] = inf.y.toDouble()

        }


        val signals = Source.detectPeaks(points, lag, threshold, influence, Source.PARTS_INTENSITY)

        val peakStart: MutableList<Int> = ArrayList()
        val peakEnd: MutableList<Int> = ArrayList()

        var inPeak = false

        var prevSignal = -1
        for ((i, item) in signals.withIndex()) {
            if ((signals[i] == 0 || signals[i] == 1) && prevSignal == -1) {

                println("Signal Starting Point" + signals[i] + " i " + i)
                peakStart.add(i)


            }
            if ((prevSignal == 0 || prevSignal == 1) && signals[i] == -1) {
                println("Signal Ending Point" + signals[i] + " i " + i)
                peakEnd.add(i)

            }

            prevSignal = signals[i]
        }

        println("Peak starting points (x-axis): $peakStart")
        println("Peak ending points (x-axis): $peakEnd")

        val highlightedRegionPeak =
            peakStart.zip(peakEnd) // Combines lists until the end of the shorter one


        for ((i, p) in highlightedRegionPeak) {
            highlightedRegions.add(Pair(i, p))
        }
        val shadedDataSets = ArrayList<LineDataSet>()
        var bit = Source.contourBitmap

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

//            Log.e("TisNew", "t - " + newRfTop + " rf - " + newRf + " b - " + newRfBottom)


            arrayListCont.add(
                ContourData(
                    id, rf,
                    rfTop, rfBottom,
                    "0",
                    "0",
                    "0", true
                )
            )

            val re = convertRFToRect(
                rfTop.toFloat().toString(),
                rfBottom.toFloat().toString(),
                rf.toFloat().toString()
            )

//            rectangleList.add(re)

            bit = RegionOfInterest.drawRectWithROI(
                bit,
                re.left,
                re.top,
                re.width(),
                re.height()
            )
//            Log.e("TisNew", "left - " + re.left + " top - " + re.top + " width - " + re.width())

            val x: Int = re.left
            val y: Int = re.top
            val w: Int = re.width()
            val h: Int = re.height()

            val paint = Paint()
            paint.color = Color.RED
            paint.textSize = 30f
            paint.style = Paint.Style.FILL


            val canvas = Canvas(bit)
            canvas.drawText(id, x.toFloat(), y.toFloat(), paint)

//            binding.capturedImage.setImageBitmap(bit)

//            Log.e(
//                "Love", "ID : " + id.toString() + "top " +
//                        re.top + ", bottom " + re.bottom + ", left" + re.left + ", right " + re.right
//            )

            ///////////////////////////////////////////////////////////////////////////////

            val shadedRegion = ArrayList<Entry>()
            for (entry in information) {
                val x = entry.x
                val y = entry.y
                //                if (x >= mRFTop && x <= mRFBottom) {
                if (x in newRfBottom..newRfTop) {
                    // Add the points within the range
//                    Log.e("ThisIsNotAnErrorBBB", "Top : $x Bottom : $y RF : $rf")

                    shadedRegion.add(Entry(x, y))
                }
            }


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

            // Add the shaded dataset to the list of shaded datasets
            shadedDataSets.add(shadedDataSet)

//            lineDataSets.add(shadedDataSet)

            //////

        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showContoursList()
        }

        val contourGraphSelModelArrayList = ArrayList<ContourGraphSelModel>()

        for ((i, a) in contourDataArrayListNew.withIndex()) {
            contourGraphSelModelArrayList.add(
                ContourGraphSelModel(
                    a.rfTop,
                    a.rfBottom,
                    a.rf,
                    a.id,
                    a.buttonColor
                )
            )
        }


        highLightMyRegion(contourGraphSelModelArrayList, lineDataSets)


    }

    private fun highLightMyRegion(
        contourGraphSelModelArrayList: ArrayList<ContourGraphSelModel>,
        lineDataSets: ArrayList<ILineDataSet>
    ) {
        val shadedDataSets = ArrayList<LineDataSet>()

        chart.clear()
        rectangleList.clear()

        var bit = Source.contourBitmap
//        //normal

        for (cont in contourGraphSelModelArrayList) {

            val id = cont.id


//            Toast.makeText(this, "Source.PARTS_INTENSITY" + Source.PARTS_INTENSITY, Toast.LENGTH_SHORT).show();


            val newRfTop = cont.rfTop.toFloat() * Source.PARTS_INTENSITY
            val newRfBottom = cont.rfBottom.toFloat() * Source.PARTS_INTENSITY
            val newRf = cont.rf.toFloat() * Source.PARTS_INTENSITY

//            Log.e("RfsBPD", "B $newRfBottom, T $newRfTop R $newRf")

//            Log.e("TisNew", "t - " + newRfTop + " rf - " + newRf + " b - " + newRfBottom)

            val re = convertRFToRect(
                cont.rfTop.toString(),
                cont.rfBottom.toString(),
                cont.rf.toString()
            )


//            Toast.makeText(this@PeakDetectionAutomatic, "" + newRfTop, Toast.LENGTH_SHORT).show()

            rectangleList.add(re)

            bit = RegionOfInterest.drawRectWithROI(
                bit,
                re.left,
                re.top,
                re.width(),
                re.height()
            )

//            Log.e("TisNew", "left - " + re.left + " top - " + re.top + " width - " + re.width())


            val x: Int = re.left
            val y: Int = re.top
            val w: Int = re.width()
            val h: Int = re.height()

            val paint = Paint()
            paint.color = Color.RED
            paint.textSize = 30f
            paint.style = Paint.Style.FILL


            val canvas = Canvas(bit)
            canvas.drawText(id, x.toFloat(), y.toFloat(), paint)

            binding.capturedImage.setImageBitmap(bit)

            /////////////////////////////////////////////////////////////////////////////////
            var int1 = information[0].y
            var int2 = 0.0f
            val shadedRegion = ArrayList<Entry>()
            for (entry in information) {
                val x = entry.x
                val y = entry.y
                //                if (x >= mRFTop && x <= mRFBottom) {
                if (x in newRfBottom..newRfTop) {
                    // Add the points within the range
//                    Log.e("ThisIsNotAnErrorBBB", "Top : $x Bottom : $y RF : $newRf")
                    if (isApproximatelyEqual(x, newRfBottom)) {
                        int1 = y
                    }
                    if (isApproximatelyEqual(x, newRfTop)) {
                        int2 = y
                    }
                    shadedRegion.add(Entry(x, y))


                }
            }


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

//            Log.e("NotAnError", "Int1 : " + int1 + ", Int2 : " + int2)

            val slopePoints2 = ArrayList<Entry>()
            slopePoints2.add(Entry(newRfBottom, int1))
            slopePoints2.add(Entry(newRfTop, int2))

            val baselineDataSet = LineDataSet(slopePoints2, "Baseline")
            baselineDataSet.setDrawCircles(false)
            baselineDataSet.color = Color.BLACK // Set the color for the baseline

            baselineDataSet.lineWidth = 1f // Set the line width for the baseline

            lineDataSets.add(baselineDataSet)
            // Add the shaded dataset to the list of shaded datasets
            shadedDataSets.add(shadedDataSet)

            lineDataSets.add(shadedDataSet)

        }


        // Add all shaded datasets to iLineDataSets
//        lineDataSets.addAll(shadedDataSets)


        // Add each stored region to shaded regions

        plotTable()


        val lineData = LineData(lineDataSets)
        chart.data = lineData
        chart.invalidate()
    }

    fun isApproximatelyEqual(a: Float, b: Float, epsilon: Float = 1f): Boolean {
        return kotlin.math.abs(a - b) < epsilon
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showContoursList() {
        contourDataArrayListNew = java.util.ArrayList()
        for (i in arrayListCont.indices) {
            var color = getColor(R.color.grey)
            if (i == 0) {
                color = getColor(R.color.grey)
            }
            if (i == 1) {
                color = getColor(R.color.yellow)
            }
            if (i == 2) {
                color = getColor(R.color.orange)
            }
            if (i == 3) {
                color = getColor(R.color.blue)
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
                color = getColor(R.color.blue2)
            }
            contourDataArrayListNew.add(
                ContourData(
                    arrayListCont[i].id,
                    arrayListCont[i].rf,
                    arrayListCont[i].rfTop,
                    arrayListCont[i].rfBottom,
                    arrayListCont[i].cv,
                    arrayListCont[i].area,
                    arrayListCont[i].volume,
                    arrayListCont[i].isSelected,
                    color
                )
            )
        }

//        contourDataArrayListNew = Source.contourDataArrayList;
        contourIntGraphAdapter =
            ContourIntGraphAdapter(true, this, contourDataArrayListNew, 0, this, true, false, false)
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
//            information.add(
//                Entry(
//                    Source.PARTS_INTENSITY - rFvsAreaArrayList[i].rf.toString().toFloat(),
//                    rFvsAreaArrayList[i].area.toString().toFloat()
//                )
//            )

            information.add(
                Entry(
                    Source.PARTS_INTENSITY - rFvsAreaArrayList[i].rf.toString().toFloat(),
                    rFvsAreaArrayList[i].area.toString().toFloat()
                )
            )
//            mySTR = "$mySTR[ rf: " + (Source.PARTS_INTENSITY - rFvsAreaArrayList[i].rf.toString()
//                .toFloat()) + ", inten: " + rFvsAreaArrayList[i].area.toString().toFloat() + " ]"
        }
//        println(mySTR)


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


        ////////////////
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
            if (id.contains("g") || id.contains(Source.manual_contour_prefix)) {
                newRfTop = mRFTop
                newRfBottom = mRFBottom
            } else {
                newRfTop = mRFTop * Source.percentRFTop
                newRfBottom = mRFBottom * PixelGraph.adjustRfBottom(mRFTop - mRFBottom)
            }


//            Log.e("CON_ID", id)

//            Log.e("ThisIsNotAnErrorAAA", "Top : $newRfTop Bottom : $newRfBottom RF : $mRF")
            val shadedRegion = ArrayList<Entry>()
            for (entry in dataVal) {
                val x = entry.x
                val y = entry.y
                //                if (x >= mRFTop && x <= mRFBottom) {
                if (x in newRfBottom..newRfTop) {
                    // Add the points within the range
//                    Log.e("ThisIsNotAnErrorBBB", "Top : $x Bottom : $y RF : $mRF")

                    shadedRegion.add(Entry(x, y))
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

            // Add the shaded dataset to the list of shaded datasets
            shadedDataSets.add(shadedDataSet)
        }

        // Add all shaded datasets to iLineDataSets
//        iLineDataSets.addAll(shadedDataSets)
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


    }

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
//            println("Before sort")
//            println(
//                rFvsAreaArrayList[i].rf.toString() + " , " + rFvsAreaArrayList[i]
//                    .area
//            )
        }

        // Sort the list by the rf value in ascending order
        rFvsAreaArrayList.sortWith(Comparator.comparingDouble<RFvsArea> { obj: RFvsArea -> obj.rf })

        var doubleArray = DoubleArray(rFvsAreaArrayList.size)

        for (i in rFvsAreaArrayList.indices) {
//            println("After sort")

            doubleArray[i] = rFvsAreaArrayList[i].area

//            println(
//                rFvsAreaArrayList[i].rf.toString() + " , " + rFvsAreaArrayList[i]
//                    .area
//            )
        }

        val smoothData = smoothData(doubleArray, 10)

//        Toast.makeText(
//            this@PeakDetectionAutomatic,
//            "smooth size : " + smoothData.size,
//            Toast.LENGTH_SHORT
//        ).show()

        rFvsAreaArrayList = ArrayList()

        for (i in smoothData.indices) {
            rFvsAreaArrayList.add(RFvsArea(i.toDouble(), smoothData[i]))
        }

    }

    fun smoothData(data: DoubleArray, windowSize: Int): DoubleArray {
        val smoothedData = DoubleArray(data.size)

        for (i in data.indices) {
            var sum = 0.0
            var count = 0

            for (j in maxOf(0, i - windowSize / 2)..minOf(data.size - 1, i + windowSize / 2)) {
                sum += data[j]
                count++
            }

            smoothedData[i] = sum / count
        }

        return smoothedData
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

//        Log.e("ThisIsNotAnError", "Top : $mRFTop Bottom : $mRFBottom RF : $mRF")

        val contourGraphSelModelArrayList = java.util.ArrayList<ContourGraphSelModel>()

        for (i in arrayListCont.indices) {
            if (arrayListCont[i].isSelected) {
                if (contourDataArrayListNew[i].isSelected) {
                    contourGraphSelModelArrayList.add(
                        ContourGraphSelModel(
                            contourDataArrayListNew[i].rfTop,
                            contourDataArrayListNew[i].rfBottom, contourDataArrayListNew[i].rf,
                            contourDataArrayListNew[i].id,
                            contourDataArrayListNew[i].buttonColor
                        )
                    )
                }

            }
        }

        val lineDataSets = ArrayList<ILineDataSet>()
        lineDataSets.add(lineDataSet)
        highLightMyRegion(contourGraphSelModelArrayList, lineDataSets)
    }

    override fun newOnClick(position: Int) {
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


}