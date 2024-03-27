package com.enaboapps.switchify.keyboard.prediction

import android.content.Context
import co.thingthing.fleksy.lib.api.FleksyLib
import co.thingthing.fleksy.lib.api.LibraryConfiguration
import co.thingthing.fleksy.lib.model.LanguageFile
import co.thingthing.fleksy.lib.model.TypingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface PredictionListener {
    fun onPredictionsAvailable(predictions: List<String>)
}

class PredictionManager(private val context: Context, private val listener: PredictionListener) :
    CoroutineScope {

    private lateinit var fleksyLib: FleksyLib

    private val predictionJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + predictionJob

    fun initialize() {
        val apiKey = "68e0cd20-363e-4668-b52f-80c60757e9bf"
        val secret = "fa9473585679692b7e0f3390cbd04820"
        val licence = LibraryConfiguration.LicenseConfiguration(apiKey, secret)
        val languageFile = LanguageFile.Asset("encrypted/resourceArchive-en-US.jet")
        val config = LibraryConfiguration(licence)
        fleksyLib = FleksyLib(context.applicationContext, languageFile, config)
    }

    fun predict(text: String) {
        // If the last character is a space, get predictions for the next word
        if (text.isNotEmpty() && text.last() == ' ') {
            getPredictionsForNextWord(text)
        } else {
            getCurrentPredictions(text)
        }
    }

    private fun getCurrentPredictions(text: String) {
        println("Getting predictions for: $text")
        val typingContext = TypingContext(text)
        launch {
            val result = fleksyLib.currentWordPrediction(typingContext)
            if (result.isSuccess) {
                val predictions = result.getOrNull()
                if (predictions != null) {
                    for (prediction in predictions) {
                        println(prediction.label)
                    }
                    listener.onPredictionsAvailable(predictions.map { it.label })
                }
            } else {
                println("Error: ${result.exceptionOrNull()}")
            }
        }
    }

    private fun getPredictionsForNextWord(text: String) {
        println("Getting predictions for next word after: $text")
        val typingContext = TypingContext(text)
        launch {
            val result = fleksyLib.nextWordPrediction(typingContext)
            if (result.isSuccess) {
                val predictions = result.getOrNull()
                if (predictions != null) {
                    for (prediction in predictions) {
                        println(prediction.label)
                    }
                    listener.onPredictionsAvailable(predictions.map { it.label })
                }
            } else {
                println("Error: ${result.exceptionOrNull()}")
            }
        }
    }
}