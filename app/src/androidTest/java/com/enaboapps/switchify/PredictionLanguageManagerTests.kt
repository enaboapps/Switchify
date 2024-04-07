package com.enaboapps.switchify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enaboapps.switchify.keyboard.prediction.PredictionLanguageManager
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PredictionLanguageManagerTests {
    @Test
    fun `getAvailableLanguages should return available languages`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = PredictionLanguageManager(context)
        manager.getAvailableLanguages { languages ->
            for (language in languages) {
                println("Language: ${language?.locale}")
            }
            assert(languages.isNotEmpty())
            assert(languages.any { it?.locale == "en-US" })
        }
    }
}