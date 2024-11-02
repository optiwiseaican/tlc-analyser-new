package com.aican.tlcanalyzer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.adapterClasses.CroppedAdapter
import com.aican.tlcanalyzer.utils.Source

class CroppedImages : AppCompatActivity() {


    lateinit var recyclerView: RecyclerView
    lateinit var croppedAdapter: CroppedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cropped_images)

        recyclerView = findViewById(R.id.recyclerView)


        croppedAdapter = CroppedAdapter(this@CroppedImages, Source.croppedArrayList)
        recyclerView.adapter = croppedAdapter

        croppedAdapter.notifyDataSetChanged()



    }
}