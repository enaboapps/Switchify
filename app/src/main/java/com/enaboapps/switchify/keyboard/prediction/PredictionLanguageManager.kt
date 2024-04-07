package com.enaboapps.switchify.keyboard.prediction

import android.content.Context
import co.thingthing.fleksy.lib.languages.LanguagesHelper
import co.thingthing.fleksy.lib.languages.RemoteLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class PredictionLanguageManager(private val context: Context): CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    fun getAvailableLanguages(completion: (List<RemoteLanguage?>) -> Unit) {
        // Get available languages
        LanguagesHelper.availableLanguages(context) { result ->
            if (result.isSuccess) {
                val languages = result.getOrNull()
                if (languages != null) {
                    completion(languages)
                }
            } else {
                completion(emptyList())
            }
        }
    }
}