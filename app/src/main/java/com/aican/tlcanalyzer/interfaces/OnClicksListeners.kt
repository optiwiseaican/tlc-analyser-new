package com.aican.tlcanalyzer.interfaces

interface OnClicksListeners {

    fun onClick(
        position: Int,
        parentPosition: Int,
        id: String,
        rfTop: String,
        rfBottom: String,
        rf: String,
        isSelected: Boolean
    )

    fun newOnClick(position: Int)


}