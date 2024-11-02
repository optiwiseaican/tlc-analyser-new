package com.aican.tlcanalyzer

import android.content.ContextWrapper
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.adapterClasses.SplitAdapter
import com.aican.tlcanalyzer.dataClasses.SplitData
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.databinding.ActivitySelectImagesSplitBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectImagesSplit : AppCompatActivity() {


    lateinit var binding: ActivitySelectImagesSplitBinding
    lateinit var img_uri: Uri
    lateinit var id: String
    lateinit var databaseHelper: DatabaseHelper
    lateinit var arrayList: ArrayList<SplitData>
    lateinit var duplicateArrayList: ArrayList<SplitData>
    lateinit var adapter: SplitAdapter
    lateinit var work: String
    var works = arrayOf("new", "existing")
    lateinit var tableName: String
    lateinit var projectDescription: String
    lateinit var projectName: String
    lateinit var projectImage: String
    lateinit var roiTableID: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectImagesSplitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        databaseHelper = DatabaseHelper(this)
//        img_uri = Uri.parse(intent.getStringExtra("img_path"))
        projectDescription = intent.getStringExtra("projectDescription").toString()
        roiTableID = intent.getStringExtra("roiTableID").toString()
        id = intent.getStringExtra("id").toString()
        projectImage = intent.getStringExtra("projectImage").toString()
        work = intent.getStringExtra("w").toString()
        tableName = intent.getStringExtra("tableName").toString()
        projectName = intent.getStringExtra("projectName").toString()
        binding.projectName.setText(projectName)

//        Source.toast(this@SelectImagesSplit, intent.getStringExtra("tableName").toString())

        val dir =
            File(
                ContextWrapper(this).externalMediaDirs[0],
                getString(R.string.app_name) + id
            )

        val file = File(dir, projectImage)

        setArrayList()


    }

    private fun setArrayList() {
        arrayList = ArrayList()
        duplicateArrayList = ArrayList()


        val cursor: Cursor = databaseHelper.getSplitTableData(tableName)

        if (cursor.moveToFirst()) {
            do {
                val note = SplitData(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    roiTableID,
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getString(11),
                    cursor.getString(12),
                    cursor.getString(13)
                )
                arrayList.add(note)
            } while (cursor.moveToNext())
        }

        duplicateArrayList = arrayList


        if (arrayList.size == 0) {

            val filePath = "SPLIT_NAME" + System.currentTimeMillis() + ".jpg"
            val fileId = "SPLIT_ID" + System.currentTimeMillis()
            val fileName = projectImage

            val currentTimeMillis = System.currentTimeMillis()

            val currentDate = Date(currentTimeMillis)

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val formattedDate: String? = sdf.format(currentDate)

            val timeStamp = formattedDate


            databaseHelper.insertSplitImage(
                tableName,
                fileId,
                fileName,
                filePath,
                timeStamp,
                "100",
                "2",
                roiTableID,
                getIntent().getStringExtra("volumePlotTableID"),
                getIntent().getStringExtra("intensityPlotTableID"),
                getIntent().getStringExtra("plotTableID"),
                getIntent().getStringExtra("projectDescription"),
                "0",
                "0",
                "0"
            )

        } else {
            arrayList.removeAt(0)
        }



        adapter = SplitAdapter(
            this,
            arrayList,
            databaseHelper,
            id,
            projectName,
            projectDescription,
            tableName, "multi",
            getIntent().getStringExtra("timeStamp").toString(),
            getIntent().getStringExtra("projectNumber").toString(),
            getIntent().getStringExtra("splitId").toString()
        )
        binding.recView.adapter = adapter
        adapter.notifyDataSetChanged()


    }


}