package com.aican.tlcanalyzer

import android.R
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.customClasses.LegacyTableView
import com.aican.tlcanalyzer.dataClasses.HrVsAreaPer
import com.aican.tlcanalyzer.databinding.ActivityHrVsAreaPerGraphBinding
import com.aican.tlcanalyzer.utils.Source
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlin.math.pow


class HrVsAreaPerGraph : AppCompatActivity() {

    lateinit var binding: ActivityHrVsAreaPerGraphBinding
    lateinit var intensityChartPlot: LineChart
    var lineData: LineData? = null
    lateinit var lineDataSetArrayList: ArrayList<LineDataSet>
    lateinit var hrVsAreaPerArrayListRM: ArrayList<HrVsAreaPer>
    lateinit var hrVsAreaPerArrayListFinal: ArrayList<HrVsAreaPer>

    lateinit var warningMessage: String
    var c_text: TextView? = null
    var a_text: TextView? = null
    var answer: TextView? = null

    var a = 0.0
    var b: Double = 0.0
    var c: Double = 0.0

    var xsum = 0.0
    var ysum = 0.0
    var xysum = 0.0
    var x2sum = 0.0

    val items = arrayOf("Order 1", "Order 2", "Order 3", "Order 4")
    val items2 = Array(24) { i -> "Hour ${i + 1}" }
    lateinit var adapter: ArrayAdapter<String>
    lateinit var adapter2: ArrayAdapter<String>

    //    companion object {
    var orderPosition: Int = 1
    var uptoHourPosition: Int = 23

    //    }
    lateinit var legacyTableView: LegacyTableView
    lateinit var legacyTableView2: LegacyTableView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHrVsAreaPerGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        c_text = binding.c
        a_text = binding.a
        answer = binding.textView6

        legacyTableView = binding.legacyTableView
        legacyTableView2 = binding.legacyTableView1


        binding.back.setOnClickListener {
            finish()
        }

        warningMessage = " Warning : "
        if (Source.hrVsAreaPerArrayListFinal == null || Source.hrVsAreaPerArrayListFinal.size == 0 || Source.hrVsAreaPerArrayListRM == null ||
            Source.hrVsAreaPerArrayListRM.size == 0
        ) {
            Toast.makeText(
                this@HrVsAreaPerGraph,
                "Please select the images before analysis: HRVsAreaAct",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }


        hrVsAreaPerArrayListFinal = ArrayList()
        hrVsAreaPerArrayListRM = ArrayList()

        hrVsAreaPerArrayListFinal.addAll(Source.hrVsAreaPerArrayListFinal)
        hrVsAreaPerArrayListRM.addAll(Source.hrVsAreaPerArrayListRM)


        println(hrVsAreaPerArrayListRM)

        val n: Int = hrVsAreaPerArrayListRM.size
        val y = DoubleArray(n)
        for (i in 0 until n) {
            y[i] = Math.log(hrVsAreaPerArrayListRM.get(i).areaPer.toDouble())
        }
        for (i in 0 until n) {
            xsum += hrVsAreaPerArrayListRM.get(i).hr.toDouble()
            ysum += y[i]
            xysum += hrVsAreaPerArrayListRM.get(i).hr.toDouble() * y[i]
            x2sum += hrVsAreaPerArrayListRM.get(i).hr.toDouble() * hrVsAreaPerArrayListRM.get(i).hr
                .toDouble()
        }

        a = (n * xysum - xsum * ysum) / (n * x2sum - xsum * xsum)
        b = (x2sum * ysum - xsum * xysum) / (x2sum * n - xsum * xsum)
        c = Math.exp(b)
        c_text!!.text = "Constant, c = "
        c_text!!.append(c.toString())
        a_text!!.text = "Power, a = "
        a_text!!.append(a.toString())
        answer!!.text =
            "Exponential Fit; y = c*exp(a*x)" //+new DecimalFormat("*.####").format(c)+"exp("+Double.toString(a)+"x)");


//        Toast.makeText(
//            this,
//            "Size Rm " + hrVsAreaPerArrayListRM.size + " Final " + hrVsAreaPerArrayListFinal.size,
//            Toast.LENGTH_SHORT
//        ).show()

        adapter = ArrayAdapter(this, R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.orderSpinner.adapter = adapter


        adapter2 = ArrayAdapter(this, R.layout.simple_spinner_item, items2)
        adapter2.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.hourSpinner.adapter = adapter2


        binding.hourSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                uptoHourPosition = position
                plotGraph4(orderPosition + 1, uptoHourPosition + 1)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.hourSpinner.setSelection(23)

            }

        }
        binding.hourSpinner.setSelection(uptoHourPosition)


        binding.orderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                orderPosition = position
                plotGraph4(orderPosition + 1, uptoHourPosition + 1)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.orderSpinner.setSelection(1)


            }
        }

        binding.orderSpinner.setSelection(orderPosition)

        plotGraph4(orderPosition + 1, uptoHourPosition + 1)


    }

    private fun curveFitRM(degree: Int): DoubleArray? {
        val x_axis = ArrayList<String>()
        val y_axis = ArrayList<String>()

        // Populate x_axis and y_axis with your data
        // ...
        hrVsAreaPerArrayListRM.forEach { item ->
            x_axis.add(item.hr.toString())
            y_axis.add(item.areaPer.toString())
        }

        // Convert ArrayLists to arrays of type double
        val x = DoubleArray(x_axis.size)
        val y = DoubleArray(y_axis.size)
        for (i in x_axis.indices) {
            x[i] = x_axis[i].toDouble()
            y[i] = y_axis[i].toDouble()
        }
        val N = x.size
        var n = degree
        val X = DoubleArray(2 * n + 1)
        for (i in 0 until 2 * n + 1) {
            X[i] = 0.0
            for (j in 0 until N) X[i] = X[i] + Math.pow(x[j], i.toDouble())
        }
        val B = Array(n + 1) { DoubleArray(n + 2) }
        val a = DoubleArray(n + 1)
        for (i in 0..n) for (j in 0..n) B[i][j] = X[i + j]
        val Y = DoubleArray(n + 1)
        for (i in 0 until n + 1) {
            Y[i] = 0.0
            for (j in 0 until N) Y[i] = Y[i] + Math.pow(x[j], i.toDouble()) * y[j]
        }
        for (i in 0..n) B[i][n + 1] = Y[i]
        n += 1
        for (i in 0 until n) for (k in i + 1 until n) if (B[i][i] < B[k][i]) for (j in 0..n) {
            val temp = B[i][j]
            B[i][j] = B[k][j]
            B[k][j] = temp
        }
        for (i in 0 until n - 1) for (k in i + 1 until n) {
            val t = B[k][i] / B[i][i]
            for (j in 0..n) B[k][j] = B[k][j] - t * B[i][j]
        }
        for (i in n - 1 downTo 0) {
            a[i] = B[i][n]
            for (j in 0 until n) if (j != i) a[i] = a[i] - B[i][j] * a[j]
            a[i] = a[i] / B[i][i]
        }
        return a
    }


    private fun curveFitFinal(degree: Int): DoubleArray? {
        val x_axis = ArrayList<String>()
        val y_axis = ArrayList<String>()

        // Populate x_axis and y_axis with your data
        // ...
        hrVsAreaPerArrayListFinal.forEach { item ->
            x_axis.add(item.hr.toString())
            y_axis.add(item.areaPer.toString())
        }

        // Convert ArrayLists to arrays of type double
        val x = DoubleArray(x_axis.size)
        val y = DoubleArray(y_axis.size)
        for (i in x_axis.indices) {
            x[i] = x_axis[i].toDouble()
            y[i] = y_axis[i].toDouble()
        }
        val N = x.size
        var n = degree
        val X = DoubleArray(2 * n + 1)
        for (i in 0 until 2 * n + 1) {
            X[i] = 0.0
            for (j in 0 until N) X[i] = X[i] + Math.pow(x[j], i.toDouble())
        }
        val B = Array(n + 1) { DoubleArray(n + 2) }
        val a = DoubleArray(n + 1)
        for (i in 0..n) for (j in 0..n) B[i][j] = X[i + j]
        val Y = DoubleArray(n + 1)
        for (i in 0 until n + 1) {
            Y[i] = 0.0
            for (j in 0 until N) Y[i] = Y[i] + Math.pow(x[j], i.toDouble()) * y[j]
        }
        for (i in 0..n) B[i][n + 1] = Y[i]
        n += 1
        for (i in 0 until n) for (k in i + 1 until n) if (B[i][i] < B[k][i]) for (j in 0..n) {
            val temp = B[i][j]
            B[i][j] = B[k][j]
            B[k][j] = temp
        }
        for (i in 0 until n - 1) for (k in i + 1 until n) {
            val t = B[k][i] / B[i][i]
            for (j in 0..n) B[k][j] = B[k][j] - t * B[i][j]
        }
        for (i in n - 1 downTo 0) {
            a[i] = B[i][n]
            for (j in 0 until n) if (j != i) a[i] = a[i] - B[i][j] * a[j]
            a[i] = a[i] / B[i][i]
        }
        return a
    }

    private fun extrapolate(x: Double, coefficients: DoubleArray): Double {
        var y = coefficients[0]
        for (j in 1 until coefficients.size) {
            y += x.pow(j.toDouble()) * coefficients[j]
        }
        return y
    }

    private fun plotGraph4(degree: Int, hour: Int) {

// Extrapolate data and add it to the array


        intensityChartPlot = binding.chart

        val entriesFinal = ArrayList<Entry>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            hrVsAreaPerArrayListRM.sortWith(Comparator.comparingDouble { obj: HrVsAreaPer -> obj.hr.toDouble() })
            hrVsAreaPerArrayListFinal.sortWith(Comparator.comparingDouble { obj: HrVsAreaPer -> obj.hr.toDouble() })
        }


//        val latestHourRM = hrVsAreaPerArrayListRM[hrVsAreaPerArrayListRM.size - 1].hr
//        val latestHourFinal = hrVsAreaPerArrayListFinal[hrVsAreaPerArrayListFinal.size - 1].hr

//        val degree = 2 // Adjust the degree as per your requirement

        val coefficients = curveFitFinal(degree)
        val coefficientsRM = curveFitRM(degree)

        val newhrVsAreaPerArrayListFinal = ArrayList<HrVsAreaPer>()
        val newhrVsAreaPerArrayListRM = ArrayList<HrVsAreaPer>()

        newhrVsAreaPerArrayListFinal.clear()
        newhrVsAreaPerArrayListRM.clear()

        for (hr in 1..hour) {
            val areaPer = extrapolate(hr.toDouble(), coefficients!!)
            newhrVsAreaPerArrayListFinal.add(HrVsAreaPer(hr.toFloat(), areaPer.toFloat()))
        }

//        plotTable2(newhrVsAreaPerArrayListFinal)

        for (hr in +1..hour) {
            val areaPer = extrapolate(hr.toDouble(), coefficientsRM!!)
            newhrVsAreaPerArrayListRM.add(HrVsAreaPer(hr.toFloat(), areaPer.toFloat()))
        }

        plotTable(newhrVsAreaPerArrayListRM, newhrVsAreaPerArrayListFinal)

//        for (hr in (latestHourFinal + 1).toInt()..24) {
//            val areaPer = extrapolate(hr.toDouble(), coefficients)
//            hrVsAreaPerArrayListFinal.add(HrVsAreaPer(hr.toFloat(), areaPer))
//        }
//
//        for (hr in (latestHourRM + 1).toInt()..24) {
//            val areaPer = extrapolate(hr.toDouble(), coefficients)
//            hrVsAreaPerArrayListFinal.add(HrVsAreaPer(hr.toFloat(), areaPer))
//        }

//        extrapolateData()


        for (my in newhrVsAreaPerArrayListRM) {

            if (my.areaPer <= 0) {
                warningMessage =
                    warningMessage + " \n -> Percentage Area {RM Spot} for Hour " + my.hr + " is missing"
//                Toast.makeText(
//                    this,
//                    "You cannot plot the graph, please check all HR's, RM's & Final's",
//                    Toast.LENGTH_LONG
//                ).show()
//
//                finish()

                binding.warningM.text = warningMessage
            }

            println("HR = " + my.hr + " Area = " + String.format("%.2f", my.areaPer).toFloat())
            entriesFinal.add(
                Entry(
                    my.hr,
                    String.format("%.2f", my.areaPer).toFloat()
                )
            )
        }

//        entriesFinal.add(Entry(1f, 10.88f))
//        entriesFinal.add(Entry(2f, 20.56f))
        val dataSetFinal = LineDataSet(entriesFinal, "Final Product")
        dataSetFinal.color = Color.RED

        val entriesRM = ArrayList<Entry>()

        for (my in newhrVsAreaPerArrayListFinal) {

            if (my.areaPer <= 0) {


                warningMessage =
                    warningMessage + " \n -> Percentage Area {Final Spot} for Hour " + my.hr + " is missing"

//                Toast.makeText(
//                    this,
//                    "You cannot plot the graph, please check all HR's, RM's & Final's",
//                    Toast.LENGTH_LONG
//                ).show()
//
//                finish()

                binding.warningM.text = warningMessage
            }
            println("HR = " + my.hr + " Area = " + String.format("%.2f", my.areaPer).toFloat())
            entriesRM.add(
                Entry(
                    my.hr,
                    String.format("%.2f", my.areaPer).toFloat()
                )
            )
        }

//        entriesRM.add(Entry(1f, 30f))
//        entriesRM.add(Entry(2f, 15f))
        val dataSetRM = LineDataSet(entriesRM, "Raw Material")
        dataSetRM.color = Color.BLUE

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSetFinal)
        dataSets.add(dataSetRM)

        val lineData = LineData(dataSets)
        intensityChartPlot.data = lineData

        val xAxis = intensityChartPlot.xAxis
        xAxis.granularity = 1f // Set the interval between labels
        xAxis.axisMinimum = 1f // Set the minimum value on the x-axis
        xAxis.axisMaximum = 24f


        intensityChartPlot.invalidate()
    }


    private fun plotTable(
        newHrvsArea: ArrayList<HrVsAreaPer>,
        newHrvsAreaFinal: ArrayList<HrVsAreaPer>
    ) {

        LegacyTableView.insertLegacyTitle("HR", "Raw Material Area", "Final Product Area")

        var totalArea = 0.0

        for (i in newHrvsArea.indices) {
            LegacyTableView.insertLegacyContent(
                newHrvsArea.get(i).hr.toString(),
                newHrvsArea.get(i).areaPer.toString(),
                newHrvsAreaFinal.get(i).areaPer.toString()
            )
        }
        legacyTableView2.setTheme(LegacyTableView.CUSTOM)
        legacyTableView2.setContent(LegacyTableView.readLegacyContent())
        legacyTableView2.setTitle(LegacyTableView.readLegacyTitle())
//        legacyTableView.setBottomShadowVisible(true);
        //        legacyTableView.setBottomShadowVisible(true);
        legacyTableView2.setHighlight(LegacyTableView.ODD)
        legacyTableView2.setBottomShadowVisible(false)
        legacyTableView2.setFooterTextAlignment(LegacyTableView.CENTER)
        legacyTableView2.setTableFooterTextSize(5)
        legacyTableView2.setTableFooterTextColor("#f0f0ff")
        legacyTableView2.setTitleTextAlignment(LegacyTableView.CENTER)
        legacyTableView2.setContentTextAlignment(LegacyTableView.CENTER)
        legacyTableView2.setTablePadding(20)
        legacyTableView2.setBackgroundOddColor("#F0F0FF")
        legacyTableView2.setHeaderBackgroundLinearGradientBOTTOM("#F0F0FF")
        legacyTableView2.setHeaderBackgroundLinearGradientTOP("#F0F0FF")
        legacyTableView2.setBorderSolidColor("#f0f0ff")
        legacyTableView2.setTitleTextColor("#212121")
        legacyTableView2.setTitleFont(LegacyTableView.BOLD)
        legacyTableView2.setZoomEnabled(false)
        legacyTableView2.setShowZoomControls(false)

        legacyTableView2.setContentTextColor("#000000")
        legacyTableView2.build()
    }

    private fun plotTable2(newHrvsArea: ArrayList<HrVsAreaPer>) {

        LegacyTableView.insertLegacyTitle("HR", "Area")

        var totalArea = 0.0



        for (i in newHrvsArea.indices) {
            LegacyTableView.insertLegacyContent(
                newHrvsArea.get(i).hr.toString(),
                newHrvsArea.get(i).areaPer.toString()
            )
        }
        legacyTableView.setTheme(LegacyTableView.CUSTOM)
        //get titles and contents
        //get titles and contents
        legacyTableView.setContent(LegacyTableView.readLegacyContent())
        legacyTableView.setTitle(LegacyTableView.readLegacyTitle())
        legacyTableView.setBottomShadowVisible(true)
        legacyTableView.setHighlight(LegacyTableView.ODD) //highlight rows oddly or evenly

        //tableView.setHighlight(EVEN);
        //tableView.setHighlight(EVEN);
        legacyTableView.setBottomShadowVisible(true)
        legacyTableView.setFooterTextAlignment(LegacyTableView.CENTER)
        legacyTableView.setTableFooterTextSize(5)
        legacyTableView.setTableFooterTextColor("#000000")
        legacyTableView.setTitleTextAlignment(LegacyTableView.CENTER)
        legacyTableView.setContentTextAlignment(LegacyTableView.CENTER)
        legacyTableView.setTablePadding(20) //increasing spacing will increase the table size

        //tableView.setBottomShadowColorTint("#ffffff");

        //tableView.setBackgroundEvenColor("#FFCCBC");
        //tableView.setBackgroundEvenColor("#303F9F");
        //tableView.setBottomShadowColorTint("#ffffff");

        //tableView.setBackgroundEvenColor("#FFCCBC");
        //tableView.setBackgroundEvenColor("#303F9F");
        legacyTableView.setBackgroundOddColor("#dcdede")
        //you can also declare your color values as global strings to make your work easy :)
        //you can also declare your color values as global strings to make your work easy :)
        legacyTableView.setHeaderBackgroundLinearGradientBOTTOM("#dcdede") //header background bottom color

        legacyTableView.setHeaderBackgroundLinearGradientTOP("#dcdede") //header background top color

        legacyTableView.setBorderSolidColor("#000000")
        legacyTableView.setTitleTextColor("#000000")
        legacyTableView.setTitleFont(LegacyTableView.BOLD)
        legacyTableView.setZoomEnabled(true)
        legacyTableView.setShowZoomControls(true)
        //by default the initial scale is 0, you
        // may change this depending on initiale scale preferences
        //tableView.setInitialScale(100);//default initialScale is zero (0)
        //by default the initial scale is 0, you
        // may change this depending on initiale scale preferences
        //tableView.setInitialScale(100);//default initialScale is zero (0)
        legacyTableView.setContentTextColor("#000000")
        legacyTableView.build()
    }


    private fun plotGraph3() {
        intensityChartPlot = binding.chart

        val entries = ArrayList<Entry>()
        entries.add(Entry(1f, 10f))
        entries.add(Entry(2f, 20f))

        val dataSet = LineDataSet(entries, "Example")
        dataSet.color = Color.BLUE

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)

        val lineData = LineData(dataSets)
        intensityChartPlot.data = lineData

        intensityChartPlot.invalidate()
    }


    private fun plotGraph2() {
        intensityChartPlot = binding.chart

        val hrVsAreaPerArrayListFinal = Source.hrVsAreaPerArrayListFinal
        val hrVsAreaPerArrayListRM = Source.hrVsAreaPerArrayListRM

        val dataSets = ArrayList<ILineDataSet>()

        dataSets.add(createLineDataSet(hrVsAreaPerArrayListFinal, "Final Product", Color.RED))
        dataSets.add(createLineDataSet(hrVsAreaPerArrayListRM, "Raw Material", Color.GREEN))

        val lineData = LineData(dataSets)
        intensityChartPlot.data = lineData

        // Customize chart appearance if needed
        intensityChartPlot.setDrawGridBackground(false)

        // Refresh chart
        intensityChartPlot.invalidate()
    }

    private fun createLineDataSet(
        dataList: List<HrVsAreaPer>,
        label: String,
        color: Int
    ): LineDataSet {
        val entries = ArrayList<Entry>()

        for (item in dataList) {
            entries.add(Entry(item.hr, item.areaPer))
        }

        val dataSet = LineDataSet(entries, label)
        dataSet.color = color
        dataSet.setCircleColor(color)
        dataSet.valueTextColor = Color.BLACK

        return dataSet
    }

    private fun plotGraph() {
        intensityChartPlot = binding.chart
        lineDataSetArrayList = ArrayList()

        val informationRM = ArrayList<Entry>()
        val informationFinal = ArrayList<Entry>()
        val dataSets = ArrayList<ILineDataSet>()

        for (my in hrVsAreaPerArrayListRM) {
            informationRM.add(
                Entry(
                    my.hr,
                    String.format("%.2f", my.areaPer).toFloat()
                )
            )
        }

        lineDataSetArrayList.add(LineDataSet(informationRM, "Raw Material"))

        for (my in hrVsAreaPerArrayListFinal) {
            informationFinal.add(
                Entry(
                    my.hr,
                    String.format("%.2f", my.areaPer).toFloat()
                )
            )
        }

        lineDataSetArrayList.add(LineDataSet(informationFinal, "Final Product"))

        for ((i, line) in lineDataSetArrayList.withIndex()) {
            if (i == 0) {
                line.color = Color.GREEN
            }
            if (i == 1) {
                line.color = Color.RED

            }
            dataSets.add(line)

        }

//        lineDataSetArrayList.get(0).color = Color.GREEN
//        lineDataSetArrayList.get(1).color = Color.RED
//
//
//        dataSets.addAll(lineDataSetArrayList)

        showChart(dataSets)


    }

    private fun showChart(iLineDataSets: ArrayList<ILineDataSet>) {
        lineData = LineData(iLineDataSets)
        intensityChartPlot.clear()
        intensityChartPlot.data = lineData
        intensityChartPlot.invalidate()

    }

//    fun extrapolate(x: Double, coefficients: DoubleArray): Float {
//        var result = coefficients[0] // Initialize result with the constant term
//        for (i in 1 until coefficients.size) {
//            result += coefficients[i] * Math.pow(x, i.toDouble()) // Add higher-order terms
//        }
//        return result.toFloat()
//    }

    fun performExtrapolation(dataList: ArrayList<HrVsAreaPer?>?, coefficients: DoubleArray?) {
        // Example usage: extrapolate x = 6 hours
        val xValueToExtrapolate = 6.0 // Adjust as needed
        val extrapolatedY = extrapolate(xValueToExtrapolate, coefficients!!)

        // Use the extrapolatedY as needed (display, store, etc.)
        // For example, you can display it in a TextView:
        val extrapolatedValueTextView = binding.extrapolatedValueTextView
        extrapolatedValueTextView.text = "Extrapolated Y: $extrapolatedY"
    }


}