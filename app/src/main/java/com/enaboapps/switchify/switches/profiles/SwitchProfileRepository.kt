package com.enaboapps.switchify.switches.profiles

import android.content.Context
import com.enaboapps.switchify.switches.SwitchEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import java.util.UUID

internal class SwitchProfileRepository internal constructor(
    private val persistence: SwitchProfilePersistence,
    private val idFactory: () -> String = { UUID.randomUUID().toString() }
) {
    private val mutex = Mutex()
    private var initialized = false
    private val placeholder = SwitchProfileDocument(
        activeProfileId = "uninitialized",
        profiles = listOf(SwitchProfile("uninitialized", "Default", emptyList()))
    )
    private val _document = MutableStateFlow(placeholder)
    val document: StateFlow<SwitchProfileDocument> = _document.asStateFlow()

    suspend fun initialize() = mutex.withLock {
        if (initialized) return@withLock
        val stored = persistence.readProfiles().getOrNull()
        val resolved = stored?.takeIf(::isValidDocument) ?: migrateLegacy()
        _document.value = resolved
        initialized = true
    }

    fun profiles(): List<SwitchProfile> = _document.value.profiles

    fun activeProfile(): SwitchProfile = profile(_document.value.activeProfileId)
        ?: _document.value.profiles.first()

    fun profile(profileId: String): SwitchProfile? =
        _document.value.profiles.firstOrNull { it.id == profileId }

    fun events(profileId: String = _document.value.activeProfileId): List<SwitchEvent> =
        profile(profileId)?.switches.orEmpty().map { it.copy(holdActions = it.holdActions.toList()) }

    suspend fun createEmpty(name: String): SwitchProfileMutationResult =
        create(name, emptyList())

    suspend fun duplicate(profileId: String, name: String): SwitchProfileMutationResult {
        val source = profile(profileId) ?: return SwitchProfileMutationResult.NotFound
        return create(name, source.switches)
    }

    suspend fun rename(profileId: String, name: String): SwitchProfileMutationResult = mutex.withLock {
        ensureInitializedLocked()
        val current = profile(profileId) ?: return@withLock SwitchProfileMutationResult.NotFound
        validateName(name, profileId)?.let { return@withLock it }
        val renamed = current.copy(name = name.trim())
        val updated = _document.value.copy(
            profiles = _document.value.profiles.map { if (it.id == profileId) renamed else it }
        )
        if (!persist(updated)) return@withLock SwitchProfileMutationResult.StorageFailure
        SwitchProfileMutationResult.Success(renamed)
    }

    suspend fun delete(profileId: String): SwitchProfileMutationResult = mutex.withLock {
        ensureInitializedLocked()
        val current = profile(profileId) ?: return@withLock SwitchProfileMutationResult.NotFound
        if (_document.value.activeProfileId == profileId) {
            return@withLock SwitchProfileMutationResult.ActiveProfile
        }
        if (_document.value.profiles.size == 1) {
            return@withLock SwitchProfileMutationResult.LastProfile
        }
        val updated = _document.value.copy(
            profiles = _document.value.profiles.filterNot { it.id == profileId }
        )
        if (!persist(updated)) return@withLock SwitchProfileMutationResult.StorageFailure
        SwitchProfileMutationResult.Success(current)
    }

    suspend fun replaceEvents(profileId: String, events: List<SwitchEvent>): Boolean = mutex.withLock {
        ensureInitializedLocked()
        val current = profile(profileId) ?: return@withLock false
        if (events.map { it.code }.distinct().size != events.size) return@withLock false
        val updatedProfile = current.copy(
            switches = events.map { it.copy(holdActions = it.holdActions.toList()) }
        )
        persist(
            _document.value.copy(
                profiles = _document.value.profiles.map {
                    if (it.id == profileId) updatedProfile else it
                }
            )
        )
    }

    suspend fun commitActiveProfile(profileId: String): Boolean = mutex.withLock {
        ensureInitializedLocked()
        if (profile(profileId) == null) return@withLock false
        persist(_document.value.copy(activeProfileId = profileId))
    }

    private suspend fun create(
        name: String,
        switches: List<SwitchEvent>
    ): SwitchProfileMutationResult = mutex.withLock {
        ensureInitializedLocked()
        validateName(name)?.let { return@withLock it }
        val created = SwitchProfile(
            id = idFactory(),
            name = name.trim(),
            switches = switches.map { it.copy(holdActions = it.holdActions.toList()) }
        )
        val updated = _document.value.copy(profiles = _document.value.profiles + created)
        if (!persist(updated)) return@withLock SwitchProfileMutationResult.StorageFailure
        SwitchProfileMutationResult.Success(created)
    }

    private fun validateName(
        name: String,
        excludedProfileId: String? = null
    ): SwitchProfileMutationResult.InvalidName? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return SwitchProfileMutationResult.InvalidName("required")
        val exists = _document.value.profiles.any {
            it.id != excludedProfileId && it.name.equals(trimmed, ignoreCase = true)
        }
        return if (exists) SwitchProfileMutationResult.InvalidName("duplicate") else null
    }

    private suspend fun ensureInitializedLocked() {
        if (initialized) return
        val stored = persistence.readProfiles().getOrNull()
        _document.value = stored?.takeIf(::isValidDocument) ?: migrateLegacy()
        initialized = true
    }

    private suspend fun migrateLegacy(): SwitchProfileDocument {
        val legacyEvents = persistence.readLegacyEvents().getOrNull().orEmpty()
        val migrated = newDocument(legacyEvents)
        if (persistence.writeProfiles(migrated).isSuccess) {
            persistence.deleteLegacyEvents()
        }
        return migrated
    }

    private suspend fun persist(document: SwitchProfileDocument): Boolean {
        if (persistence.writeProfiles(document).isFailure) return false
        _document.value = document
        return true
    }

    private fun isValidDocument(document: SwitchProfileDocument): Boolean {
        if (document.version != SwitchProfileDocument.CURRENT_VERSION) return false
        if (document.profiles.isEmpty()) return false
        if (document.profiles.none { it.id == document.activeProfileId }) return false
        if (document.profiles.any { it.id.isBlank() || it.name.isBlank() }) return false
        if (document.profiles.map { it.id }.distinct().size != document.profiles.size) return false
        return document.profiles.map { it.name.lowercase(Locale.ROOT) }.distinct().size ==
            document.profiles.size
    }

    private fun newDocument(events: List<SwitchEvent>): SwitchProfileDocument {
        val defaultProfile = SwitchProfile(
            id = idFactory(),
            name = "Default",
            switches = events.map { it.copy(holdActions = it.holdActions.toList()) }
        )
        return SwitchProfileDocument(
            activeProfileId = defaultProfile.id,
            profiles = listOf(defaultProfile)
        )
    }

    companion object {
        @Volatile
        private var instance: SwitchProfileRepository? = null

        fun getInstance(context: Context): SwitchProfileRepository {
            return instance ?: synchronized(this) {
                instance ?: SwitchProfileRepository(
                    SwitchProfileLocalPersistence(context)
                ).also { instance = it }
            }
        }
    }
}
