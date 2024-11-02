package com.aican.tlcanalyzer.dataClasses

data class SplitData(
    val id: String,
    val imageName: String,
    val imagePath: String,
    val timeStamp: String,
    val thresholdVal: String,
    val noOfSpots: String,
    val roiTableID: String,
    val volumePlotTableID: String,
    val intensityPlotTableID: String,
    val plotTableID: String,
    val description: String,
    val hour: String,
    val rmSpot: String,
    val finalSpot: String
){

    override fun toString(): String {
        return "ID: $id, " +
                "Image Name: $imageName, " +
                "Image Path: $imagePath, " +
                "Time Stamp: $timeStamp, " +
                "Threshold Value: $thresholdVal, " +
                "Number of Spots: $noOfSpots, " +
                "ROI Table ID: $roiTableID, " +
                "Volume Plot Table ID: $volumePlotTableID, " +
                "Intensity Plot Table ID: $intensityPlotTableID, " +
                "Plot Table ID: $plotTableID, " +
                "Description: $description, " +
                "Hour: $hour, " +
                "RM Spot: $rmSpot, " +
                "Final Spot: $finalSpot"
    }

}
