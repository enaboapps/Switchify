package com.enaboapps.switchify.keyboard.prediction

import android.content.Context
import co.thingthing.fleksy.lib.api.FleksyLib
import co.thingthing.fleksy.lib.api.LibraryConfiguration
import co.thingthing.fleksy.lib.model.LanguageFile
import co.thingthing.fleksy.lib.model.TypingContext
import com.enaboapps.switchify.BuildConfig
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
        val apiKey = BuildConfig.FLEKSY_API_KEY
        val secret = BuildConfig.FLEKSY_API_SECRET
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
                    // Prevent duplicate predictions
                    val uniquePredictions = predictions.distinctBy { it.label }
                    println("Unique predictions: ${uniquePredictions.map { it.label }}")
                    listener.onPredictionsAvailable(uniquePredictions.map { it.label })
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
                    // Prevent duplicate predictions
                    val uniquePredictions = predictions.distinctBy { it.label }
                    println("Unique predictions: ${uniquePredictions.map { it.label }}")
                    listener.onPredictionsAvailable(uniquePredictions.map { it.label })
                }
            } else {
                println("Error: ${result.exceptionOrNull()}")
            }
        }
    }
}