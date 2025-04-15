package com.aican.tlcanalyzer

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.adapterClasses.AnalMultiIntAdapter
import com.aican.tlcanalyzer.dataClasses.AnalMultiIntModel
import com.aican.tlcanalyzer.dataClasses.ContourData
import com.aican.tlcanalyzer.dataClasses.RFvsArea
import com.aican.tlcanalyzer.dataClasses.SplitContourData
import com.aican.tlcanalyzer.databinding.ActivityAnalyseMultipleIntensityBinding
import com.aican.tlcanalyzer.interfaces.OnCheckBoxChangeListener
import com.aican.tlcanalyzer.interfaces.OnClicksListeners
import com.aican.tlcanalyzer.utils.RandomColors
import com.aican.tlcanalyzer.utils.Source
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.Random


class AnalyseMultipleIntensity : AppCompatActivity(), OnClicksListeners, OnCheckBoxChangeListener {

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
    lateinit var tableLayout: TableLayout
    private var tableRowHeader: TableRow? = null

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
        tableLayout = binding.table
        splitContourData = ArrayList(splitContourDataList)
        splitContourDatas = ArrayList()

        Log.e("ThisIsNotError", splitContourDataList.size.toString())
//
//        val displayMetrics = resources.displayMetrics
//        val screenWidth = displayMetrics.widthPixels
//        val chartWidth = screenWidth - (20 * displayMetrics.density).toInt()
//        val chartHeight = (chartWidth * 3) / 2  // Height for 2:3 aspect
//
//        val params = binding.chart.layoutParams
//        params.width = chartWidth
//        params.height = chartHeight
//        binding.chart.layoutParams = params

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.chart.post {
                val displayMetrics = resources.displayMetrics

                val visibleHeight = binding.root.height
                val reservedSpace = (64 * displayMetrics.density).toInt()
                val usableHeight = visibleHeight - reservedSpace

                val chartHeight = usableHeight
                val chartWidth = (chartHeight * 3) / 2

                val params = binding.chart.layoutParams
                params.height = chartHeight
                params.width = chartWidth
                binding.chart.layoutParams = params
            }
        }





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
        plotTable(splitContourDataList2)
        binding.showTable.text = "Hide Table"
        var show_table = true
        binding.showTable.setOnClickListener {
            if (show_table) {
                binding.showTable.text = "Show Table"
                show_table = false
                binding.table.visibility = View.GONE
            } else {
                binding.showTable.text = "Hide Table"
                show_table = true
                binding.table.visibility = View.VISIBLE

            }
        }

    }

    fun plotTable(splitContourData: ArrayList<SplitContourData>) {
        createTableHeader()
        Log.d("OrderCheck", "Before plotTable: ${splitContourData.map { it.name }}")

        for (spliData in splitContourData) { // Ensure correct order
            var totalArea = 0f

            if (spliData.isSelected) {
                for (contour in spliData.contourData) {
                    totalArea += contour.area.toFloat()
                }

                for (i in spliData.contourData.indices) {
                    createTableRow(
                        spliData.name,
                        spliData.contourData[i].getId(),
                        spliData.contourData[i].getRf(),
                        String.format("%.2f", (1.0 / spliData.contourData[i].getRf().toFloat())),
                        Source.formatToTwoDecimalPlaces(spliData.contourData[i].area.toString()),
                        String.format(
                            "%.2f",
                            (spliData.contourData[i].area.toFloat() / totalArea) * 100
                        ) + " %",
                        spliData.contourData[i].getVolume(),
                        spliData.labelDataArrayList[i].getLabel(),
                        i
                    )
                }
            }
        }
    }

    //"ID", "Rf", "Cv", "Area", "% area", "Volume"
    private fun createTableRow(
        imageName: String,
        ID: String, Rf: String, Cv: String, Area: String, pArea: String,
        volume: String, label: String, index: Int
    ) {
        val tableRow = TableRow(this)
        val lp = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tableRow.layoutParams = lp

        val textViewImageName = TextView(this)
        val textViewID = TextView(this)
        val textViewRf = TextView(this)
        val textViewCv = TextView(this)
        val textViewArea = TextView(this)
        val textViewPArea = TextView(this)
        val textViewVolume = TextView(this)
        val textViewLabel = TextView(this)

        textViewImageName.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT,
            0f
        )
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
        if (Source.SHOW_VOLUME_DATA) textViewVolume.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT,
            0.3f
        )
        if (Source.SHOW_LABEL_DATA)
            textViewLabel.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT,
                0.3f
            )

        textViewImageName.gravity = Gravity.CENTER
        textViewID.gravity = Gravity.CENTER
        textViewRf.gravity = Gravity.CENTER
        textViewCv.gravity = Gravity.CENTER
        textViewArea.gravity = Gravity.CENTER
        textViewPArea.gravity = Gravity.CENTER
        textViewVolume.gravity = Gravity.CENTER
        textViewLabel.gravity = Gravity.CENTER

        textViewImageName.maxLines = 3
        textViewCv.maxLines = 3
        textViewRf.maxLines = 2
        textViewPArea.maxLines = 2
        textViewArea.maxLines = 2
        textViewVolume.maxLines = 2
        textViewLabel.maxLines = 2

        textViewImageName.setPadding(5, 15, 5, 15)
        textViewID.setPadding(5, 15, 5, 15)
        textViewRf.setPadding(5, 15, 5, 15)
        textViewCv.setPadding(5, 15, 5, 15)
        textViewArea.setPadding(5, 15, 5, 15)
        textViewPArea.setPadding(5, 15, 5, 15)
        textViewVolume.setPadding(5, 15, 5, 15)
        textViewLabel.setPadding(5, 15, 5, 15)

        textViewImageName.text = imageName
        textViewID.text = ID
        textViewRf.text = Rf
        textViewCv.text = Cv
        textViewArea.text = Area
        textViewPArea.text = pArea
        textViewVolume.text = volume
        textViewLabel.text = label

        textViewImageName.setTextColor(getColor(R.color.black))
        textViewID.setTextColor(getColor(R.color.black))
        textViewRf.setTextColor(getColor(R.color.black))
        textViewCv.setTextColor(getColor(R.color.black))
        textViewArea.setTextColor(getColor(R.color.black))
        textViewPArea.setTextColor(getColor(R.color.black))
        textViewVolume.setTextColor(getColor(R.color.black))
        textViewLabel.setTextColor(getColor(R.color.black))

        textViewImageName.setBackgroundResource(R.drawable.cell_shape_white)
        textViewID.setBackgroundResource(R.drawable.cell_shape_white)
        textViewRf.setBackgroundResource(R.drawable.cell_shape_grey)
        textViewCv.setBackgroundResource(R.drawable.cell_shape_white)
        textViewArea.setBackgroundResource(R.drawable.cell_shape_grey)
        textViewPArea.setBackgroundResource(R.drawable.cell_shape_white)
        textViewVolume.setBackgroundResource(R.drawable.cell_shape_grey)
        textViewLabel.setBackgroundResource(R.drawable.cell_shape_grey)


        if (index == -1) {
            tableRowHeader = tableRow
            textViewImageName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_medium).toInt()
                    .toFloat()
            )
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
            textViewLabel.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_medium).toInt()
                    .toFloat()
            )

            //            textViewRf.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_sort_by_alpha_black, 0);
//            textViewCv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_sort_by_alpha_black, 0);
//            textViewArea.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_sort_by_alpha_black, 0);
//            textViewPArea.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_sort_by_alpha_black, 0);
//            textViewVolume.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_sort_by_alpha_black, 0);
            textViewImageName.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewID.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewRf.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewCv.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewArea.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewPArea.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewVolume.setBackgroundResource(R.drawable.cell_shape_blue)
            textViewLabel.setBackgroundResource(R.drawable.cell_shape_blue)
        } else {
            textViewImageName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_small).toInt()
                    .toFloat()
            )
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
            textViewLabel.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_small).toInt()
                    .toFloat()
            )
        }

        tableRow.addView(textViewImageName)
        tableRow.addView(textViewID)
        tableRow.addView(textViewRf)
        tableRow.addView(textViewCv)
        tableRow.addView(textViewArea)
        tableRow.addView(textViewPArea)
        if (Source.SHOW_VOLUME_DATA) tableRow.addView(textViewVolume)
        if (Source.SHOW_LABEL_DATA) tableRow.addView(textViewLabel)


        tableLayout.addView(tableRow)
//        tableLayout.addView(tableRow, index + 1)
    }

    private fun createTableHeader() {
        tableLayout.removeAllViews()
        createTableRow("Img Name", "ID", "Rf", "Cv", "Area", "% area", "Volume", "Label", -1)
    }

    private fun showAllCon() {
        Log.d("OrderCheck", "Before showAllCon: ${splitContourData.map { it.name }}")

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

//            Toast.makeText(
//                this@AnalyseMultipleIntensity,
//                "S" + updatedContourDataList.size,
//                Toast.LENGTH_SHORT
//            ).show()

            val updatedSplitContourData = SplitContourData(
                split.id,
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
                split.labelDataArrayList,
                split.intensityPlotTableID
            )
            if (updatedSplitContourData.isSelected) {

                analMultiArrayList.add(
                    AnalMultiIntModel(
                        true,
                        split.name,
                        split.mainImageName,
                        split.contourImageName,
                        updatedContourDataList
                    )
                )

            }
            splitContourDataList2.add(updatedSplitContourData)
        }

        splitContourDatas.addAll(splitContourDataList2)

        adapter =
            AnalMultiIntAdapter(id, this@AnalyseMultipleIntensity, analMultiArrayList, this, this)
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

                    //            Log.e("CON_ID", id);

//            Toast.makeText(this, "ID" + id, Toast.LENGTH_SHORT).show();


                    var newRfTop: Float = mRFTop * Source.percentRFTop

                    var newRfBottom: Float = mRFBottom * Source.percentRFBottom


                    if (ids.contains("m")) {
                        newRfTop = mRFTop
                        newRfBottom = mRFBottom
                    } else {
                        newRfTop = mRFTop * Source.percentRFTop
                        newRfBottom = mRFBottom * PixelGraph.adjustRfBottom(mRFTop - mRFBottom)
                    }
                    newRfTop = mRFTop
                    newRfBottom = mRFBottom


                    Log.e(
                        "ThisIsNotAnErrorVishalMultiple",
                        "Top : $newRfTop Bottom : $newRfBottom RF : $mRF"
                    )

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

        val xAxis = intensityChartPlot.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM


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
                return Color.BLUE///////////////////////////
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

    private fun getRandomColor(): Int {
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

    override fun onCheckBoxChange(
        position: Int,
        data: AnalMultiIntModel
    ) {
        if (position >= 0 && position < splitContourDataList2.size) {
            // Find the index of the matching item
            val index =
                splitContourDataList2.indexOfFirst { it.contourImageName == data.contourImageName }

            if (index != -1) { // Ensure the item was found
                splitContourDataList2[index].isSelected = data.isSelected
                plotROIGraphs(splitContourDataList2)
                plotTable(splitContourDataList2)
            }
        }
    }


}