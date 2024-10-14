package com.enaboapps.switchify.preferences

import android.content.SharedPreferences
import android.util.Log
import com.enaboapps.switchify.auth.AuthManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * PreferenceSync manages the synchronization of user preferences between the local SharedPreferences
 * and Firebase Firestore. It provides functionality to upload local settings to Firestore,
 * retrieve settings from Firestore, and listen for remote changes.
 *
 * @property sharedPreferences The SharedPreferences instance used for local storage.
 */
class PreferenceSync(private val sharedPreferences: SharedPreferences) {
    private val firestoreDb = FirebaseFirestore.getInstance()
    private val authManager = AuthManager.instance
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "PreferenceSync"
        private const val COLLECTION_USER_SETTINGS = "user-settings"
        private const val DOCUMENT_PREFERENCES = "preferences"
        private const val COLLECTION_USERS = "users"
        private val BLACKLISTED_KEYS = setOf(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_METHOD)
    }

    /**
     * Uploads the current local settings to Firestore.
     * This method runs asynchronously and logs the result.
     */
    fun uploadSettingsToFirestore() {
        coroutineScope.launch {
            try {
                val userId = getUserId() ?: return@launch
                val userSettings = getAllPreferences()
                uploadSettings(userId, userSettings)
                Log.i(TAG, "Settings uploaded successfully for user: $userId")
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading settings", e)
            }
        }
    }

    /**
     * Retrieves settings from Firestore and applies them to the local SharedPreferences.
     * This method runs asynchronously and logs the result.
     */
    fun retrieveSettingsFromFirestore() {
        coroutineScope.launch {
            try {
                val userId = getUserId() ?: return@launch
                val settings = retrieveSettings(userId)
                if (settings.isNotEmpty()) {
                    applySettings(settings)
                    Log.i(TAG, "Settings retrieved and applied for user: $userId")
                } else {
                    Log.w(TAG, "No settings found for user: $userId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving settings", e)
            }
        }
    }

    /**
     * Sets up a listener for remote changes to the user's settings in Firestore.
     * When changes are detected, it applies them to the local SharedPreferences.
     */
    fun listenForSettingsChangesOnRemote() {
        coroutineScope.launch {
            try {
                val userId = getUserId() ?: return@launch
                setupRemoteListener(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up remote settings listener", e)
            }
        }
    }

    /**
     * Retrieves the current user's ID.
     *
     * @return The user's ID, or null if not available.
     */
    private suspend fun getUserId(): String? = withContext(Dispatchers.IO) {
        authManager.getUserId().also { userId ->
            if (userId == null) Log.e(TAG, "User ID is null")
        }
    }

    /**
     * Retrieves all non-blacklisted preferences from SharedPreferences.
     *
     * @return A map of preference key-value pairs, excluding blacklisted keys and unsupported types.
     */
    private fun getAllPreferences(): Map<String, Any> {
        return sharedPreferences.all.mapNotNull { (key, value) ->
            if (!BLACKLISTED_KEYS.contains(key) && value != null) {
                when (value) {
                    is String, is Boolean, is Int, is Long, is Float -> key to value
                    else -> {
                        Log.w(TAG, "Unsupported type for key: $key, value: $value")
                        null
                    }
                }
            } else null
        }.toMap()
    }

    /**
     * Uploads the given settings to Firestore for the specified user.
     *
     * @param userId The ID of the user whose settings are being uploaded.
     * @param settings The settings to upload.
     */
    private suspend fun uploadSettings(userId: String, settings: Map<String, Any>) {
        withContext(Dispatchers.IO) {
            firestoreDb.collection(COLLECTION_USER_SETTINGS)
                .document(DOCUMENT_PREFERENCES)
                .collection(COLLECTION_USERS)
                .document(userId)
                .set(settings, SetOptions.merge())
                .await()
        }
    }

    /**
     * Retrieves settings from Firestore for the specified user.
     *
     * @param userId The ID of the user whose settings are being retrieved.
     * @return A map of the user's settings, or an empty map if no settings are found.
     */
    private suspend fun retrieveSettings(userId: String): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            firestoreDb.collection(COLLECTION_USER_SETTINGS)
                .document(DOCUMENT_PREFERENCES)
                .collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
                .data ?: emptyMap()
        }
    }

    /**
     * Applies the given settings to the local SharedPreferences.
     *
     * @param settings The settings to apply.
     */
    private fun applySettings(settings: Map<String, Any>) {
        with(sharedPreferences.edit()) {
            settings.forEach { (key, value) ->
                if (!BLACKLISTED_KEYS.contains(key)) {
                    when (value) {
                        is String -> putString(key, value)
                        is Boolean -> putBoolean(key, value)
                        is Long -> putLong(key, value)
                        is Double -> putFloat(key, value.toFloat())
                        is Int -> putInt(key, value)
                        else -> Log.w(TAG, "Unsupported type for key: $key, value: $value")
                    }
                }
            }
            apply()
        }
    }

    /**
     * Sets up a listener for remote changes to the user's settings in Firestore.
     *
     * @param userId The ID of the user whose settings are being monitored.
     */
    private fun setupRemoteListener(userId: String) {
        firestoreDb.collection(COLLECTION_USER_SETTINGS)
            .document(DOCUMENT_PREFERENCES)
            .collection(COLLECTION_USERS)
            .document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error listening for remote settings changes", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    handleRemoteSettingsUpdate(snapshot)
                } else {
                    Log.w(TAG, "Remote settings document does not exist for user: $userId")
                }
            }
        Log.i(TAG, "Registered listener for remote settings changes")
    }

    /**
     * Handles updates to remote settings by applying them to the local SharedPreferences.
     *
     * @param snapshot The DocumentSnapshot containing the updated settings.
     */
    private fun handleRemoteSettingsUpdate(snapshot: DocumentSnapshot) {
        snapshot.data?.let { settings ->
            applySettings(settings)
            Log.i(TAG, "Remote settings updated for user: ${authManager.getUserId()}")
        }
    }
}