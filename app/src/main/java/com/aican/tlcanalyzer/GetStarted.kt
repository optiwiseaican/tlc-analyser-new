package com.aican.tlcanalyzer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.aican.tlcanalyzer.utils.SharedPrefData

class GetStarted : AppCompatActivity() {

    lateinit var getStartedButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)
        supportActionBar?.hide()
        getStartedButton = findViewById(R.id.getStartedButton)

        if (SharedPrefData.getSavedData(this, "first").toString() == "y") {
            val intent = Intent(this@GetStarted, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        getStartedButton.setOnClickListener {
            SharedPrefData.saveData(this@GetStarted, "first", "y")
            val intent = Intent(this@GetStarted, LoginActivity::class.java)
            startActivity(intent)
            finish()

        }

    }
}