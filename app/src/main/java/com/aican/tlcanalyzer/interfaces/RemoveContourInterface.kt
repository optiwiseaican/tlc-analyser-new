package com.aican.tlcanalyzer.interfaces

import android.graphics.Rect

interface RemoveContourInterface {
    fun removeContour(id: Int)
    fun removeManualContour(id: Int, mId: String, rfId: Int)

    fun editManualContour(id: Int, mId: String, rfId: Int, rect: Rect)
}