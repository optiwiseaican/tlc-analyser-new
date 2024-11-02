package com.aican.tlcanalyzer.trash

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.PixelGraph
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.adapterClasses.ContourIntGraphAdapter
import com.aican.tlcanalyzer.dataClasses.ContourData
import com.aican.tlcanalyzer.dataClasses.ContourGraphSelModel
import com.aican.tlcanalyzer.dataClasses.RFvsArea
import com.aican.tlcanalyzer.databinding.ActivityPeakDetectionBinding
import com.aican.tlcanalyzer.interfaces.OnClicksListeners
import com.aican.tlcanalyzer.utils.Source
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class PeakDetection : AppCompatActivity(), OnClicksListeners {

    lateinit var binding: ActivityPeakDetectionBinding
    lateinit var rFvsAreaArrayList: ArrayList<RFvsArea>
    var mode: String? = null
    private lateinit var information: ArrayList<Entry>
    private lateinit var contourDataArrayListNew: ArrayList<ContourData>
    private lateinit var arrayListCont: ArrayList<ContourData>
    private lateinit var contourIntGraphAdapter: ContourIntGraphAdapter
    private var lineDataSet = LineDataSet(null, null)
    private var iLineDataSets = ArrayList<ILineDataSet>()
    private lateinit var lineData: LineData
    private var intensityGap = Source.PARTS_INTENSITY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPeakDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arrayListCont = ArrayList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getDataFromIntent()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showContoursList()
        }

        showData()


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
            ContourIntGraphAdapter(this, contourDataArrayListNew, 0, this, true, false, false)
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
            if (id.contains("g") || id.contains("m")) {
                newRfTop = mRFTop
                newRfBottom = mRFBottom
            } else {
                newRfTop = mRFTop * Source.percentRFTop
                newRfBottom = mRFBottom * PixelGraph.adjustRfBottom(mRFTop - mRFBottom)
            }


            Log.e("CON_ID", id)

            Log.e("ThisIsNotAnErrorAAA", "Top : $newRfTop Bottom : $newRfBottom RF : $mRF")
            val shadedRegion = ArrayList<Entry>()
            for (entry in dataVal) {
                val x = entry.x
                val y = entry.y
                //                if (x >= mRFTop && x <= mRFBottom) {
                if (x in newRfBottom..newRfTop) {
                    // Add the points within the range
                    Log.e("ThisIsNotAnErrorBBB", "Top : $x Bottom : $y RF : $mRF")

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
        binding.chart.description.isEnabled = false
        binding.chart.legend.isEnabled = false
        binding.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.chart.xAxis.setDrawGridLines(false)
        binding.chart.axisLeft.setDrawGridLines(false)
        binding.chart.axisRight.isEnabled = false
        binding.chart.clear()
        binding.chart.data = lineData
        binding.chart.invalidate()


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
            println("Before sort")
            println(
                rFvsAreaArrayList[i].rf.toString() + " , " + rFvsAreaArrayList[i]
                    .area
            )
        }

        // Sort the list by the rf value in ascending order
        rFvsAreaArrayList.sortWith(Comparator.comparingDouble<RFvsArea> { obj: RFvsArea -> obj.rf })

        var doubleArray = DoubleArray(rFvsAreaArrayList.size)

        for (i in rFvsAreaArrayList.indices) {
            println("After sort")

            doubleArray[i] = rFvsAreaArrayList[i].area

//            println(
//                rFvsAreaArrayList[i].rf.toString() + " , " + rFvsAreaArrayList[i]
//                    .area
//            )
        }

        val smoothData = smoothData(doubleArray, 15)

        rFvsAreaArrayList.clear()

        for (i in smoothData.indices) {
            rFvsAreaArrayList.add(RFvsArea(i.toDouble(), smoothData[i]))
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

    }

    override fun newOnClick(position: Int) {
    }

}