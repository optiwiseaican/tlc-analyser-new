package com.aican.tlcanalyzer.dragcircle.dragableline

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aican.tlcanalyzer.R
import kotlin.random.Random

class SampleActivity : AppCompatActivity() {

    private lateinit var draggableLinesView: DraggableLinesView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        draggableLinesView = findViewById(R.id.draggableLinesView)

        // Load Image
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        draggableLinesView.setImage(bitmap)

        // Add Line Button
        findViewById<Button>(R.id.addLineButton).setOnClickListener {
            val randomY = Random.nextInt(100, draggableLinesView.height - 50).toFloat()
            draggableLinesView.addNewLine(randomY)
        }

        // Clear All Button
        findViewById<Button>(R.id.clearAllButton).setOnClickListener {
            draggableLinesView.clearAll()
        }

        // Undo Last Button
        findViewById<Button>(R.id.undoLastButton).setOnClickListener {
            draggableLinesView.undoLast()
        }
        // Undo Last Button
        findViewById<Button>(R.id.getAllRectangles).setOnClickListener {
            val rectangles = draggableLinesView.getAllRectangles()
            println(rectangles)

        }
    }
}
