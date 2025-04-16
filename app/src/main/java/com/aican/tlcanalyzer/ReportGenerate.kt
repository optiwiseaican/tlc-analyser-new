package com.aican.tlcanalyzer

import android.content.ContextWrapper
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.adapterClasses.ContourIntGraphAdapter
import com.aican.tlcanalyzer.customClasses.LegacyTableView
import com.aican.tlcanalyzer.dataClasses.AreaWithContourID
import com.aican.tlcanalyzer.dataClasses.ContourData
import com.aican.tlcanalyzer.dataClasses.ContourGraphSelModel
import com.aican.tlcanalyzer.dataClasses.ContourSet
import com.aican.tlcanalyzer.dataClasses.LabelData
import com.aican.tlcanalyzer.dataClasses.RFvsArea
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.database.UsersDatabase
import com.aican.tlcanalyzer.databinding.ActivityReportGenerateBinding
import com.aican.tlcanalyzer.dialog.AuthDialog
import com.aican.tlcanalyzer.dialog.LoadingDialog
import com.aican.tlcanalyzer.interfaces.OnClicksListeners
import com.aican.tlcanalyzer.utils.Source
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ReportGenerate : AppCompatActivity(), OnClicksListeners {

    lateinit var chartROI: LineChart
    lateinit var barChartROI: BarChart
    var iLineDataSets1 = ArrayList<ILineDataSet>()
    var lineDataSet1 = LineDataSet(null, null)


    private lateinit var binding: ActivityReportGenerateBinding
    private lateinit var chart1: LineChart
    private lateinit var mode1: String
    private lateinit var lineData1: LineData
    private lateinit var intensities1: ArrayList<Double>
    private lateinit var rFvsAreaArrayList1: ArrayList<RFvsArea>
    private lateinit var contourSetArrayList1: ArrayList<ContourSet>
    private lateinit var barChart: BarChart
    private lateinit var barData: BarData
    private lateinit var barDataSet: BarDataSet
    private lateinit var barEntriesArrayList: ArrayList<BarEntry>
    private lateinit var legacyTableView: LegacyTableView
    private lateinit var legacyTableViewROI: LegacyTableView
    private lateinit var id: String
    private lateinit var projectName: String
    private lateinit var projectImage: String
    private lateinit var back: ImageView
    private lateinit var dir: File
    lateinit var usersDatabase: UsersDatabase
    lateinit var labelDataArrayList: java.util.ArrayList<LabelData>
    lateinit var databaseHelper: DatabaseHelper
    lateinit var plotTableID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportGenerateBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        contoursAreaArrayList = ArrayList()
        back = findViewById(R.id.back)
        binding!!.selectRoi.visibility = View.GONE
        supportActionBar!!.hide()
        back.setOnClickListener(View.OnClickListener { finish() })
        id = intent.getStringExtra("id").toString()
        projectName = intent.getStringExtra("projectName").toString()
        plotTableID = intent.getStringExtra("plotTableID").toString()
        legacyTableView = binding!!.legacyTableView
        legacyTableViewROI = binding!!.legacyTableViewRoi
        binding!!.originalImage.setImageBitmap(Source.originalBitmap)
        databaseHelper = DatabaseHelper(this@ReportGenerate)

        insertLabelData()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showContoursList()
        }


        setAllDatas()
        settingVolumeData()

        dir = Source.getSplitFolderFile(
            this,
            intent.getStringExtra("projectName"),
            intent.getStringExtra("id")
        )



        checkBoxClickListeners()
        val exportDir = File(getExternalFilesDir(null).toString() + "/" + s())
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        if (intent.getStringExtra("i") == "withRoi") {
            binding!!.selectRoi.visibility = View.VISIBLE
            binding!!.selectRoi.setOnClickListener {
                if (binding!!.selectRoi.isChecked) {
                    binding!!.roiPanel.visibility = View.VISIBLE
                } else {
                    binding!!.roiPanel.visibility = View.GONE
                }
            }
            binding!!.originalImageRoi.setImageBitmap(Source.originalBitmapROI)
            binding!!.capturedImageRoi.setImageBitmap(Source.contourBitmapROI)
            plotTableROI()
            plotROIGraphs()
        }

        usersDatabase = UsersDatabase(this)



        binding!!.generatePDFReport.setOnClickListener {
            if (binding!!.selectRoi.isChecked || binding!!.selectOriginalImg.isChecked || binding!!.selectVolPlot.isChecked || binding!!.selectIntPlot.isChecked || binding!!.selectContImg.isChecked || binding!!.selectContTable.isChecked) {

                CoroutineScope(Dispatchers.Main).launch {
                    // Show the progress dialog
                    LoadingDialog.showLoading(
                        this@ReportGenerate, false, false, "Generating PDF..."
                    )

                    try {
                        // Perform the PDF generation in the background
                        val generatedFile = withContext(Dispatchers.IO) {

                            usersDatabase.logUserAction(
                                AuthDialog.activeUserName,
                                AuthDialog.activeUserRole,
                                "Report Generated",
                                intent.getStringExtra("projectName").toString(),
                                intent.getStringExtra("id").toString(),
                                AuthDialog.projectType
                            )


                            generatePDF(dir)
                        }

                        // Handle the generated PDF file
                        handleGeneratedPDF(generatedFile)
                    } catch (e: Exception) {
                        // Handle any exceptions that occurred during PDF generation
                        showError(e.message)
                    } finally {
                        // Dismiss the progress dialog
                        LoadingDialog.cancelLoading()
                    }
                }

            } else {
                Toast.makeText(this@ReportGenerate, "Select ast least one task", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        val contourImage = intent.getStringExtra("contourImage").toString()
//        dir = File(
//            ContextWrapper(this).externalMediaDirs[0], resources.getString(R.string.app_name) + id
//        )


        val outFile = File(dir, contourImage)
        if (outFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)
            Source.contourBitmap = myBitmap
            binding!!.capturedImage.setImageBitmap(Source.contourBitmap)


//            captured_image.setImageBitmap(myBitmap
        } else {
            Source.toast(this, "Contour image not available")
        }

        barDataSet.valueFormatter = object : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry): String {
                return barEntry.data.toString()
            }
        }


        plotTable()

    }

    private fun s() = "All PDF Files"

    private fun insertLabelData() {
        labelDataArrayList = java.util.ArrayList()
        val cursor: Cursor = databaseHelper.getDataFromTable("LABEL_$plotTableID")
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    labelDataArrayList.add(LabelData(cursor.getString(0), cursor.getString(1)))
                } while (cursor.moveToNext())
            }
        }
    }

    private fun handleGeneratedPDF(path: Uri) {


        // Handle the generated PDF file here
        // For example, display it in a PDF viewer activity
        startActivity(Intent(this, PDFActivity::class.java).apply {
            putExtra("path", path.toString())
        })
    }

    private fun showError(message: String?) {
        // Display an error message to the user
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }

    private fun plotROIGraphs() {
        var barEntriesArrayListROI: ArrayList<BarEntry> = ArrayList()

        // volume graph plot start
        barChartROI = findViewById(R.id.volumeChart_roi)
        barEntriesArrayListROI = ArrayList()
        for (i in Source.volumeDATAROI.indices) {
            barEntriesArrayListROI.add(
                BarEntry(
                    i.toFloat(), Source.volumeDATAROI[i].toString().toFloat()
                )
            )
        }
        val barDataSetROI = BarDataSet(barEntriesArrayListROI, "Volume Graph")
        val barDataROI = BarData(barDataSetROI)
        barChartROI.setData(barDataROI)
        barDataSetROI.setColors(*ColorTemplate.MATERIAL_COLORS)
        barDataSetROI.valueTextColor = Color.BLACK
        barDataSetROI.valueTextSize = 16f
        barChartROI.getDescription().isEnabled = false

        // volume graph plot end

        // intensity plot start
        chartROI = findViewById(R.id.intensityChart_roi)
        val lineDataSetROI = LineDataSet(null, null)
        val iLineDataSetsROI = ArrayList<ILineDataSet>()
        val lineDataROI: LineData
        var intensitiesROI: ArrayList<Double?>
        var rFvsAreaArrayListROI: ArrayList<RFvsArea?>
        var contourSetArrayListROI: ArrayList<ContourSet?>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Source.rFvsAreaArrayListROI.sortWith(Comparator.comparingDouble { obj: RFvsArea -> obj.rf })
        }
        val informationROI = ArrayList<Entry>()
        for (i in Source.rFvsAreaArrayListROI.indices) {
            informationROI.add(
                Entry(
                    Source.rFvsAreaArrayListROI[i].rf.toString().toFloat(),
                    Source.rFvsAreaArrayListROI[i].area.toString().toFloat()
                )
            )
        }
        lineDataSetROI.values = informationROI
        lineDataSetROI.setDrawCircles(false)
        lineDataSetROI.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lineDataSetROI.color = Color.RED
        iLineDataSetsROI.add(lineDataSetROI)
        lineDataROI = LineData(iLineDataSetsROI)
        //        chart.getAxisLeft().setInverted(true);
        chartROI.getDescription().isEnabled = false
        chartROI.getLegend().isEnabled = false
        chartROI.getXAxis().position = XAxis.XAxisPosition.BOTTOM
        chartROI.getXAxis().setDrawGridLines(false)
        chartROI.getAxisLeft().setDrawGridLines(false)
        chartROI.getAxisRight().isEnabled = false
        chartROI.clear()
        chartROI.setData(lineDataROI)
        chartROI.invalidate()

        // intensity plot end
    }

    private fun generatePDF(pdfDir: File): Uri {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
        val currentDateandTime = sdf.format(Date())

        // Ensure the directory exists
        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }

        val file = File(
            pdfDir,
            "REPORT_${projectName}_${id}_$currentDateandTime.pdf"
        )

        Log.e("FileNameErrors", file.path)

        val outputStream: OutputStream = FileOutputStream(file)
        val writer = PdfWriter(file)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        val company_name = "N/A"
        val user_name = "N/A"


        try {
            val dottedLine = Paragraph().setBorder(
                DottedBorder(
                    1f
                )
            ) // Color and width of the border
                .setMarginTop(5f) // Distance between text and line
                .setMarginBottom(5f) // Distance between line and next element

            document.add(Paragraph(""))
            document.add(
                Paragraph(
                    "Order No. : $currentDateandTime"
                )
            )
            document.add(Paragraph("Company Name : " + company_name + "\n Username : " + user_name))

            document.add(Paragraph(""))

            val textColor = DeviceRgb(255f, 0f, 0f)
            val fontSize = 16f
            val coloredText: Text =
                Text("Report's PDF").setFontColor(textColor).setFontSize(fontSize)

            document.add(Paragraph(coloredText).setTextAlignment(TextAlignment.CENTER))

            document.add(dottedLine)
            document.add(Paragraph(" "))

            val chunk1 = Paragraph(
                Text("Report of $projectName").setFontColor(DeviceGray.BLACK).setFontSize(24f)
//                    .setFont(PdfFontFactory.createFont(FontConstants.TIMES_ROMAN))

//                    .setFont(StandardFonts.HELVETICA)
            ).setTextAlignment(TextAlignment.CENTER)

            document.add(chunk1)
            document.add(Paragraph(" "))
            document.add(Paragraph(" "))
            if (binding!!.selectRoi.isChecked) {
//                document.addNewPage()

//image
                val paragraph1 = Paragraph(
                    Text("Region of Interest Data")
//                        .setFont(StandardFonts.TIMES_BOLD)
                )
                paragraph1.setTextAlignment(TextAlignment.CENTER)
                document.add(paragraph1)
                document.add(Paragraph("\n"))
                document.add(
                    Paragraph(
                        Text(
                            "Original Image"
                        )
//                            .setFont(StandardFonts.TIMES_BOLD)
                    )
                )
                document.add(Paragraph(" "))
                val stream = ByteArrayOutputStream()
                Source.originalBitmapROI.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()
                val imageData = ImageDataFactory.create(byteArray)
                val image = Image(imageData)
                image.scaleToFit(595f, 500f)
//                Source.toast(this@ReportGenerate, image.imageHeight.toString())
                document.add(image)
                //
                document.add(Paragraph())


//image


                document.add(
                    Paragraph(
                        Text("Detected Spot Image")
//                        .setFont(StandardFonts.TIMES_BOLD)
                    ).setTextAlignment(TextAlignment.CENTER)

                )
                document.add(Paragraph(" "))
                val stream80 = ByteArrayOutputStream()
                Source.contourBitmapROI.compress(Bitmap.CompressFormat.JPEG, 100, stream80)
                val byteArray80 = stream80.toByteArray()
                val imageData80 = ImageDataFactory.create(byteArray80)
                val image80 = Image(imageData80)
                image80.scaleToFit(595f, 500f)
                document.add(image80)
                document.add(Paragraph("\n"))

//
                document.add(Paragraph(" "))

                document.add(Paragraph(" "))
                // table
                // Create a table with 3 columns

                document.add(
                    Paragraph(
                        Text("Spot Table")
//                        .setFont(StandardFonts.TIMES_BOLD)
                    ).setTextAlignment(TextAlignment.CENTER)

                )

                var columnWidth = floatArrayOf(200f, 210f, 190f, 170f, 170f)
                if (Source.SHOW_VOLUME_DATA)
                    columnWidth = floatArrayOf(200f, 210f, 190f, 170f, 170f, 240f, 340f)
                if (Source.SHOW_LABEL_DATA)
                    columnWidth = floatArrayOf(200f, 210f, 190f, 170f, 170f, 340f)

                val table = Table(columnWidth)
                table.addCell("Id")
                table.addCell("Rf")
                table.addCell("Cv")
                table.addCell("Area")
                table.addCell("% Area")
                if (Source.SHOW_VOLUME_DATA)
                    table.addCell("Volume")
                if (Source.SHOW_LABEL_DATA)
                    table.addCell("Label")

                var totalArea = 0f

                if (contoursAreaArrayList.size == Source.contourDataArrayList.size) {
                    for (i in Source.contourDataArrayList.indices) {
                        totalArea += contoursAreaArrayList.get(i).area.toFloat()
                    }

                    for (i in Source.contourDataArrayListROI.indices) {
                        val contourData = Source.contourDataArrayListROI[i]
                        table.addCell(contourData.id)
                        table.addCell(contourData.rf)
                        table.addCell(String.format("%.2f", contourData.cv.toDouble()))
                        table.addCell(Source.formatToTwoDecimalPlaces(contoursAreaArrayList.get(i).area.toString()))
                        table.addCell(
                            String.format(
                                "%.2f",
                                (contoursAreaArrayList.get(i).area.toFloat() / totalArea) * Source.PARTS_INTENSITY
                            ) + " %"
                        )
                        if (Source.SHOW_VOLUME_DATA)
                            table.addCell(contourData.volume)
                        if (Source.SHOW_LABEL_DATA)
                            table.addCell(
                                labelDataArrayList.get(i).label
                            )
                    }
                } else {
                    for (i in Source.contourDataArrayList.indices) {
                        totalArea += Source.contourDataArrayList.get(i).getArea().toFloat()
                    }

                    for (i in Source.contourDataArrayListROI.indices) {
                        val contourData = Source.contourDataArrayListROI[i]
                        table.addCell(contourData.id)
                        table.addCell(contourData.rf)
                        table.addCell(String.format("%.2f", contourData.cv.toDouble()))
                        table.addCell("null")
                        table.addCell(
                            "null"
                        )
                        if (Source.SHOW_VOLUME_DATA)
                            table.addCell(contourData.volume)
                        if (Source.SHOW_LABEL_DATA)
                            table.addCell(
                                labelDataArrayList.get(i).label
                            )
                    }
                }

//                for (i in Source.contourDataArrayList.indices) {
//                    totalArea += Source.contourDataArrayList.get(i).getArea().toFloat()
//                }
//
//                for (i in Source.contourDataArrayListROI.indices) {
//                    val contourData = Source.contourDataArrayListROI[i]
//                    table.addCell(contourData.id)
//                    table.addCell(contourData.rf)
//                    table.addCell(String.format("%.2f", contourData.cv.toDouble()))
//                    table.addCell(contourData.area)
//                    table.addCell(
//                        String.format(
//                            "%.2f",
//                            Source.contourDataArrayList.get(i).getArea()
//                                .toFloat() / totalArea * Source.PARTS_INTENSITY
//                        ) + " %"
//                    )
//                    table.addCell(contourData.volume)
//                }
                document.add(table)
                document.add(Paragraph("\n"))
                document.add(Paragraph(" "))
                document.add(Paragraph(" "))

                document.add(
                    Paragraph(
                        Text("Intensity Plot")
//                        .setFont(StandardFonts.TIMES_BOLD)
                    ).setTextAlignment(TextAlignment.CENTER)

                )

                //intensity plot
                val chartBitmap1 = chartROI!!.chartBitmap


                val stream1 = ByteArrayOutputStream()
                chartBitmap1.compress(Bitmap.CompressFormat.JPEG, 100, stream1)
                val byteArray1 = stream1.toByteArray()
                val imageData1 = ImageDataFactory.create(byteArray1)
                val image1 = Image(imageData1)
                image1.scaleToFit(595f, 500f)
                document.add(image1)
                document.add(Paragraph("\n"))

                // volume plot
                document.add(Paragraph(" "))
                document.add(Paragraph("\n"))
                document.add(Paragraph(" "))

                document.add(
                    Paragraph(
                        Text("Volume Plot")
//                        .setFont(StandardFonts.TIMES_BOLD)
                    ).setTextAlignment(TextAlignment.CENTER)

                )

                val chartBitmap25 = barChartROI!!.chartBitmap

//            Bitmap scaledBitmap = Bitmap.createScaledBitmap(chartBitmap, chartBitmap.getWidth() / 2, chartBitmap.getHeight() / 2, false);
                val stream25 = ByteArrayOutputStream()
                chartBitmap25.compress(Bitmap.CompressFormat.JPEG, 100, stream25)
                val byteArray25 = stream25.toByteArray()
                val imageData25 = ImageDataFactory.create(byteArray25)
                val image25 = Image(imageData25)
                image25.scaleToFit(595f, 500f)
                document.add(image25)
                //                document.newPage();
                document.add(Paragraph("\n"))
                //                document.add(new Chunk(new LineSeparator()));
            }
            document.add(Paragraph("\n"))


            if (binding!!.selectOriginalImg.isChecked) {
//image
//                document.add(Paragraph("\n"))
//                document.add(Paragraph("\n"))
                document.add(
                    Paragraph(
                        Text(
                            "Original Image"
                        )
//                            .setFont(StandardFonts.TIMES_BOLD)
                    )
                )
//                document.add(Paragraph("\n"))
                val stream = ByteArrayOutputStream()
                Source.originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()
                val imageData = ImageDataFactory.create(byteArray)
                val image = Image(imageData)
                image.scaleToFit(595f, 200f)
                document.add(image)
                //
            }
            document.add(Paragraph("\n"))

//            document.add(Paragraph("\n"))
            if (binding!!.selectContImg.isChecked) {
//image
//                document.add(Paragraph("\n"))
                document.add(
                    Paragraph(
                        Text(
                            "Detected Spot Image"
                        )
//                            .setFont(StandardFonts.TIMES_BOLD)
                    )
                )

                val stream = ByteArrayOutputStream()
                Source.contourBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()
                val imageData = ImageDataFactory.create(byteArray)
                val image = Image(imageData)
                image.scaleToFit(595f, 200f)
                document.add(image)
                //
            }
            document.add(Paragraph("\n"))
            if (binding!!.selectContTable.isChecked) {
                document.add(Paragraph(" "))
//                document.add(Chunk(LineSeparator()))
                document.add(Paragraph(" "))
                // table
                // Create a table with 3 columns

                document.add(
                    Paragraph(
                        Text(
                            "Spot Table"
                        )
//                            .setFont(StandardFonts.TIMES_BOLD)
                    )
                )

                var columnWidth = floatArrayOf(200f, 210f, 190f, 170f, 170f)
                if (Source.SHOW_VOLUME_DATA)
                    columnWidth = floatArrayOf(200f, 210f, 190f, 170f, 170f, 240f, 340f)
                if (Source.SHOW_LABEL_DATA)
                    columnWidth = floatArrayOf(200f, 210f, 190f, 170f, 170f, 240f, 340f)

                var totalArea = 0f

//                for (i in Source.contourDataArrayList.indices) {
//                    totalArea += Source.contourDataArrayList.get(i).getArea().toFloat()
//                }


                val table = Table(columnWidth)
                table.addCell("Id")
                table.addCell("Rf")
                table.addCell("Cv")
                table.addCell("Area")
                table.addCell("% Area")
                if (Source.SHOW_VOLUME_DATA)
                    table.addCell("Volume")
                if (Source.SHOW_LABEL_DATA)
                    table.addCell("Label")

                if (contoursAreaArrayList.size == Source.contourDataArrayList.size) {
                    for (i in contoursAreaArrayList.indices) {
                        totalArea += contoursAreaArrayList.get(i).area.toFloat()
                    }

                    for (i in Source.contourDataArrayList.indices) {
                        val contourData = Source.contourDataArrayList[i]
                        table.addCell(contourData.id)
                        table.addCell(contourData.rf)
                        table.addCell(String.format("%.2f", contourData.cv.toDouble()))
                        table.addCell(Source.formatToTwoDecimalPlaces(contoursAreaArrayList.get(i).area.toString()))
                        table.addCell(
                            String.format(
                                "%.2f",
                                (contoursAreaArrayList.get(i).area.toFloat() / totalArea) * 100
                            ) + " %"
                        )
                        if (Source.SHOW_VOLUME_DATA)
                            table.addCell(contourData.volume)
                        if (Source.SHOW_LABEL_DATA)
                            table.addCell(
                                labelDataArrayList.get(i).label
                            )
                    }
                } else {
                    for (i in Source.contourDataArrayList.indices) {
                        totalArea += Source.contourDataArrayList.get(i).getArea().toFloat()
                    }

                    for (i in Source.contourDataArrayList.indices) {
                        val contourData = Source.contourDataArrayList[i]
                        table.addCell(contourData.id)
                        table.addCell(contourData.rf)
                        table.addCell(String.format("%.2f", contourData.cv.toDouble()))
                        table.addCell("null")
                        table.addCell(
                            "null"
                        )
                        if (Source.SHOW_VOLUME_DATA)
                            table.addCell(contourData.volume)
                        if (Source.SHOW_LABEL_DATA)
                            table.addCell(
                                labelDataArrayList.get(i).label
                            )
                    }
                }

//                for (i in Source.contourDataArrayList.indices) {
//                    val contourData = Source.contourDataArrayList[i]
//                    table.addCell(contourData.id)
//                    table.addCell(contourData.rf)
//                    table.addCell(String.format("%.2f", contourData.cv.toDouble()))
//                    table.addCell(contourData.area)
//                    table.addCell(
//                        String.format(
//                            "%.2f",
//                            Source.contourDataArrayList.get(i).getArea()
//                                .toFloat() / totalArea * Source.PARTS_INTENSITY
//                        ) + " %"
//                    )
//                    table.addCell(contourData.volume)
//                }
                document.add(table)
            }
            document.add(Paragraph("\n"))
            if (binding!!.selectIntPlot.isChecked) {
                document.add(Paragraph(" "))
//                document.add(Chunk(LineSeparator()))
                document.add(Paragraph(" "))
                document.add(
                    Paragraph(
                        Text(
                            "Intensity Plot"
                        )
//                            .setFont(StandardFonts.TIMES_BOLD)
                    )
                )
                //intensity plot
                val chartBitmap1 = chart1.chartBitmap

//            Bitmap scaledBitmap1 = Bitmap.createScaledBitmap(chartBitmap1, chartBitmap1.getWidth() / 2, chartBitmap1.getHeight() / 2, false);
                val stream1 = ByteArrayOutputStream()
                chartBitmap1.compress(Bitmap.CompressFormat.JPEG, 100, stream1)
                val byteArray1 = stream1.toByteArray()
                val imageData1 = ImageDataFactory.create(byteArray1)
                val image1 = Image(imageData1)
                image1.scaleToFit(495f, 350f)
                document.add(image1)

                val bitmap = getRecyclerViewBitmap(binding.contourListRecView)
                val stream2 = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream2)
                val byteArray2 = stream2.toByteArray()
                val imageData2 = ImageDataFactory.create(byteArray2)
                val image2 = Image(imageData2)
                image2.scaleToFit(495f, 350f)
                document.add(image2)


            }
            document.add(Paragraph("\n"))
            if (binding!!.selectVolPlot.isChecked) {
                document.add(Paragraph(" "))
//                document.add(Chunk(LineSeparator()))
                document.add(Paragraph(" "))
                document.add(
                    Paragraph(
                        Text(
                            "Volume Plot"
                        )
//                            .setFont(StandardFonts.TIMES_BOLD)
                    )
                )
                val chartBitmap2 = barChart.chartBitmap

//              Bitmap scaledBitmap = Bitmap.createScaledBitmap(chartBitmap, chartBitmap.getWidth() / 2, chartBitmap.getHeight() / 2, false);
                val stream2 = ByteArrayOutputStream()
                chartBitmap2.compress(Bitmap.CompressFormat.JPEG, 100, stream2)
                val byteArray2 = stream2.toByteArray()
                val imageData2 = ImageDataFactory.create(byteArray2)
                val image2 = Image(imageData2)
                image2.scaleToFit(595f, 350f)
                document.add(image2)
            }
            document.add(Paragraph("\n"))
            document.close()
            val path = Uri.parse(file.path)
//            DialogMain.cancelLoading()
//            startActivity(
//                Intent(this@ReportGenerate, PDFActivity::class.java)
//                    .putExtra("path", path.toString())
//            )
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    this@ReportGenerate, "Error : " + e.message, Toast.LENGTH_SHORT
                ).show()
            }
            e.printStackTrace()
        }
        val path = Uri.parse(file.path)

        return path
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
                recyclerView.width, recyclerView.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            recyclerView.draw(canvas)

            return bitmap
        }

        // Return null if there are no items in the RecyclerView
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }


    private fun plotTableROI() {
        var totalArea = 0f

        // Determine which columns to include
        val showVolume = Source.SHOW_VOLUME_DATA
        val showLabel = Source.SHOW_LABEL_DATA

        // Define table headers dynamically based on conditions
        val headers = mutableListOf("ID", "Rf", "Cv", "Area")
        if (showVolume) headers.add("Volume")
        if (showLabel) headers.add("Label")

        LegacyTableView.insertLegacyTitle(*headers.toTypedArray())

        // Calculate total area for percentage area calculations
        for (i in Source.contourDataArrayListROI.indices) {
            totalArea += Source.contourDataArrayListROI[i].area.toFloat()
        }

        for (i in Source.contourDataArrayListROI.indices) {
            val rowData = mutableListOf(
                Source.contourDataArrayListROI[i].id,
                Source.contourDataArrayListROI[i].rf,
                (1 / Source.contourDataArrayListROI[i].rf.toFloat()).toString(),
                Source.formatToTwoDecimalPlaces(Source.contourDataArrayListROI[i].area)
            )

            if (showVolume) rowData.add(Source.contourDataArrayListROI[i].volume)
            if (showLabel) rowData.add(labelDataArrayList[i].label)

            LegacyTableView.insertLegacyContent(*rowData.toTypedArray())
        }

        // Apply table formatting and styles
        legacyTableView.setTheme(LegacyTableView.CUSTOM)
        legacyTableView.setContent(LegacyTableView.readLegacyContent())
        legacyTableView.setTitle(LegacyTableView.readLegacyTitle())

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

    private fun plotTable() {
        var totalArea = 0f

        // Determine which columns to include
        val showVolume = Source.SHOW_VOLUME_DATA
        val showLabel = Source.SHOW_LABEL_DATA

        // Define table headers dynamically based on conditions
        val headers = mutableListOf("ID", "Rf", "Cv", "Area", "% Area")
        if (showVolume) headers.add("Volume")
        if (showLabel) headers.add("Label")

        LegacyTableView.insertLegacyTitle(*headers.toTypedArray())

        // Ensure we have correct sizes for the contour data and area lists
        if (Source.contourDataArrayList.size == contoursAreaArrayList.size) {
            for (i in Source.contourDataArrayList.indices) {
                totalArea += contoursAreaArrayList[i].area.toFloat()
            }

            for (i in Source.contourDataArrayList.indices) {
                val rowData = mutableListOf(
                    Source.contourDataArrayList[i].id,
                    Source.contourDataArrayList[i].rf,
                    (1 / Source.contourDataArrayList[i].rf.toFloat()).toString(),
                    Source.formatToTwoDecimalPlaces(contoursAreaArrayList[i].area.toString()),
                    String.format(
                        "%.2f",
                        contoursAreaArrayList[i].area.toFloat() / totalArea * 100
                    ) + " %"
                )

                if (showVolume) rowData.add(Source.contourDataArrayList[i].volume)
                if (showLabel) rowData.add(labelDataArrayList[i].label)

                LegacyTableView.insertLegacyContent(*rowData.toTypedArray())
            }
        } else {
            for (i in Source.contourDataArrayList.indices) {
                totalArea += Source.contourDataArrayList[i].getArea().toFloat()
            }

            for (i in Source.contourDataArrayList.indices) {
                val rowData = mutableListOf(
                    Source.contourDataArrayList[i].id,
                    Source.contourDataArrayList[i].rf,
                    (1 / Source.contourDataArrayList[i].rf.toFloat()).toString(),
                    "null",
                    "null"
                )

                if (showLabel) rowData.add(labelDataArrayList[i].label)

                LegacyTableView.insertLegacyContent(*rowData.toTypedArray())
            }
        }

        // Table formatting and styling
        legacyTableView.setTheme(LegacyTableView.CUSTOM)
        legacyTableView.setContent(LegacyTableView.readLegacyContent())
        legacyTableView.setTitle(LegacyTableView.readLegacyTitle())

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

    private fun settingVolumeData() {

        // initializing variable for bar chart.
        barChart = findViewById(R.id.volumeChart)

        // calling method to get bar entries.
        barEntries

        // creating a new bar data set.
        barDataSet = BarDataSet(barEntriesArrayList, "Volume Graph")

        // creating a new bar data and
        // passing our bar data set.
        barData = BarData(barDataSet)

        barChart.axisLeft.axisMinimum = 0f

        val maxValue: Double =
            VolumeGraph.getMaxValue(Source.volumeDATA) // Function to get the maximum value


        barChart.axisLeft.axisMaximum = maxValue.toFloat() // Adjust the maximum value as needed


        // below line is to set data
        // to our bar chart.
        barChart.setData(barData)

        // adding color to our bar data set.
        barDataSet!!.setColors(*ColorTemplate.MATERIAL_COLORS)

        // setting text color.
        barDataSet!!.valueTextColor = Color.BLACK

        // setting text size
        barDataSet!!.valueTextSize = 16f
        barChart.getDescription().isEnabled = false
    }

    private val barEntries: Unit
        get() {
            // creating a new array list
            barEntriesArrayList = ArrayList()


            for (i in Source.volumeDATA.indices) {

                // Format the value as needed, e.g., with two decimal places
                val id = Source.contourDataArrayList[i].id


                val barEntry = BarEntry(
                    i.toFloat(), Source.volumeDATA[i].toString().toFloat()
                )


                barEntry.data = id

                barEntriesArrayList.add(
                    barEntry
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
        contourIntGraphAdapter = ContourIntGraphAdapter(
            true,
            this, contourDataArrayListNew, 0, this, false, false, false
        )
        binding.contourListRecView.adapter = contourIntGraphAdapter
        contourIntGraphAdapter.notifyDataSetChanged()
    }

    lateinit var contourIntGraphAdapter: ContourIntGraphAdapter


    private fun setAllDatas() {
        chart1 = findViewById(R.id.intensityChart)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getDataFromIntent()
        }
        //        setupChart();
        showData1()
    }

    lateinit var contourDataArrayListNew: ArrayList<ContourData>

    lateinit var contoursAreaArrayList: java.util.ArrayList<AreaWithContourID>


    private fun showData1() {
        val information = ArrayList<Entry>()


        rFvsAreaArrayList1.reverse()

        for (i in rFvsAreaArrayList1!!.indices) {

            val scaledYValue: Float = rFvsAreaArrayList1.get(i).getArea().toString()
                .toFloat() / Source.SCALING_FACTOR_INT_GRAPH // Adjust the scaling factor as needed


//            information.add(
//                Entry(
//                    Source.PARTS_INTENSITY - rFvsAreaArrayList1!![i].rf.toString().toFloat(),

//                    scaledYValue
//                )
//            )


            information.add(
                Entry(
                    Source.PARTS_INTENSITY - rFvsAreaArrayList1!![i].rf.toString().toFloat(),
                    rFvsAreaArrayList1.get(i).getArea().toString().toFloat()
                )
            )
        }


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

        // reversing


//        showChart5(information, 1000, 3000, contourDataArrayListNew);
        val contourGraphSelModelArrayList = java.util.ArrayList<ContourGraphSelModel>()

//        //normal
        for (i in contourDataArrayListNew.indices) {
            contourGraphSelModelArrayList.add(
                ContourGraphSelModel(
                    contourDataArrayListNew.get(i).getRfTop(),
                    contourDataArrayListNew.get(i).getRfBottom(),
                    contourDataArrayListNew.get(i).getRf(),
                    contourDataArrayListNew.get(i).id,
                    contourDataArrayListNew.get(i).getButtonColor()
                )
            )
        }

        ////
        minYValue = Float.MAX_VALUE // Initialize with a very large value


        for (entry in rFvsAreaArrayList1) {
            if (entry.area < minYValue) {
                minYValue = entry.area.toFloat()
            }
        }


        showChart5Reverse(information, contourGraphSelModelArrayList)
//        showChart1(information)
    }

    var minYValue = 0.0f


//    var iLineDataSets = java.util.ArrayList<ILineDataSet>()
//    var lineDataSet = LineDataSet(null, null)

    fun showChart5Reverse(
        dataVal: java.util.ArrayList<Entry>,
        contourDataArray: java.util.ArrayList<ContourGraphSelModel>
    ) {
        contoursAreaArrayList.clear()
        iLineDataSets1.clear()
        lineDataSet1.setValues(dataVal)
        lineDataSet1.setDrawCircles(false)
        lineDataSet1.setColor(getColor(R.color.purple_200))

        // Disable filling for the entire curve
        lineDataSet1.setDrawFilled(false)
        iLineDataSets1.add(lineDataSet1)

        // Create an ArrayList to hold all shaded regions
        val shadedDataSets = java.util.ArrayList<LineDataSet>()
        for (i in contourDataArray.indices) {
//        for (int i = contourDataArray.size() - 1; i >= 0; i--) {
            val mRFTop =
                String.format("%.0f", contourDataArray[i].rfTop.toFloat() * Source.PARTS_INTENSITY)
                    .toInt().toFloat()
            val mRFBottom = String.format(
                "%.0f", contourDataArray[i].rfBottom.toFloat() * Source.PARTS_INTENSITY
            ).toInt().toFloat()
            val mRF =
                String.format("%.0f", contourDataArray[i].rf.toFloat() * Source.PARTS_INTENSITY)
                    .toInt().toFloat()

            var newRfTop: Float = mRFTop * 1.1f
//            val newRfTop = mRFTop;
            //            val newRfTop = mRFTop;
            var newRfBottom: Float = mRFBottom * 0.9f
//            val newRfBottom = mRFBottom;

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

            val lowerRectangleArea = PixelGraph.calculateAreaRectangleNew(
                (int1 + int2) / 2.0f, shadedRegion
            ) * 100 / Source.PARTS_INTENSITY


            //            double lowerRectangleArea = calculateRectangleArea((int1 + int2) / 2.0f, shadedRegion) * 100 / Source.PARTS_INTENSITY;
            Log.e("AreaUnderCurve124", peakArea.toString() + "")
            Log.e("AreaUnderCurveRectangle", lowerRectangleArea.toString() + "")
            var finalArea = peakArea - lowerRectangleArea

            if (dataVal.size > Source.PARTS_INTENSITY) {
                finalArea = finalArea / 2
            }


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
        chart1.getLegend().isEnabled = false
        chart1.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM)
        chart1.xAxis.setDrawGridLines(true)
        chart1.axisLeft.setDrawGridLines(true)
        chart1.axisRight.setEnabled(false)
        chart1.clear()
        chart1.data = lineData1
        chart1.invalidate()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun getDataFromIntent() {
        rFvsAreaArrayList1 = ArrayList()
        val intent = intent
        mode1 = intent.getStringExtra(resources.getString(R.string.modeKey)).toString()
        //        b = intent.getByteArrayExtra(getResources().getString(R.string.pixelsArrayKey));
//        intensities1 = ArrayList()
//        intensities1 = Source.intensities

//        Toast.makeText(this@ReportGenerate, "" + Source.rFvsAreaArrayList.size, Toast.LENGTH_SHORT).show()

        rFvsAreaArrayList1 = Source.rFvsAreaArrayList
//        contourSetArrayList1 = Source.contourSetArrayList
        for (i in rFvsAreaArrayList1.indices) {
            println("Before sort")
            println(rFvsAreaArrayList1.get(i).rf.toString() + " , " + rFvsAreaArrayList1.get(i).area)
        }

        // Sort the list by the rf value in ascending order
        rFvsAreaArrayList1.sortWith(Comparator.comparingDouble { obj: RFvsArea -> obj.rf })
        for (i in rFvsAreaArrayList1.indices) {
            println("After sort")
            println(rFvsAreaArrayList1.get(i).rf.toString() + " , " + rFvsAreaArrayList1.get(i).area)
        }
    }

    private fun checkBoxClickListeners() {
        binding!!.capturedImage.visibility = View.GONE
        binding!!.contourListRecView.visibility = View.GONE
        binding!!.legacyTableView.visibility = View.GONE
        binding!!.intensityChart.visibility = View.GONE
        binding!!.volumeChart.visibility = View.GONE
        binding!!.originalImage.visibility = View.GONE
        binding!!.roiPanel.visibility = View.GONE
        binding!!.selectOriginalImg.setOnClickListener {
            if (binding!!.selectOriginalImg.isChecked) {
                binding!!.originalImage.visibility = View.VISIBLE
            } else {
                binding!!.originalImage.visibility = View.GONE
            }
        }
        binding!!.selectContImg.setOnClickListener {
            if (binding!!.selectContImg.isChecked) {
                binding!!.capturedImage.visibility = View.VISIBLE
            } else {
                binding!!.capturedImage.visibility = View.GONE
            }
        }
        binding!!.selectContTable.setOnClickListener {
            if (binding!!.selectContTable.isChecked) {
                binding!!.legacyTableView.visibility = View.VISIBLE
            } else {
                binding!!.legacyTableView.visibility = View.GONE
            }
        }
        binding!!.selectIntPlot.setOnClickListener {
            if (binding!!.selectIntPlot.isChecked) {
                binding!!.intensityChart.visibility = View.VISIBLE
                binding!!.contourListRecView.visibility = View.VISIBLE

            } else {
                binding!!.intensityChart.visibility = View.GONE
                binding!!.contourListRecView.visibility = View.GONE

            }
        }
        binding!!.selectVolPlot.setOnClickListener {
            if (binding!!.selectVolPlot.isChecked) {
                binding!!.volumeChart.visibility = View.VISIBLE
            } else {
                binding!!.volumeChart.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //        Source.showContourImg = true;
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