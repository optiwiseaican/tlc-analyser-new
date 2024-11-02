package com.aican.tlcanalyzer.settingActivities

import android.R
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.SplitImage
import com.aican.tlcanalyzer.databinding.SettingsActivityBinding
import com.aican.tlcanalyzer.utils.SharedPrefData
import com.aican.tlcanalyzer.utils.Source

class SplitSettings : AppCompatActivity() {

    lateinit var id: String
    lateinit var projectName: String
    lateinit var binding: SettingsActivityBinding
    val arrayOfInt = arrayOf(100, 500, 1000, 2000, 3000, 5000, 10000)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        id = intent.getStringExtra("id").toString()
        projectName = intent.getStringExtra("projectName").toString()

        binding.projectName.text = "$projectName Settings"

        binding.back.setOnClickListener {
            finish()
        }

        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, arrayOfInt)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.noOfPartSpinner.adapter = adapter

        if (SharedPrefData.getSavedData(
                this@SplitSettings,
                SplitImage.INTENSITY_PART_KEY
            ) != null && SharedPrefData.getSavedData(
                this@SplitSettings,
                SplitImage.INTENSITY_PART_KEY
            ) != ""
        ) {
            val data =
                SharedPrefData.getSavedData(this@SplitSettings, SplitImage.INTENSITY_PART_KEY)

            Source.PARTS_INTENSITY = data.toInt()

            for (d in arrayOfInt.indices) {
                if (arrayOfInt[d].toString() == data) {
                    binding.noOfPartSpinner.setSelection(d)
                }
            }
        } else {
            SharedPrefData.saveData(this@SplitSettings, SplitImage.INTENSITY_PART_KEY, "1000")
            Source.PARTS_INTENSITY = 1000
        }

        binding.noOfPartSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent.getItemAtPosition(position) as Int

                    SharedPrefData.saveData(
                        this@SplitSettings,
                        SplitImage.INTENSITY_PART_KEY,
                        selectedItem.toString()
                    )

                    Source.PARTS_INTENSITY = selectedItem

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do something when nothing is selected
                }
            }
    }


}