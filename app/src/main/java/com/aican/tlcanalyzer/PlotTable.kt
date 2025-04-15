package com.aican.tlcanalyzer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.customClasses.LegacyTableView
import com.aican.tlcanalyzer.database.UsersDatabase
import com.aican.tlcanalyzer.databinding.ActivityPlotTableBinding
import com.aican.tlcanalyzer.dialog.AuthDialog
import com.aican.tlcanalyzer.utils.Source
import com.aican.tlcanalyzer.utils.Source.contourDataArrayList

class PlotTable : AppCompatActivity() {

    lateinit var binding: ActivityPlotTableBinding
    lateinit var legacyTableView: LegacyTableView

    lateinit var usersDatabase: UsersDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlotTableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        legacyTableView = binding.legacyTableView

        supportActionBar?.hide()

        binding.back.setOnClickListener {
            finish()
        }


        usersDatabase = UsersDatabase(this)

        usersDatabase.logUserAction(
            AuthDialog.activeUserName,
            AuthDialog.activeUserRole,
            "Table Plot",
            intent.getStringExtra("projectName").toString(),
            intent.getStringExtra("id").toString(),
            AuthDialog.projectType
        )


        plotTable()

    }


    private fun plotTable() {
        if (Source.SHOW_VOLUME_DATA) {
            LegacyTableView.insertLegacyTitle("ID", "Rf", "Cv", "Area", "% area", "Volume")
            var totalArea = 0.0

            for (i in contourDataArrayList.indices) {

                totalArea += contourDataArrayList[i].area.toFloat()

            }

            for (i in contourDataArrayList.indices) {
                LegacyTableView.insertLegacyContent(
                    contourDataArrayList.get(i).getId(),
                    contourDataArrayList.get(i).getRf(),
                    (1 / Source.contourDataArrayList[i].rf.toFloat()).toString(),
                    Source.formatToTwoDecimalPlaces(contourDataArrayList.get(i).getArea()),
                    String.format(
                        "%.2f", ((contourDataArrayList[i].area.toFloat() / totalArea) * 100)
                    ) + " %",
                    contourDataArrayList.get(i).getVolume()
                )
            }
        }else{
            LegacyTableView.insertLegacyTitle("ID", "Rf", "Cv", "Area", "% area",)
            var totalArea = 0.0

            for (i in contourDataArrayList.indices) {

                totalArea += contourDataArrayList[i].area.toFloat()

            }

            for (i in contourDataArrayList.indices) {
                LegacyTableView.insertLegacyContent(
                    contourDataArrayList.get(i).getId(),
                    contourDataArrayList.get(i).getRf(),
                    (1 / Source.contourDataArrayList[i].rf.toFloat()).toString(),
                    Source.formatToTwoDecimalPlaces(contourDataArrayList.get(i).getArea()),
                    String.format(
                        "%.2f", ((contourDataArrayList[i].area.toFloat() / totalArea) * 100)
                    ) + " %",
                )
            }
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

    override fun onBackPressed() {
        super.onBackPressed()
//        Source.showContourImg = true
    }

}