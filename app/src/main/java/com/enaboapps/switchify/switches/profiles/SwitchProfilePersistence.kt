package com.enaboapps.switchify.switches.profiles

import android.content.Context
import android.util.AtomicFile
import com.enaboapps.switchify.switches.SwitchEvent
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStreamWriter

internal interface SwitchProfilePersistence {
    suspend fun readProfiles(): Result<SwitchProfileDocument?>
    suspend fun writeProfiles(document: SwitchProfileDocument): Result<Unit>
    suspend fun readLegacyEvents(): Result<List<SwitchEvent>?>
    suspend fun deleteLegacyEvents(): Result<Unit>
}

internal class SwitchProfileLocalPersistence(context: Context) : SwitchProfilePersistence {
    private val protectedContext = context.applicationContext.createDeviceProtectedStorageContext()
    private val profileFile = AtomicFile(File(protectedContext.filesDir, PROFILE_FILE_NAME))
    private val legacyFile = File(protectedContext.filesDir, LEGACY_FILE_NAME)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    override suspend fun readProfiles(): Result<SwitchProfileDocument?> = withContext(Dispatchers.IO) {
        runCatching {
            if (!profileFile.baseFile.exists()) return@runCatching null
            profileFile.openRead().bufferedReader().use { reader ->
                gson.fromJson(reader, SwitchProfileDocument::class.java)
            }
        }
    }

    override suspend fun writeProfiles(document: SwitchProfileDocument): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val stream = profileFile.startWrite()
                try {
                    OutputStreamWriter(stream).apply {
                        gson.toJson(document, this)
                        flush()
                    }
                    profileFile.finishWrite(stream)
                } catch (error: Throwable) {
                    profileFile.failWrite(stream)
                    throw error
                }
                val verified = profileFile.openRead().bufferedReader().use { reader ->
                    gson.fromJson(reader, SwitchProfileDocument::class.java)
                }
                check(verified == document)
            }
        }

    override suspend fun readLegacyEvents(): Result<List<SwitchEvent>?> = withContext(Dispatchers.IO) {
        runCatching {
            if (!legacyFile.exists()) return@runCatching null
            val type = object : TypeToken<Set<SwitchEvent>>() {}.type
            legacyFile.bufferedReader().use { reader ->
                val events: Set<SwitchEvent> = gson.fromJson(reader, type)
                events.toList()
            }
        }
    }

    override suspend fun deleteLegacyEvents(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (legacyFile.exists()) check(legacyFile.delete())
        }
    }

    private companion object {
        const val PROFILE_FILE_NAME = "switch_profiles.json"
        const val LEGACY_FILE_NAME = "switch_events.json"
    }
}
