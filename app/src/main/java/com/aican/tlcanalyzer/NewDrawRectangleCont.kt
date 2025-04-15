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
            val rectangleHeight = 50f  // Fixed height for the rectangle

            // Determine the position of the new top line
            val newY = if (draggableLinesView.lines.isEmpty() || draggableLinesView.lines.size % 2 == 0) {
                100f  // First rectangle starts at 100px
            } else {
                draggableLinesView.lines.last() + 70f  // Space below the last rectangle
            }

            val bottomY = newY + rectangleHeight  // Bottom line for the rectangle

            // Ensure both lines fit inside the view height
            if (bottomY <= viewHeight - 10) {
                draggableLinesView.addNewLine(newY)     // Add top line
                draggableLinesView.addNewLine(bottomY)  // Add bottom line
            } else {
                Toast.makeText(this, "No space for more rectangles", Toast.LENGTH_SHORT).show()
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