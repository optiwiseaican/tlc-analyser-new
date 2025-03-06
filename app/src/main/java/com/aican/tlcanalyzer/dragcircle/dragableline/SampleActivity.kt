package com.aican.tlcanalyzer.dragcircle.dragableline

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.R

class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        val draggableLinesView = findViewById<DraggableVerticalLinesView>(R.id.draggableLinesView)

        // ✅ Load an image from resources and set it in the view
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        draggableLinesView.setImage(bitmap)

        // ✅ Buttons for actions
        findViewById<Button>(R.id.addLineButton).setOnClickListener {
            val randomX = (50..950).random().toFloat()  // Random X position within bounds
            draggableLinesView.addNewLine(randomX)
        }

        findViewById<Button>(R.id.undoLastButton).setOnClickListener {
            draggableLinesView.undoLast()
        }

        findViewById<Button>(R.id.clearAllButton).setOnClickListener {
            draggableLinesView.clearAll()
        }

        findViewById<Button>(R.id.getAllLines).setOnClickListener {
            val lines = draggableLinesView.getAllLines()
            Log.d("VerticalLines", "Lines at: $lines")
            Toast.makeText(this, "Lines at: $lines", Toast.LENGTH_SHORT).show()
        }
    }
}
