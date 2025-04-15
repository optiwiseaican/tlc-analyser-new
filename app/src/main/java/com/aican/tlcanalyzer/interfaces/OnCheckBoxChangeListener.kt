package com.aican.tlcanalyzer.interfaces

import com.aican.tlcanalyzer.dataClasses.AnalMultiIntModel

interface OnCheckBoxChangeListener {
    fun onCheckBoxChange(
        position: Int,
        data: AnalMultiIntModel
    )

}