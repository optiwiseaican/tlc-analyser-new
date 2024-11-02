package com.aican.tlcanalyzer.interfaces

import android.content.Context
import java.io.File

interface OnPDFSelectListener {
    fun onPDFSelected(file: File?, fileName: String, position: Int)
    fun onDelete(file: File?, position: Int)
    fun inExternalApp(file: File?, context: Context?)
}