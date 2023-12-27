package com.enaboapps.switchify.utils

class StringUtils {

    companion object {

        // Function to get time string in seconds from milliseconds (ex. 1.5 seconds)
        fun getSecondsString(milliseconds: Int): String {
            val seconds: Double = (milliseconds / 1000.0)
            return "$seconds seconds"
        }

    }

}