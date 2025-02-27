package com.aican.tlcanalyzer

import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.adapterClasses.ContourIntGraphAdapter
import com.aican.tlcanalyzer.adapterClasses.MultiSplitAdapter
import com.aican.tlcanalyzer.dataClasses.AnalMultiIntModel
import com.aican.tlcanalyzer.dataClasses.AreaWithContourID
import com.aican.tlcanalyzer.dataClasses.ContourData
import com.aican.tlcanalyzer.dataClasses.ContourGraphSelModel
import com.aican.tlcanalyzer.dataClasses.HrVsAreaPer
import com.aican.tlcanalyzer.dataClasses.RFvsArea
import com.aican.tlcanalyzer.dataClasses.SplitContourData
import com.aican.tlcanalyzer.databinding.ActivityPlotMultipleIntensityBinding
import com.aican.tlcanalyzer.interfaces.OnClicksListeners
import com.aican.tlcanalyzer.utils.RandomColors
import com.aican.tlcanalyzer.utils.Source
import com.aican.tlcanalyzer.utils.Source.splitContourDataList
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceGray
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.DottedBorder
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.property.TextAlignment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random


class PlotMultipleIntensity : AppCompatActivity(), OnClicksListeners {

    lateinit var binding: ActivityPlotMultipleIntensityBinding
    lateinit var adapter: MultiSplitAdapter
    lateinit var id: String
    lateinit var projectName: String
    lateinit var intensityLineChart: LineChart
    lateinit var splitContourData: ArrayList<SplitContourData>
    lateinit var splitContourDatas: ArrayList<SplitContourData>
    private val splitContourDataList2 = ArrayList<SplitContourData>()

//    lateinit var splitContourDataList: ArrayList<SplitContourData>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlotMultipleIntensityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.projectName.setText(intent.getStringExtra("projectName").toString())

        binding.back.setOnClickListener(View.OnClickListener { finish() })
        contoursAreaArrayList = java.util.ArrayList()

        id = intent.getStringExtra("id").toString()
        projectName = intent.getStringExtra("projectName").toString()
//        Source.toast(this@PlotMultipleIntensity, intent.getStringExtra("tableName"))

        val exportDir = File(getExternalFilesDir(null).toString() + "/" + "All PDF Files")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        splitContourDataList = Source.splitContourDataList

        binding.analyseInt.setOnClickListener {
            if (splitContourDataList.size <= 0) {
                Toast.makeText(
                    this@PlotMultipleIntensity,
                    "Please select the images before analysis",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                val i = Intent(this@PlotMultipleIntensity, AnalyseMultipleIntensity::class.java)
                i.putExtra("w", "split")
                i.putExtra("img_path", intent.getStringExtra("img_path").toString())
                i.putExtra("projectName", intent.getStringExtra("projectName").toString())
                i.putExtra(
                    "projectDescription",
                    intent.getStringExtra("projectDescription").toString()
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
            }
        }

        setRecView()

        splitContourData = ArrayList(splitContourDataList)
        splitContourDatas = ArrayList()

        showAllCon()


        val splitContData = ArrayList<SplitContourData>(splitContourDataList)

        binding.checkAnalysis.setOnClickListener {
            if (splitContData == null || splitContData.isEmpty()) {
                Source.toast(this, "No data available")
            } else {

//                DialogMain.showLoading(this@PlotMultipleIntensity, false, false, "Generating PDF...")

                println("Graph Data : " + splitContourDatas.joinToString())

                plotROIGraphs2(splitContourDatas)

                generateReport()

                println("Graph Data After PDF : " + splitContourDatas.joinToString())

//                startActivity(Intent(this@PlotMultipleIntensity, PlotMultiIntGraph::class.java))
            }
        }

        binding.hrVsAreaPer.setOnClickListener {
            // Clear previous values
            Source.hrVsAreaPerArrayListRM = ArrayList()
            Source.hrVsAreaPerArrayListFinal = ArrayList()

            val hrVsAreaPerArrayRM = ArrayList<HrVsAreaPer>()
            val hrVsAreaPerArrayFinal = ArrayList<HrVsAreaPer>()

            // ✅ 1. Check if splitContourDataList is empty
            if (splitContourDataList.isEmpty()) {
                Toast.makeText(this, "Please select images before analysis", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val splitContourDataListCopy = ArrayList(splitContourDataList)

            var isValid = false // ❌ Default to false to prevent navigation if no valid data is found

            for (split in splitContourDataListCopy) {
                if (!split.isSelected) continue  // ✅ Skip unselected items early

                val lengthh = split.name.length
                if (lengthh <= 14) continue

                var totalArea = 0f
                var rmArea = 0f
                var finalArea = 0f

                // ✅ 2. Check if split.contourData is empty
                if (split.contourData.isEmpty()) {
                    Toast.makeText(this, "Error: No contour data for ${split.name}", Toast.LENGTH_SHORT).show()
                    continue
                }

                // ✅ 3. Calculate totalArea
                for (s in split.contourData) {
                    totalArea += s.area.toFloat()
                }

                // ✅ 4. Extract rmArea and finalArea
                for (s in split.contourData) {
                    if (s.id == split.rmSpot) rmArea = s.area.toFloat()
                    if (s.id == split.finalSpot) finalArea = s.area.toFloat()
                }

                // ✅ 5. Check if totalArea is zero (to avoid division by zero)
                if (totalArea == 0f) {
                    Toast.makeText(this, "Error: Total Area is zero for ${split.name}", Toast.LENGTH_SHORT).show()
                    continue
                }

                // ✅ 6. Check if rmArea or finalArea is missing
                if (rmArea == 0f) {
                    Toast.makeText(this, "Error: RM Area is missing for ${split.name}", Toast.LENGTH_SHORT).show()
                    continue
                }

                if (finalArea == 0f) {
                    Toast.makeText(this, "Error: Final Area is missing for ${split.name}", Toast.LENGTH_SHORT).show()
                    continue
                }

                // ✅ 7. Calculate percentages
                val rmAreaPercent = (rmArea / totalArea) * 100
                val finalPercent = (finalArea / totalArea) * 100

                Log.d("CalculationCheck", "Split: ${split.name}, HR: ${split.hr}")
                Log.d("CalculationCheck", "Total Area: $totalArea, RM Area: $rmArea, Final Area: $finalArea")
                Log.d("CalculationCheck", "RM Area %: $rmAreaPercent, Final %: $finalPercent")

                hrVsAreaPerArrayRM.add(HrVsAreaPer(split.hr.toFloat(), rmAreaPercent))
                hrVsAreaPerArrayFinal.add(HrVsAreaPer(split.hr.toFloat(), finalPercent))

                isValid = true // ✅ Mark as valid if at least one split is processed successfully
            }

            // ✅ 8. Ensure at least one valid entry exists before proceeding
            if (!isValid || hrVsAreaPerArrayRM.isEmpty() || hrVsAreaPerArrayFinal.isEmpty()) {
                Toast.makeText(this, "Error: No valid selection or missing data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 9. If everything is valid, update Source and move to the next activity
            Source.hrVsAreaPerArrayListRM.addAll(hrVsAreaPerArrayRM)
            Source.hrVsAreaPerArrayListFinal.addAll(hrVsAreaPerArrayFinal)

            val intentt = Intent(this, HrVsAreaPerGraph::class.java)
            startActivity(intentt)
        }


    }

    private fun showChart(iLineDataSets: ArrayList<ILineDataSet>) {

        lineData = LineData(iLineDataSets)
        intensityLineChart.clear()
        intensityLineChart.data = lineData
        intensityLineChart.invalidate()
    }

    var lineData: LineData? = null
    var lineDataSetArrayList: ArrayList<LineDataSet>? = null


    private fun plotROIGraphs2(splitContourDataList: ArrayList<SplitContourData>) {


        // intensity plot start
        intensityLineChart = binding.intensityChartPlot1

        lineDataSetArrayList = ArrayList()
        val dataSets = ArrayList<ILineDataSet>()
        for (i in splitContourDataList.indices) {
            val multiSplitIntensity = splitContourDataList[i]

            val rFvsAreaArrayList = multiSplitIntensity.getrFvsAreaArrayList()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                rFvsAreaArrayList.sortWith(Comparator.comparingDouble { obj: RFvsArea -> obj.rf })
            }


            val informationROI = ArrayList<Entry>()
            for (j in rFvsAreaArrayList.indices) {

                val scaledYValue: Float = rFvsAreaArrayList[j].area.toString()
                    .toFloat() / Source.SCALING_FACTOR_INT_GRAPH // Adjust the scaling factor as needed
                // Adjust the scaling factor as needed

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
            }

            informationROI.reverse()

            lineDataSetArrayList!!.add(LineDataSet(informationROI, multiSplitIntensity.name))

//            lineDataSetArrayList!![i].mode = LineDataSet.Mode.HORIZONTAL_BEZIER

            lineDataSetArrayList!![i].color = setColor(i)

            lineDataSetArrayList!![i].lineWidth = 1f

            if (multiSplitIntensity.isSelected) {
                dataSets.add(lineDataSetArrayList!![i])

                lineDataSetArrayList!![i].setDrawCircles(false)
                lineDataSetArrayList!![i].setDrawFilled(false)

                val contourDataArray = multiSplitIntensity.contourData


                for (s in contourDataArray.indices) {
                    val ids = contourDataArray[s].id

                    val mRFTop = (String.format(
                        "%.0f",
                        contourDataArray[s].rfTop.toFloat() * Source.PARTS_INTENSITY
                    ).toInt()).toFloat()
                    val mRFBottom = (String.format(
                        "%.0f",
                        contourDataArray[s].rfBottom.toFloat() * Source.PARTS_INTENSITY
                    ).toInt()).toFloat()
                    val mRF = (String.format(
                        "%.0f",
                        contourDataArray[s].rf.toFloat() * Source.PARTS_INTENSITY
                    ).toInt()).toFloat()


                    var newRfTop: Float = mRFTop * Source.percentRFTop
//            val newRfTop = mRFTop;
                    //            val newRfTop = mRFTop;
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

                    Log.d("ButtonColor", contourDataArray[s].buttonColor.toString())

                    shadedDataSet.color = contourDataArray[s].buttonColor
                    shadedDataSet.setDrawFilled(true)
                    shadedDataSet.setDrawCircles(false)
                    shadedDataSet.fillColor = contourDataArray[s].buttonColor
                    shadedDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                    shadedDataSet.lineWidth = 0f

                    if (contourDataArray[s].isSelected) {
                        dataSets.add(shadedDataSet)
                    }
                }

            }
            // Add all shaded datasets to iLineDataSets

        }
        showChart(dataSets)
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

    }

    private fun getMaxValue(values: java.util.ArrayList<Double>): Double {
        var max = Double.MIN_VALUE
        for (value in values) {
            if (value > max) {
                max = value
            }
        }
        return max
    }

    fun generateReport() {
        val dir =
            File(
                ContextWrapper(this).externalMediaDirs[0],
                resources.getString(R.string.app_name) + id
            )
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
        val currentDateandTime = sdf.format(Date())
        val file = File(
            getExternalFilesDir(null).toString() + File.separator +
                    "All PDF Files/REPORT_" + projectName + "_" + id + "_" + currentDateandTime +
                    ".pdf"
        )

        val outputStream: OutputStream = FileOutputStream(file)
        val writer = PdfWriter(file)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        var company_name = "N/A"
        var user_name = "N/A"


        try {
            document.add(Paragraph(""))
            document.add(
                Paragraph(
                    "Order No. : $currentDateandTime"
                )
            )
            document.add(Paragraph("Company Name : $company_name\n Username : $user_name"))

            document.add(Paragraph(""))
            val dottedLine = Paragraph()
                .setBorder(
                    DottedBorder(
                        1f
                    )
                ) // Color and width of the border
                .setMarginTop(5f) // Distance between text and line
                .setMarginBottom(5f) // Distance between line and next element


            val textColor = DeviceRgb(255f, 0f, 0f)
            val fontSize = 16f
            val coloredText: Text = Text("Report's PDF")
                .setFontColor(textColor)
                .setFontSize(fontSize)

            document.add(Paragraph(coloredText).setTextAlignment(TextAlignment.CENTER))

            document.add(dottedLine)
            document.add(Paragraph(""))
//            document.add(Chunk(DottedLineSeparator()))

            val chunk1 = Paragraph(
                Text("Report of $projectName")
                    .setFontColor(DeviceGray.BLACK)
                    .setFontSize(24f)
//                    .setFont(PdfFontFactory.createFont(FontConstants.TIMES_ROMAN))
            )
            document.add(chunk1)
//            document.add(Chunk(lineSeparator))
            document.add(Paragraph(" "))
            document.add(
                Paragraph(
                    Text(
                        "Split Image Data"
                    )
                        .setFontSize(20f)
//                        .setFont(PdfFontFactory.createFont(FontConstants.TIMES_BOLD))
                ).setTextAlignment(TextAlignment.LEFT)
            )
//            barChart = findViewById<BarChart>(R.id.volumeChart)

            val splitContourDataList = ArrayList<SplitContourData>(
                splitContourDataList
            )



            for ((i, splits) in splitContourDataList.withIndex()) {
                val split = splits

                if (split.isSelected && split.name.equals("Main Image")) {
                    val volData = ArrayList<Double>()
                    val contourDataArray = ArrayList<ContourData>()

                    for (vol in split.contourData) {
                        volData.add(vol.volume.toDouble())
                    }

                    Source.toast(
                        this@PlotMultipleIntensity, volData.size.toString() + ""
                    );
                    settingVolumeData(volData, split.contourData)
                    setAllDatas(split.getrFvsAreaArrayList(), split.contourData)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showContoursList(split.contourData)
                    }

                    document.add(Paragraph("\n"))
                    val paragraph1 =
                        Paragraph(
                            Text("Split Image Data").setFontSize(20f)
//                                .setFont(PdfFontFactory.createFont(FontConstants.TIMES_ROMAN))
                        ).setTextAlignment(TextAlignment.CENTER)

                    paragraph1.setTextAlignment(TextAlignment.CENTER)
//                    document.add(paragraph1)
                    document.add(Paragraph("\n"))

                    document.add(
                        Paragraph(
                            Text(
                                "Master Image Data"
                            )
                        )
                    )
//                    document.add(Chunk(LineSeparator()))
                    document.add(Paragraph("\n"))
                    document.add(
                        Paragraph(
                            Text(
                                "Main Image"
                            )
                        )
                    )

                    document.add(Paragraph(" "))
                    val stream = ByteArrayOutputStream()

                    val outFile = File(dir, split.mainImageName)
                    var bitmap: Bitmap? = null
                    if (outFile.exists()) {
//                        Source.toast(this@PlotMultipleIntensity, "exist")

                        bitmap = BitmapFactory.decodeFile(outFile.path)
                    } else {
//                        Source.toast(this@PlotMultipleIntensity, "not exist")
                    }
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val byteArray = stream.toByteArray()
                    val imageData = ImageDataFactory.create(byteArray)
                    val image = Image(imageData)
                    image.scaleToFit(495f, 350f)
                    document.add(image)
                    //
                    document.add(Paragraph("\n"))

                    document.add(
                        Paragraph(
                            Text(
                                "Spot Image"
                            )
                        )
                    )

                    document.add(Paragraph(" "))
                    val stream1 = ByteArrayOutputStream()

                    val outFile1 = File(dir, split.contourImageName)
                    var bitmap1: Bitmap? = null
                    if (outFile1.exists()) {
//                        Source.toast(this@PlotMultipleIntensity, "exist")

                        bitmap1 = BitmapFactory.decodeFile(outFile1.path)
                    } else {
//                        Source.toast(this@PlotMultipleIntensity, "not exist")
                    }
                    bitmap1?.compress(Bitmap.CompressFormat.JPEG, 100, stream1)
                    val byteArray1 = stream1.toByteArray()
                    val imageData2 = ImageDataFactory.create(byteArray1)
                    val image1 = Image(imageData2)
                    image1.scaleToFit(495f, 350f)
                    document.add(image1)
                    //
                    document.add(Paragraph("\n"))

                    document.add(Paragraph(" "))

//                    document.add(Chunk(LineSeparator()))
                    document.add(Paragraph(" "))
                    // table
                    // Create a table with 3 columns
                    // table
                    // Create a table with 3 columns
                    document.add(
                        Paragraph(
                            Text(
                                "Spot Table"
                            )
                        )
                    )

                    val columnWidth = floatArrayOf(200f, 210f, 190f, 170f, 170f, 240f, 340f)

                    val table = Table(columnWidth)

                    var totalArea = 0f

                    for (i in split.contourData) {
                        totalArea += i.getArea().toFloat()
                    }

                    table.addCell("Id")
                    table.addCell("Rf")
                    table.addCell("Cv")
                    table.addCell("Area")
                    table.addCell("% Area")
                    table.addCell("Volume")
                    table.addCell("Label")

                    for ((k, iData) in split.contourData.withIndex()) {
                        table.addCell(iData.id)
                        table.addCell(iData.rf)
                        table.addCell(String.format("%.2f", iData.cv.toDouble()))
                        table.addCell(iData.area)
                        table.addCell(
                            String.format(
                                "%.2f",
                                iData.getArea()
                                    .toFloat() / totalArea * 100
                            ) + " %"
                        )
                        table.addCell(iData.volume)
                        table.addCell(split.labelDataArrayList[k].label)
                    }
//2512
                    document.add(table)

                    document.add(Paragraph("\n"))


                    // volume plot start

                    document.add(Paragraph(" "))

//                    document.add(Chunk(LineSeparator()))
                    document.add(Paragraph(" "))


                    document.add(
                        Paragraph(
                            Text(
                                "Volume Plot"
                            )
                        )
                    )

                    val chartBitmap2 = barChart.chartBitmap

//            Bitmap scaledBitmap = Bitmap.createScaledBitmap(chartBitmap, chartBitmap.getWidth() / 2, chartBitmap.getHeight() / 2, false);


//            Bitmap scaledBitmap = Bitmap.createScaledBitmap(chartBitmap, chartBitmap.getWidth() / 2, chartBitmap.getHeight() / 2, false);
                    val stream2 = ByteArrayOutputStream()
                    chartBitmap2.compress(Bitmap.CompressFormat.JPEG, 100, stream2)
                    val byteArray2 = stream2.toByteArray()
                    val imagData = ImageDataFactory.create(byteArray2)
                    val image2 = Image(imagData)
                    image2.scaleToFit(495f, 350f)
                    document.add(image2)

                    // volume plot end


                    // int plot start

                    document.add(Paragraph(" "))

//                    document.add(Chunk(LineSeparator()))
                    document.add(Paragraph(" "))



                    document.add(
                        Paragraph(
                            Text(
                                "Intensity Plot"
                            )
                        )
                    )

                    val chartBitmap3 = chart1.chartBitmap

                    val stream3 = ByteArrayOutputStream()
                    chartBitmap3.compress(Bitmap.CompressFormat.JPEG, 100, stream3)
                    val byteArray3 = stream3.toByteArray()
                    val imgData = ImageDataFactory.create(byteArray3)
                    val image3 = Image(imgData)
                    image3.scaleToFit(495f, 350f)
                    document.add(image3)


                    val bitmapk = getRecyclerViewBitmap(binding.contourListRecView)
                    val streamk = ByteArrayOutputStream()
                    bitmapk.compress(Bitmap.CompressFormat.JPEG, 100, streamk)
                    val byteArrayk = streamk.toByteArray()
                    val imageDatak = ImageDataFactory.create(byteArrayk)
                    val imagek = Image(imageDatak)
                    imagek.scaleToFit(495f, 350f)
                    document.add(imagek)


                    // int plot end


                } else {
//                    Source.toast(this@PlotMultipleIntensity, "not existhh")

                }
            }


            for ((i, splits) in splitContourDataList.withIndex()) {
                val split = splits
                if (split.isSelected && !split.name.equals("Main Image")) {

                    val volData = ArrayList<Double>()

                    for (vol in split.contourData) {
                        volData.add(vol.volume.toDouble())
                    }

                    Source.toast(
                        this@PlotMultipleIntensity, volData.size.toString() + ""
                    )
                    settingVolumeData(volData, split.contourData)
                    setAllDatas(split.getrFvsAreaArrayList(), split.contourData)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showContoursList(split.contourData)
                    }

                    document.add(Paragraph("\n"))

                    //temp

                    document.add(Paragraph("\n"))
                    document.add(Paragraph("\n"))


                    //            document.add(new Chunk(new DottedLineSeparator()));


                    document.add(dottedLine)

                    document.add(
                        Paragraph(
                            Text(
                                split.name + " - Image Data"
                            ).setFontSize(20f)
//                                .setFont(PdfFontFactory.createFont(FontConstants.COURIER_BOLD))
                        ).setTextAlignment(TextAlignment.CENTER)
                    )

//                    document.add(paragraph1)
//                    document.add(Chunk(LineSeparator()))
                    document.add(Paragraph("\n"))
                    document.add(
                        Paragraph(
                            Text(
                                split.name + " - Main Image"
                            )
                        )
                    )

                    document.add(Paragraph(" "))
                    val stream = ByteArrayOutputStream()

                    val outFile = File(dir, split.mainImageName)
                    var bitmap: Bitmap? = null
                    if (outFile.exists()) {
//                        Source.toast(this@PlotMultipleIntensity, "exist")

                        bitmap = BitmapFactory.decodeFile(outFile.path)
                    } else {
//                        Source.toast(this@PlotMultipleIntensity, "not exist")
                    }
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val byteArray = stream.toByteArray()
                    val iData = ImageDataFactory.create(byteArray)
                    val image = Image(iData)
                    image.scaleToFit(595f, 200f)
                    document.add(image)
                    //
                    document.add(Paragraph("\n"))

                    document.add(
                        Paragraph(
                            Text(
                                split.name + " - Spot Image"
                            )
                        )
                    )

                    document.add(Paragraph(" "))
                    val stream1 = ByteArrayOutputStream()

                    val outFile1 = File(dir, split.contourImageName)
                    var bitmap1: Bitmap? = null
                    if (outFile1.exists()) {
//                        Source.toast(this@PlotMultipleIntensity, "exist")

                        bitmap1 = BitmapFactory.decodeFile(outFile1.path)
                    } else {
//                        Source.toast(this@PlotMultipleIntensity, "not exist")
                    }
                    bitmap1?.compress(Bitmap.CompressFormat.JPEG, 100, stream1)
                    val byteArray1 = stream1.toByteArray()
                    val imData = ImageDataFactory.create(byteArray1)
                    val image1 = Image(imData)
                    image1.scaleToFit(595f, 200f)
                    document.add(image1)
                    //
                    document.add(Paragraph("\n"))

                    document.add(Paragraph(" "))

//                    document.add(Chunk(LineSeparator()))
                    document.add(Paragraph(" "))
                    // table
                    // Create a table with 3 columns

                    document.add(
                        Paragraph(
                            Text(
                                split.name + " - Spot Table"
                            )
                        )
                    )
                    val columnWidth = floatArrayOf(200f, 210f, 190f, 170f, 170f, 240f, 340f)

                    val table = Table(columnWidth)

                    var totalArea = 0f

//                    for (i in split.contourData) {
//                        totalArea += i.getArea().toFloat()
//                    }

                    table.addCell("Id")
                    table.addCell("Rf")
                    table.addCell("Cv")
                    table.addCell("Area")
                    table.addCell("% Area")
                    table.addCell("Volume")
                    table.addCell("Label")

                    if (split.contourData.size == contoursAreaArrayList.size) {

                        for (i in contoursAreaArrayList) {
                            totalArea += i.area.toFloat()
                        }


                        for ((i, contourData) in split.contourData.withIndex()) {
                            table.addCell(contourData.id)
                            table.addCell(contourData.rf)
                            table.addCell(String.format("%.2f", contourData.cv.toDouble()))
                            table.addCell(contoursAreaArrayList[i].area.toString())
                            table.addCell(
                                String.format(
                                    "%.2f",
                                    (contoursAreaArrayList[i].area
                                        .toFloat() / totalArea) * 100
                                ) + " %"
                            )
                            table.addCell(contourData.volume)
                            table.addCell(split.labelDataArrayList[i].label)
                        }
                    } else {

                        for (i in split.contourData) {
                            totalArea += i.getArea().toFloat()
                        }

                        for (i in split.contourData) {
                            table.addCell(i.id)
                            table.addCell(i.rf)
                            table.addCell(String.format("%.2f", i.cv.toDouble()))
                            table.addCell("null")
                            table.addCell(
                                "null"
                            )
                            table.addCell(i.volume)
                        }
                    }

//                    for (i in split.contourData) {
//                        table.addCell(i.id)
//                        table.addCell(i.rf)
//                        table.addCell(String.format("%.2f", i.cv.toDouble()))
//                        table.addCell(i.area)
//                        table.addCell(
//                            String.format(
//                                "%.2f",
//                                i.area
//                                    .toFloat() / totalArea * 100
//                            ) + " %"
//                        )
//                        table.addCell(i.volume)
//                    }
//2512
                    document.add(table)

                    document.add(Paragraph("\n"))

                    // volume plot start

                    document.add(Paragraph(" "))

//                    document.add(Chunk(LineSeparator()))
                    document.add(Paragraph(" "))


                    document.add(
                        Paragraph(
                            Text(
                                split.name + " - Volume Plot"
                            )
                        )
                    )

                    val chartBitmap2 = barChart.chartBitmap
                    val stream2 = ByteArrayOutputStream()
                    chartBitmap2.compress(Bitmap.CompressFormat.JPEG, 100, stream2)
                    val byteArray2 = stream2.toByteArray()
                    val iiiData = ImageDataFactory.create(byteArray2)
                    val image2 = Image(iiiData)
                    image2.scaleToFit(495f, 350f)
                    document.add(image2)

                    // volume plot end

                    // int plot start

                    document.add(Paragraph(" "))

//                    document.add(Chunk(LineSeparator()))
                    document.add(Paragraph(" "))


                    document.add(
                        Paragraph(
                            Text(
                                split.name + " - Intensity Plot"
                            )
                        )
                    )
                    val chartBitmap3 = chart1.chartBitmap

                    val stream3 = ByteArrayOutputStream()
                    chartBitmap3.compress(Bitmap.CompressFormat.JPEG, 100, stream3)
                    val byteArray3 = stream3.toByteArray()
                    val iDatas = ImageDataFactory.create(byteArray3)
                    val image3 = Image(iDatas)
                    image3.scaleToFit(495f, 350f)
                    document.add(image3)

                    val bitmapk = getRecyclerViewBitmap(binding.contourListRecView)
                    val streamk = ByteArrayOutputStream()
                    bitmapk.compress(Bitmap.CompressFormat.JPEG, 100, streamk)
                    val byteArrayk = streamk.toByteArray()
                    val imageDatak = ImageDataFactory.create(byteArrayk)
                    val imagek = Image(imageDatak)
                    imagek.scaleToFit(495f, 350f)
                    document.add(imagek)


                    // int plot end


                } else {
//                    Source.toast(this@PlotMultipleIntensity, "not existhh")

                }
            }

            document.add(dottedLine)

            document.add(
                Paragraph(
                    Text(
                        "All Intensity Plot"
                    )
                )
            )

            val chartBitmap2 = intensityLineChart.chartBitmap
            val stream2 = ByteArrayOutputStream()
            chartBitmap2.compress(Bitmap.CompressFormat.JPEG, 100, stream2)
            val byteArray2 = stream2.toByteArray()
            val iisData = ImageDataFactory.create(byteArray2)
            val image2 = Image(iisData)
            image2.scaleToFit(495f, 350f)
            document.add(image2)

            // volume plot end

            // int plot start

            document.add(Paragraph(" "))

//            document.add(Chunk(LineSeparator()))
            document.add(Paragraph(" "))


            document.close()
            val path = Uri.parse(file.path)
            startActivity(
                Intent(this@PlotMultipleIntensity, PDFActivity::class.java)
                    .putExtra("path", path.toString())
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRecyclerViewBitmap(recyclerView: RecyclerView): Bitmap {
        val adapter = recyclerView.adapter

        // Make sure the adapter is set and the RecyclerView has items
        if (adapter != null && adapter.itemCount > 0) {
            // Measure and layout the RecyclerView
            recyclerView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            recyclerView.layout(0, 0, recyclerView.measuredWidth, recyclerView.measuredHeight)

            // Create a bitmap of the RecyclerView
            val bitmap = Bitmap.createBitmap(
                recyclerView.width,
                recyclerView.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            recyclerView.draw(canvas)

            return bitmap
        }

        // Return null if there are no items in the RecyclerView
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }


    lateinit var barChart: BarChart
    lateinit var barDataSet: BarDataSet
    lateinit var barData: BarData

    private fun settingVolumeData(
        volumeDATA: ArrayList<Double>,
        contourDataArray: ArrayList<ContourData>
    ) {

        // initializing variable for bar chart.
        barChart = findViewById<BarChart>(R.id.volumeChart)

        // calling method to get bar entries.
        getBarEntries(volumeDATA, contourDataArray)

        // creating a new bar data set.
        barDataSet = BarDataSet(barEntriesArrayList, "Volume Graph")

        // creating a new bar data and
        // passing our bar data set.
        barData = BarData(barDataSet)


        barChart.axisLeft.axisMinimum = 0f

        val maxValue: Double =
            VolumeGraph.getMaxValue(volumeDATA) // Function to get the maximum value


        barChart.axisLeft.axisMaximum = maxValue.toFloat() // Adjust the maximum value as needed

        // below line is to set data
        // to our bar chart.
        barChart.setData(barData)

        // adding color to our bar data set.
        barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)

        // setting text color.
        barDataSet.setValueTextColor(Color.BLACK)

        barDataSet.valueFormatter = object : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry): String {
                return barEntry.data.toString()
            }
        }


        // setting text size
        barDataSet.valueTextSize = 16f
        barChart.description.isEnabled = false
    }

    lateinit var barEntriesArrayList: ArrayList<BarEntry>

    private fun getBarEntries(
        volumeDATA: ArrayList<Double>,
        contourDataArray: ArrayList<ContourData>
    ) {
        // creating a new array list
        barEntriesArrayList = ArrayList()
        barEntriesArrayList.clear()

        var i = 0
        for (d in volumeDATA) {
            val id = contourDataArray.get(i).id


            i++
            val element = BarEntry(i.toFloat(), d.toString().toFloat())
            element.data = id
            barEntriesArrayList.add(element)
        }
    }


    private fun setRecView() {
        adapter = MultiSplitAdapter(this, splitContourDataList, this)
        binding.recView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    lateinit var chart1: LineChart
    var lineDataSet1 = LineDataSet(null, null)
    var iLineDataSets1 = ArrayList<ILineDataSet>()
    lateinit var lineData1: LineData

    private fun setAllDatas(
        rFvsAreaArrayList: ArrayList<RFvsArea>,
        contourData: ArrayList<ContourData>
    ) {
        chart1 = findViewById(R.id.intensityChart)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getDataFromIntent(rFvsAreaArrayList)
        }
        //        setupChart();
        showData1(rFvsAreaArrayList, contourData)
    }

    lateinit var contourDataArrayListNew: ArrayList<ContourData>

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showContoursList(contourDataArrayListNews: ArrayList<ContourData>) {

        contourIntGraphAdapter =
            ContourIntGraphAdapter(this, contourDataArrayListNews, 0, this, true, false, false)
        binding.contourListRecView.adapter = contourIntGraphAdapter
        contourIntGraphAdapter.notifyDataSetChanged()
    }

    lateinit var contourIntGraphAdapter: ContourIntGraphAdapter


    private fun showData1(
        rFvsAreaArrayList: ArrayList<RFvsArea>,
        contourData: ArrayList<ContourData>
    ) {
        val information = java.util.ArrayList<Entry>()
        rFvsAreaArrayList.reverse()
        for (i in rFvsAreaArrayList.indices) {

            val scaledYValue: Float = rFvsAreaArrayList[i].area.toString()
                .toFloat() / Source.SCALING_FACTOR_INT_GRAPH // Adjust the scaling factor as needed

//            information.add(
//                Entry(
//                    Source.PARTS_INTENSITY - rFvsAreaArrayList.get(i).getRf().toString().toFloat(),
//                    scaledYValue
//                )
//            )

            information.add(
                Entry(
                    Source.PARTS_INTENSITY - rFvsAreaArrayList.get(i).getRf().toString().toFloat(),
                    rFvsAreaArrayList.get(i).area.toString()
                        .toFloat()
                )
            )
        }

        contourDataArrayListNew = java.util.ArrayList()


        contourDataArrayListNew = contourData
//        val mContDataList = contourData


        // reversing

//        showChart5(information, 1000, 3000, contourDataArrayListNew);
        val contourGraphSelModelArrayList = java.util.ArrayList<ContourGraphSelModel>()

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
        minYValue = Float.MAX_VALUE // Initialize with a very large value


        for (entry in rFvsAreaArrayList) {
            if (entry.area < minYValue) {
                minYValue = entry.area.toFloat()
            }
        }


//        showChart1(information)
        showChart5Reverse(information, contourGraphSelModelArrayList)
    }

    var minYValue = 0.0f
    lateinit var contoursAreaArrayList: java.util.ArrayList<AreaWithContourID>


    private fun showChart5Reverse(
        dataVal: java.util.ArrayList<Entry>,
        contourDataArray: java.util.ArrayList<ContourGraphSelModel>
    ) {

        contoursAreaArrayList.clear()

        iLineDataSets1.clear()
        lineDataSet1.setValues(dataVal)
        lineDataSet1.setDrawCircles(false)
        lineDataSet1.setColor(getColor(R.color.purple_200))
        lineDataSet1.setLineWidth(1f)


        // Disable filling for the entire curve
        lineDataSet1.setDrawFilled(false)
        iLineDataSets1.add(lineDataSet1)

        // Create an ArrayList to hold all shaded regions
        val shadedDataSets = java.util.ArrayList<LineDataSet>()
        for (i in contourDataArray.indices) {
//        for (int i = contourDataArray.size() - 1; i >= 0; i--) {
            val mRFTop =
                String.format("%.0f", contourDataArray[i].rfTop.toFloat() * Source.PARTS_INTENSITY)
                    .toInt()
                    .toFloat()
            val mRFBottom =
                String.format(
                    "%.0f",
                    contourDataArray[i].rfBottom.toFloat() * Source.PARTS_INTENSITY
                ).toInt()
                    .toFloat()
            val mRF =
                String.format("%.0f", contourDataArray[i].rf.toFloat() * Source.PARTS_INTENSITY)
                    .toInt()
                    .toFloat()

            var newRfTop: Float = mRFTop * Source.percentRFTop
//            val newRfTop = mRFTop;
            //            val newRfTop = mRFTop;
            var newRfBottom: Float = mRFBottom * Source.percentRFBottom

            val id = contourDataArray[i].id
            if (id.contains("m")) {
                newRfTop = mRFTop
                newRfBottom = mRFBottom
            } else {
                newRfTop = mRFTop * Source.percentRFTop
                newRfBottom = mRFBottom * PixelGraph.adjustRfBottom(mRFTop - mRFBottom)
            }
            newRfTop = mRFTop
            newRfBottom = mRFBottom

//            val newRfBottom = mRFBottom;
//            val newRfBottom = mRFBottom;
            var int1 = dataVal[0].y
            var int2 = 0.0f
            Log.e("ThisIsNotAnError", "Top : $newRfTop Bottom : $newRfBottom RF : $mRF")
            val shadedRegion = java.util.ArrayList<Entry>()
            for (entry in dataVal) {
                val x = entry.x
                val y = entry.y
                //                if (x >= mRFTop && x <= mRFBottom) {
                if (x in newRfBottom..newRfTop) {
                    // Add the points within the range
                    shadedRegion.add(Entry(x, y))
                }
                if (id.contains("m")) {
                    if (x == newRfBottom) {
                        int1 = y
                    }
                    if (x == newRfTop) {
                        int2 = y
                    }
                } else {
                    if (x == mRFBottom) {
                        int1 = y
                    }
                    if (x == mRFTop) {
                        int2 = y
                    }
                }
            }

            Log.e("Int12", "$int1, $int2")

            val peakArea =
                PixelGraph.calculateAreaUnderCurveNew(shadedRegion) * 100 / Source.PARTS_INTENSITY
//            val lowerRectangleArea = PixelGraph.calculateAreaRectangleNew(
//                minYValue,
//                shadedRegion
//            ) * 100 / Source.PARTS_INTENSITY
//            double lowerRectangleArea = calculateRectangleArea((int1 + int2) / 2.0f, shadedRegion) * 100 / Source.PARTS_INTENSITY;

            val lowerRectangleArea = PixelGraph.calculateAreaRectangleNew(
                (int1 + int2) / 2.0f,
                shadedRegion
            ) * 100 / Source.PARTS_INTENSITY


            //            double lowerRectangleArea = calculateRectangleArea((int1 + int2) / 2.0f, shadedRegion) * 100 / Source.PARTS_INTENSITY;
            Log.e("AreaUnderCurve124", peakArea.toString() + "")
            Log.e("AreaUnderCurveRectangle", lowerRectangleArea.toString() + "")

            val finalArea = peakArea - lowerRectangleArea

            contoursAreaArrayList.add(AreaWithContourID(id, finalArea))

            Log.e("FinalArea", "$finalArea, $id")

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
            slopePoints2.add(Entry(mRFBottom, int1))
            slopePoints2.add(Entry(mRFTop, int2))

            val baselineDataSet = LineDataSet(slopePoints2, "Baseline")
            baselineDataSet.setDrawCircles(false)
            baselineDataSet.color = Color.BLACK // Set the color for the baseline

            baselineDataSet.lineWidth = 1f // Set the line width for the baseline

//            if (id.contains("m")) {

            shadedDataSets.add(baselineDataSet)
//            }
            // Add the shaded dataset to the list of shaded datasets
            shadedDataSets.add(shadedDataSet)
        }

        // Add all shaded datasets to iLineDataSets
        iLineDataSets1.addAll(shadedDataSets)
        lineData1 = LineData(iLineDataSets1)
        chart1.getDescription().setEnabled(false)
        chart1.getLegend().setEnabled(false)
        chart1.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM)
//        chart1.getXAxis().setDrawGridLines(false)
//        chart1.getAxisLeft().setDrawGridLines(false)
//        chart1.getAxisRight().setEnabled(false)
        chart1.clear()
        chart1.setData(lineData1)
        chart1.invalidate()
    }


//    lateinit var rFvsAreaArrayList1: ArrayList<RFvsArea>

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun getDataFromIntent(rFvsAreaArrayList: ArrayList<RFvsArea>) {

        for ((i, rf) in rFvsAreaArrayList.withIndex()) {
//            println("Before sort")
//            println(
//                rFvsAreaArrayList.get(i).getRf().toString() + " , " + rFvsAreaArrayList.get(i)
//                    .getArea()
//            )
        }

        var j = 0
        // Sort the list by the rf value in ascending order
        rFvsAreaArrayList.sortWith(Comparator.comparingDouble { obj: RFvsArea -> obj.rf })
        for (rf in rFvsAreaArrayList) {
//            println("After sort")
//            println(
//                rFvsAreaArrayList.get(j).getRf().toString() + " , " + rFvsAreaArrayList.get(j)
//                    .getArea()
//            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.intensityChartPlot1.visibility = View.INVISIBLE
        binding.intensityChart.visibility = View.INVISIBLE
        binding.volumeChart.visibility = View.INVISIBLE
        setRecView()
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

    }

    override fun newOnClick(position: Int) {
        val i = Intent(this@PlotMultipleIntensity, PlotMultipleIntensity::class.java)
        i.putExtra("w", "split")
        i.putExtra("img_path", intent.getStringExtra("img_path"))
        i.putExtra("projectName", intent.getStringExtra("projectName"))
        i.putExtra("projectDescription", intent.getStringExtra("projectDescription"))
        i.putExtra("projectImage", intent.getStringExtra("projectImage"))
        i.putExtra("projectNumber", intent.getStringExtra("projectNumber"))
        i.putExtra("imageName", intent.getStringExtra("imageName"))
        i.putExtra("splitId", intent.getStringExtra("splitId"))
        i.putExtra("tableName", intent.getStringExtra("tableName"))
        i.putExtra("roiTableID", intent.getStringExtra("roiTableID"))
        i.putExtra("thresholdVal", intent.getStringExtra("thresholdVal"))
        i.putExtra("numberOfSpots", intent.getStringExtra("numberOfSpots"))
        i.putExtra("id", intent.getStringExtra("id"))
        i.putExtra("pid", intent.getStringExtra("pid"))
        i.putExtra(
            "volumePlotTableID", intent.getStringExtra("volumePlotTableID"),
        )
        i.putExtra(
            "intensityPlotTableID", intent.getStringExtra("intensityPlotTableID"),
        )
        i.putExtra(
            "plotTableID", intent.getStringExtra("plotTableID"),
        )
        startActivity(i)
        finish()
        overridePendingTransition(0, 0)

    }


}