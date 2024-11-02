package com.aican.tlcanalyzer.dataClasses

data class ManualContour(
    var shape: Int,
    var roi: android.graphics.Rect,
    var indexName: String,
    var mainContIndex: String,
    var rfIndex: Int
)
