package com.enaboapps.switchify.preferences

import android.content.SharedPreferences
import android.util.Log
import com.enaboapps.switchify.auth.AuthManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class PreferenceSync(private val sharedPreferences: SharedPreferences) {
    private val firestoreDb = FirebaseFirestore.getInstance()
    private val authManager = AuthManager.instance
    private val TAG = "PreferenceSync"

    fun uploadSettingsToFirestore() {
        val userId = authManager.getUserId()
        if (userId == null) {
            Log.e(TAG, "uploadSettingsToFirestore: User ID is null")
            return
        }

        val allPrefs = sharedPreferences.all
        val userSettings = hashMapOf<String, Any>()

        allPrefs.forEach { entry ->
            userSettings[entry.key] = entry.value ?: ""
        }

        firestoreDb.collection("user-settings")
            .document("preferences")
            .collection("users")
            .document(userId)
            .set(userSettings, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(TAG, "Settings uploaded successfully for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error uploading settings for user: $userId", e)
            }
    }

    fun retrieveSettingsFromFirestore() {
        val userId = authManager.getUserId()
        if (userId == null) {
            Log.e(TAG, "retrieveSettingsFromFirestore: User ID is null")
            return
        }

        firestoreDb.collection("user-settings")
            .document("preferences")
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    documentSnapshot.data?.let { settings ->
                        with(sharedPreferences.edit()) {
                            settings.forEach { (key, value) ->
                                when (value) {
                                    is String -> putString(key, value)
                                    is Boolean -> putBoolean(key, value)
                                    is Long -> putLong(key, value)
                                    is Double -> putFloat(key, value.toFloat())
                                    is Int -> putInt(key, value)
                                    else -> Log.w(
                                        TAG,
                                        "Unsupported type for key: $key, value: $value"
                                    )
                                }
                            }
                            apply()
                        }
                        Log.i(TAG, "Settings retrieved and applied for user: $userId")
                    }
                } else {
                    Log.w(TAG, "No settings found for user: $userId")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error retrieving settings for user: $userId", e)
            }
    }

    fun listenForSettingsChangesOnRemote() {
        val userId = authManager.getUserId()
        if (userId == null) {
            Log.e(TAG, "listenForSettingsChangesOnRemote: User ID is null")
            return
        }

        firestoreDb.collection("user-settings")
            .document("preferences")
            .collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error listening for remote settings changes", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    snapshot.data?.let { settings ->
                        with(sharedPreferences.edit()) {
                            settings.forEach { (key, value) ->
                                when (value) {
                                    is String -> putString(key, value)
                                    is Boolean -> putBoolean(key, value)
                                    is Long -> putLong(key, value)
                                    is Double -> putFloat(key, value.toFloat())
                                    is Int -> putInt(key, value)
                                    else -> Log.w(
                                        TAG,
                                        "Unsupported type for key: $key, value: $value"
                                    )
                                }
                            }
                            apply()
                        }
                        Log.i(TAG, "Remote settings updated for user: $userId")
                    }
                } else {
                    Log.w(TAG, "Remote settings document does not exist for user: $userId")
                }
            }
        Log.i(TAG, "Registered listener for remote settings changes")
    }
}