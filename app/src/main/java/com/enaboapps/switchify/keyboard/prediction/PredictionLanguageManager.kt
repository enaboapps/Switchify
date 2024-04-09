package com.enaboapps.switchify.keyboard.prediction

import android.content.Context
import co.thingthing.fleksy.lib.languages.LanguagesHelper
import co.thingthing.fleksy.lib.languages.RemoteLanguage
import co.thingthing.fleksy.lib.model.LanguageFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlin.coroutines.CoroutineContext

class PredictionLanguageManager(private val context: Context) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    fun getAvailableLanguages(completion: (List<RemoteLanguage?>) -> Unit) {
        // Get available languages
        LanguagesHelper.availableLanguages(context.applicationContext) { result ->
            if (result.isSuccess) {
                val languages = result.getOrNull()
                if (languages != null) {
                    val enLanguages = languages.filter { it?.locale?.startsWith("en") ?: false }
                    completion(enLanguages)
                } else {
                    completion(emptyList())
                }
            } else {
                completion(emptyList())
            }
        }
    }

    fun downloadLanguage(language: RemoteLanguage, completion: (Boolean) -> Unit) {
        deleteLanguageFiles()

        LanguagesHelper.downloadLanguageFile(
            context.applicationContext,
            language.locale,
            getDestinationToDownloadTo(language)
        ) { result ->
            completion(result.isSuccess)
        }
    }

    fun getCurrentLanguageFilePath(): String {
        val languagesDir = File(context.filesDir, "languages")
        if (languagesDir.exists()) {
            val files = languagesDir.listFiles()
            return if (files != null && files.isNotEmpty()) {
                files[0].absolutePath
            } else {
                stockLanguageFilePath()
            }
        }
        return stockLanguageFilePath()
    }

    fun getFleksyLanguage(): LanguageFile {
        val languageDir = File(context.filesDir, "languages")
        val languageFiles = languageDir.listFiles()
        return if (languageFiles != null && languageFiles.isNotEmpty()) {
            LanguageFile.File(languageFiles[0].absolutePath)
        } else {
            LanguageFile.Asset(stockLanguageFilePath())
        }
    }

    private fun deleteLanguageFiles() {
        val languagesDir = File(context.filesDir, "languages")
        if (languagesDir.exists()) {
            languagesDir.deleteRecursively()
        }
    }

    private fun stockLanguageFilePath(): String {
        return "encrypted/resourceArchive-en-US.jet"
    }

    private fun getDestinationToDownloadTo(language: RemoteLanguage): File {
        val languagesDir = File(context.filesDir, "languages")
        if (!languagesDir.exists()) {
            languagesDir.mkdirs()
        }
        return File(languagesDir, "${language.locale}.jet")
    }
}