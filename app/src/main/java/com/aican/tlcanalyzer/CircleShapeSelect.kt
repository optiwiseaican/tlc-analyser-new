package com.aican.tlcanalyzer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.databinding.ActivityCircleShapeSelectBinding
import com.aican.tlcanalyzer.dragcircle.dragscalecircleview.DragScaleCircleView
import com.aican.tlcanalyzer.utils.Source

class CircleShapeSelect : AppCompatActivity() {

    lateinit var dragScaleCircleView: DragScaleCircleView
    lateinit var binding: ActivityCircleShapeSelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCircleShapeSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dragScaleCircleView = findViewById(R.id.dragScaleCircleView)

        dragScaleCircleView.setImageBitmap(Source.roiBitmap)

    }
}