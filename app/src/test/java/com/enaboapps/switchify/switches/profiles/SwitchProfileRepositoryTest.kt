package com.enaboapps.switchify.switches.profiles

import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEvent
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SwitchProfileRepositoryTest {
    @Test
    fun migratesLegacyMappingsBeforeDeletingLegacyFile() = runBlocking {
        val legacy = listOf(event("1"))
        val persistence = FakePersistence(legacy = legacy)
        val repository = repository(persistence)

        repository.initialize()

        assertEquals("Default", repository.activeProfile().name)
        assertEquals(legacy, repository.events())
        assertEquals(repository.document.value, persistence.stored)
        assertTrue(persistence.legacyDeleted)
    }

    @Test
    fun failedMigrationWriteKeepsLegacyFile() = runBlocking {
        val persistence = FakePersistence(legacy = listOf(event("1")), failWrites = true)
        val repository = repository(persistence)

        repository.initialize()

        assertFalse(persistence.legacyDeleted)
        assertEquals(null, persistence.stored)
    }

    @Test
    fun malformedDocumentFallsBackToDefaultProfile() = runBlocking {
        val malformed = SwitchProfileDocument(activeProfileId = "missing", profiles = emptyList())
        val persistence = FakePersistence(stored = malformed)
        val repository = repository(persistence)

        repository.initialize()

        assertEquals("Default", repository.activeProfile().name)
        assertEquals(1, repository.profiles().size)
        assertNotEquals("missing", repository.document.value.activeProfileId)
    }

    @Test
    fun createsAndDuplicatesProfilesWithIsolatedMappings() = runBlocking {
        val repository = repository(FakePersistence())
        repository.initialize()
        val activeId = repository.activeProfile().id
        repository.replaceEvents(activeId, listOf(event("shared")))

        val duplicated = repository.duplicate(activeId, "Driving") as SwitchProfileMutationResult.Success
        assertNotEquals(activeId, duplicated.profile.id)
        assertEquals(listOf("shared"), repository.events(duplicated.profile.id).map { it.code })

        repository.replaceEvents(duplicated.profile.id, listOf(event("shared"), event("other")))
        assertEquals(listOf("shared"), repository.events(activeId).map { it.code })
        assertEquals(2, repository.events(duplicated.profile.id).size)
    }

    @Test
    fun enforcesNamesAndDeletionRules() = runBlocking {
        val repository = repository(FakePersistence())
        repository.initialize()
        val activeId = repository.activeProfile().id

        assertTrue(repository.createEmpty(" ") is SwitchProfileMutationResult.InvalidName)
        assertTrue(repository.createEmpty("default") is SwitchProfileMutationResult.InvalidName)
        assertTrue(repository.delete(activeId) is SwitchProfileMutationResult.ActiveProfile)

        val second = repository.createEmpty("Second") as SwitchProfileMutationResult.Success
        assertTrue(repository.rename(second.profile.id, "DEFAULT") is SwitchProfileMutationResult.InvalidName)
        assertTrue(repository.delete(second.profile.id) is SwitchProfileMutationResult.Success)
    }

    @Test
    fun failedSavePreservesDocumentAndActiveProfile() = runBlocking {
        val persistence = FakePersistence()
        val repository = repository(persistence)
        repository.initialize()
        val before = repository.document.value
        persistence.failWrites = true

        val result = repository.createEmpty("Will fail")

        assertTrue(result is SwitchProfileMutationResult.StorageFailure)
        assertEquals(before, repository.document.value)
        assertEquals(before.activeProfileId, repository.activeProfile().id)
    }

    @Test
    fun persistsActiveProfileOnlyAfterCommit() = runBlocking {
        val persistence = FakePersistence()
        val repository = repository(persistence)
        repository.initialize()
        val oldActive = repository.activeProfile().id
        val next = repository.createEmpty("Next") as SwitchProfileMutationResult.Success

        assertEquals(oldActive, repository.activeProfile().id)
        assertTrue(repository.commitActiveProfile(next.profile.id))
        assertEquals(next.profile.id, repository.activeProfile().id)
        assertEquals(next.profile.id, persistence.stored?.activeProfileId)
    }

    @Test
    fun rejectsDuplicateCodesInsideOneProfile() = runBlocking {
        val repository = repository(FakePersistence())
        repository.initialize()

        assertFalse(
            repository.replaceEvents(
                repository.activeProfile().id,
                listOf(event("same"), event("same"))
            )
        )
    }

    private fun repository(persistence: FakePersistence): SwitchProfileRepository {
        var id = 0
        return SwitchProfileRepository(persistence) { "profile-${++id}" }
    }

    private fun event(code: String) = SwitchEvent(
        name = code,
        code = code,
        pressAction = SwitchAction(SwitchAction.ACTION_SELECT),
        holdActions = emptyList()
    )

    private class FakePersistence(
        var stored: SwitchProfileDocument? = null,
        private val legacy: List<SwitchEvent>? = null,
        var failWrites: Boolean = false
    ) : SwitchProfilePersistence {
        var legacyDeleted = false

        override suspend fun readProfiles(): Result<SwitchProfileDocument?> = Result.success(stored)

        override suspend fun writeProfiles(document: SwitchProfileDocument): Result<Unit> {
            if (failWrites) return Result.failure(IllegalStateException("write failed"))
            stored = document
            return Result.success(Unit)
        }

        override suspend fun readLegacyEvents(): Result<List<SwitchEvent>?> = Result.success(legacy)

        override suspend fun deleteLegacyEvents(): Result<Unit> {
            legacyDeleted = true
            return Result.success(Unit)
        }
    }
}
