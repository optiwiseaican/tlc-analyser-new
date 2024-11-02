package com.aican.tlcanalyzer

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.adapterClasses.AnalMultiIntAdapter
import com.aican.tlcanalyzer.dataClasses.AnalMultiIntModel
import com.aican.tlcanalyzer.dataClasses.ContourData
import com.aican.tlcanalyzer.dataClasses.RFvsArea
import com.aican.tlcanalyzer.dataClasses.SplitContourData
import com.aican.tlcanalyzer.databinding.ActivityAnalyseMultipleIntensityBinding
import com.aican.tlcanalyzer.interfaces.OnClicksListeners
import com.aican.tlcanalyzer.utils.RandomColors
import com.aican.tlcanalyzer.utils.Source
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.Random


class AnalyseMultipleIntensity : AppCompatActivity(), OnClicksListeners {

    lateinit var binding: ActivityAnalyseMultipleIntensityBinding
    lateinit var intensityChartPlot: LineChart
    lateinit var id: String
    lateinit var projectName: String
    lateinit var adapter: AnalMultiIntAdapter
    lateinit var contourDataArrayListNew: ArrayList<ContourData>
    lateinit var splitContourData: ArrayList<SplitContourData>
    lateinit var splitContourDatas: ArrayList<SplitContourData>
    private val splitContourDataList2 = ArrayList<SplitContourData>()
    lateinit var splitContourDataList: ArrayList<SplitContourData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyseMultipleIntensityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.back.setOnClickListener {
            finish()
        }

//        for (splitData in Source.splitContourDataList){
//            splitContourDataList
//        }

        splitContourDataList = ArrayList(listOf(*Source.splitContourDataList.toTypedArray()))

        binding.sync.visibility = View.VISIBLE
        binding.syncProgress.visibility = View.GONE

        splitContourData = ArrayList(splitContourDataList)
        splitContourDatas = ArrayList()

        Log.e("ThisIsNotError", splitContourDataList.size.toString())



        id = intent.getStringExtra("id").toString()
        projectName = intent.getStringExtra("projectName").toString()

        binding.sync.setOnClickListener {

            val i = Intent(this@AnalyseMultipleIntensity, AnalyseMultipleIntensity::class.java)
            i.putExtra("w", "split")
            i.putExtra("img_path", intent.getStringExtra("img_path").toString())
            i.putExtra("projectName", intent.getStringExtra("projectName").toString())
            i.putExtra(
                "projectDescription",
                i.getStringExtra("projectDescription").toString()
            )
            i.putExtra("projectImage", intent.getStringExtra("projectImage").toString())
            i.putExtra("imageName", intent.getStringExtra("imageName").toString())

            i.putExtra("tableName", intent.getStringExtra("tableName").toString())

            i.putExtra("roiTableID", intent.getStringExtra("roiTableID").toString())
            i.putExtra("thresholdVal", intent.getStringExtra("thresholdVal").toString())
            i.putExtra("numberOfSpots", intent.getStringExtra("numberOfSpots").toString())
            i.putExtra("id", intent.getStringExtra("id").toString())
            i.putExtra("pid", intent.getStringExtra("pid").toString())
            startActivity(i)
            finish()
            overridePendingTransition(0, 0)

        }
        binding.zoomIn.setOnClickListener(View.OnClickListener { binding.chart.zoomIn() })

        binding.zoomOut.setOnClickListener(View.OnClickListener { binding.chart.zoomOut() })



        showAllCon()

        Log.e("ThisIsNotErrors", splitContourDatas.size.toString())


        plotROIGraphs(splitContourDatas)

        binding.generatePDFReport.visibility = View.GONE

        binding.generatePDFReport.setOnClickListener {

        }

    }

    private fun showAllCon() {

        val colorGenerator = RandomColors()
        val randomDarkColor = colorGenerator.getRandomDarkColor()

//        val splitContourDataList2 = ArrayList<SplitContourData>()
        val analMultiArrayList = ArrayList<AnalMultiIntModel>()

        for (split in splitContourData) {
            val updatedContourDataList = ArrayList<ContourData>()

            for ((k, contourData) in split.contourData.withIndex()) {

                val color = colorGenerator.getRandomDarkColor()


                contourData.buttonColor = color
                contourData.isSelected = true // Assuming you want all contourData to be selected

                updatedContourDataList.add(contourData)
            }

            Toast.makeText(
                this@AnalyseMultipleIntensity,
                "S" + updatedContourDataList.size,
                Toast.LENGTH_SHORT
            ).show()

            val updatedSplitContourData = SplitContourData(
                split.name,
                split.isSelected,
                split.contourImageName,
                split.mainImageName,
                split.hr,
                split.rmSpot,
                split.finalSpot,
                split.volumeDATAList,
                split.rFvsAreaArrayList,
                split.contourSetArrayList,
                updatedContourDataList,
                split.labelDataArrayList
            )
            if (updatedSplitContourData.isSelected) {

                analMultiArrayList.add(
                    AnalMultiIntModel(
                        split.name,
                        updatedContourDataList
                    )
                )

            }
            splitContourDataList2.add(updatedSplitContourData)
        }

        splitContourDatas.addAll(splitContourDataList2)

        adapter = AnalMultiIntAdapter(this@AnalyseMultipleIntensity, analMultiArrayList, this)
        binding.contourListRecView.adapter = adapter
        adapter.notifyDataSetChanged()
    }


    var lineData: LineData? = null
    var lineDataSetArrayList: ArrayList<LineDataSet>? = null

    private fun plotROIGraphs(splitContourDataList: ArrayList<SplitContourData>) {


        // intensity plot start
        intensityChartPlot = binding.chart

        lineDataSetArrayList = ArrayList()
        val dataSets = ArrayList<ILineDataSet>()
        for (i in splitContourDataList.indices) {
            val multiSplitIntensity = splitContourDataList[i]

            val rFvsAreaArrayList = multiSplitIntensity.getrFvsAreaArrayList()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                rFvsAreaArrayList.sortWith(Comparator.comparingDouble { obj: RFvsArea -> obj.rf })
            }

            var mySTR = "Pixel Anal"

            val informationROI = ArrayList<Entry>()
            for (j in rFvsAreaArrayList.indices) {

                val scaledYValue: Float = rFvsAreaArrayList[j].area.toString()
                    .toFloat() / Source.SCALING_FACTOR_INT_GRAPH // Adjust the scaling factor as needed


//                informationROI.add(
//                    Entry(
//                        Source.PARTS_INTENSITY - rFvsAreaArrayList[j].rf.toString().toFloat(),
//                        scaledYValue
//                    )
//                )
                informationROI.add(
                    Entry(
                        Source.PARTS_INTENSITY - rFvsAreaArrayList[j].rf.toString().toFloat(),
                        rFvsAreaArrayList[j].area.toString()
                            .toFloat()
                    )
                )
//                mySTR =
//                    "$mySTR[ rf: " + (Source.PARTS_INTENSITY - rFvsAreaArrayList[j].rf.toString()
//                        .toFloat()) + ", inten: " + (rFvsAreaArrayList[j].area.toString()
//                        .toFloat()) + " ]"

            }
            println(mySTR)

            informationROI.reverse()

            lineDataSetArrayList!!.add(LineDataSet(informationROI, multiSplitIntensity.name))

//            lineDataSetArrayList!![i].mode = LineDataSet.Mode.HORIZONTAL_BEZIER

            lineDataSetArrayList!![i].color = setColor(i)

            if (multiSplitIntensity.isSelected) {
                dataSets.add(lineDataSetArrayList!![i])

                lineDataSetArrayList!![i].setDrawCircles(false)
                lineDataSetArrayList!![i].lineWidth = 1f
                lineDataSetArrayList!![i].setDrawFilled(false)

                val contourDataArray = multiSplitIntensity.contourData


                for (s in contourDataArray.indices) {

                    val ids = contourDataArray.get(s).id

                    val mRFTop = (String.format(
                        "%.0f",
                        contourDataArray.get(s).getRfTop().toFloat() * Source.PARTS_INTENSITY
                    ).toInt()).toFloat()
                    val mRFBottom = (String.format(
                        "%.0f",
                        contourDataArray.get(s).getRfBottom().toFloat() * Source.PARTS_INTENSITY
                    ).toInt()).toFloat()
                    val mRF = (String.format(
                        "%.0f",
                        contourDataArray.get(s).getRf().toFloat() * Source.PARTS_INTENSITY
                    ).toInt()).toFloat()


                    var newRfTop: Float = mRFTop * Source.percentRFTop

                    var newRfBottom: Float = mRFBottom * Source.percentRFBottom

                    if (ids.contains("m")) {
                        newRfTop = mRFTop
                        newRfBottom = mRFBottom
                    } else {
                        newRfTop = mRFTop * Source.percentRFTop
                        newRfBottom = mRFBottom * PixelGraph.adjustRfBottom(mRFTop - mRFBottom)
                    }


                    val shadedRegion = ArrayList<Entry>()

                    for (entry in informationROI) {
                        val x = entry.x
                        val y = entry.y
                        if (x in newRfBottom..newRfTop) {
                            // Add the points within the range
                            shadedRegion.add(Entry(x, y))
                        }
                    }

                    val shadedDataSet = LineDataSet(shadedRegion, "Spot ${ids}")

                    Log.d("ButtonColor", contourDataArray.get(s).buttonColor.toString())

                    shadedDataSet.color = contourDataArray.get(s).buttonColor
                    shadedDataSet.setDrawFilled(true)
                    shadedDataSet.setDrawCircles(false)
                    shadedDataSet.fillColor = contourDataArray.get(s).buttonColor
                    shadedDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                    shadedDataSet.lineWidth = 0f

                    if (contourDataArray.get(s).isSelected) {
                        dataSets.add(shadedDataSet)
                    }
                }

            }
            // Add all shaded datasets to iLineDataSets

        }
        showChart(dataSets)
    }

    private fun showChart(iLineDataSets: ArrayList<ILineDataSet>) {

        intensityChartPlot.clear()
        lineData = LineData(iLineDataSets)
        intensityChartPlot.clear()

        intensityChartPlot.data = lineData
//        intensityChartPlot.getLegend().setEnabled(false)

        intensityChartPlot.invalidate()

        binding.syncProgress.visibility = View.GONE
        binding.sync.visibility = View.VISIBLE
    }

    private fun setColor(i: Int): Int {

        when (i) {
            0 -> {
                return Color.RED
            }

            1 -> {
                return Color.GREEN
            }

            2 -> {
                return Color.BLUE
            }

            3 -> {
                return Color.BLACK
            }

            4 -> {
                return Color.GREEN
            }

            else -> {
                return getRandomColor()
            }

        }

    }

    fun getRandomColor(): Int {
        val random = Random()
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))
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


        if (parentPosition >= 0 && parentPosition < splitContourDataList2.size) {
            val splitContourData = splitContourDataList2[parentPosition]
            val contourData = splitContourData.contourData.find { it.id == id }

            contourData?.isSelected = isSelected

            splitContourDataList2[parentPosition] = splitContourData

            adapter.notifyDataSetChanged()
        } else {
            // Handle the case where the parent position is out of bounds
        }

        splitContourDatas.clear()
        splitContourDatas.addAll(splitContourDataList2)

        plotROIGraphs(splitContourDatas)
    }

    override fun newOnClick(position: Int) {

    }


}