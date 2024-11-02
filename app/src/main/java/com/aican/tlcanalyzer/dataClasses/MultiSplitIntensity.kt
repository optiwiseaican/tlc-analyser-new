package com.aican.tlcanalyzer.dataClasses

data class MultiSplitIntensity(
    val name: String,
    var isSelected: Boolean,
    val intensity: ArrayList<RFvsArea>
//    val splitContourDataList: ArrayList<SplitContourData>
) {
    override fun toString(): String {
        val selectedStatus = if (isSelected) "Selected" else "Not Selected"
        return "Name: $name, $selectedStatus, Intensity: $intensity"
    }
}
