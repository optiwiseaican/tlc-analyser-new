package com.aican.tlcanalyzer.utils

import android.graphics.Color

class RandomColors {
    private val usedColors = mutableSetOf<Int>()

    fun getRandomDarkColor(): Int {
        val random = java.util.Random()
        var color: Int

        do {
            val red = random.nextInt(150)
            val green = random.nextInt(150)
            val blue = random.nextInt(150)
            color = Color.rgb(red, green, blue)
        } while (usedColors.contains(color) || isSimilarColor(color))

        usedColors.add(color)
        return color
    }

    private fun isSimilarColor(color: Int): Boolean {
        for (usedColor in usedColors) {
            if (areColorsSimilar(usedColor, color)) {
                return true
            }
        }
        return false
    }

    private fun areColorsSimilar(color1: Int, color2: Int): Boolean {
        val threshold = 50 // Adjust this threshold as needed
        val redDiff = Math.abs(Color.red(color1) - Color.red(color2))
        val greenDiff = Math.abs(Color.green(color1) - Color.green(color2))
        val blueDiff = Math.abs(Color.blue(color1) - Color.blue(color2))

        return redDiff < threshold && greenDiff < threshold && blueDiff < threshold
    }
}
