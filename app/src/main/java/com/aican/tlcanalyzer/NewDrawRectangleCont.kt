package com.aican.tlcanalyzer

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aican.tlcanalyzer.databinding.ActivityNewDrawRectangleContBinding
import com.aican.tlcanalyzer.dragcircle.dragableline.DraggableLinesView
import com.aican.tlcanalyzer.utils.Source
import kotlin.random.Random

class NewDrawRectangleCont : AppCompatActivity() {

    private lateinit var draggableLinesView: DraggableLinesView
    lateinit var binding: ActivityNewDrawRectangleContBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewDrawRectangleContBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        draggableLinesView = binding.draggableLinesView

        binding.back.setOnClickListener {
            finish()
        }


        if (Source.contourBitmap == null) {
            Log.e("ItsNullvalue", "Null Uri" + "")
            Toast.makeText(
                this@NewDrawRectangleCont,
                "Bitmap Null, Spot the contour once",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        binding.draggableLinesView.setImage(Source.contourBitmap)

        binding.addLineButton.setOnClickListener {
            val viewHeight = draggableLinesView.height

            val newY = if (draggableLinesView.lines.isEmpty() || draggableLinesView.lines.size % 2 == 0) {
                // First line or new rectangle: Add on the left side
                100f
            } else {
                // Second line of the rectangle: Add slightly to the right of the previous line
                draggableLinesView.lines.last() + 20f
            }

            // Ensure it doesn't exceed the view height
            if (newY < viewHeight - 50) {
                draggableLinesView.addNewLine(newY)
            }
        }


        binding.clearAll.setOnClickListener {
            draggableLinesView.clearAll()
        }

        binding.undoButton.setOnClickListener {
            draggableLinesView.undoLast()
        }

        binding.saveRect.setOnClickListener {
            val rectangles = draggableLinesView.getAllRectangles()

            if (rectangles.isNotEmpty()) {
                // Store the rectangles in Source.rectangleList
                Source.rectangleList = ArrayList()
                Source.rectangleList.addAll(rectangles)
                Source.rectangle = true

                // Finish activity (same as in DrawRectangleCont)
                finish()
            } else {
                Toast.makeText(
                    this@NewDrawRectangleCont,
                    "No rectangles detected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }
}