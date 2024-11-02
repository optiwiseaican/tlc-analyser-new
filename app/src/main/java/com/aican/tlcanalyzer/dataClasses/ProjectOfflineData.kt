package com.aican.tlcanalyzer.dataClasses

data class ProjectOfflineData(
    val id: String,
    val projectName: String,
    val projectDescription: String,
    val timeStamp: String,
    val projectNumber: String,
    val projectImage: String,
    val imageSplitAvailable: String,
    val splitId: String,
    val thresholdVal: String,
    val noOfSpots: String,
    val tableName: String,
    val roiTableID: String,
    val volumePlotTableID: String,
    val intensityPlotTableID: String,
    val plotTableID: String,
    val rmSpot: String,
    val finalSpot: String
)
