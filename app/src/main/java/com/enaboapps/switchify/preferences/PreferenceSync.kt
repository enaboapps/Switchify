package com.enaboapps.switchify.preferences

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.content.SharedPreferences
import com.enaboapps.switchify.auth.AuthManager

class PreferenceSync(private val sharedPreferences: SharedPreferences) {
    private val firestoreDb = FirebaseFirestore.getInstance()
    private val authManager = AuthManager.instance

    fun uploadSettingsToFirestore() {
        val userId = authManager.getUserId() ?: return

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
                // Handle success
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    fun retrieveSettingsFromFirestore() {
        val userId = authManager.getUserId() ?: return
        firestoreDb.collection("user-settings")
            .document("preferences")
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                documentSnapshot.data?.let { settings ->
                    with(sharedPreferences.edit()) {
                        settings.forEach { (key, value) ->
                            when (value) {
                                is String -> putString(key, value)
                                is Boolean -> putBoolean(key, value)
                                is Long -> putLong(key, value)
                                is Double -> putFloat(key, value.toFloat()) // Firestore stores floats as doubles
                                is Int -> putInt(key, value)
                                else -> {} // Add more types as needed
                            }
                        }
                        apply()
                    }
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    fun listenForSettingsChanges() {
        val userId = authManager.getUserId() ?: return
        firestoreDb.collection("user-settings")
            .document("preferences")
            .collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                snapshot?.data?.let { settings ->
                    with(sharedPreferences.edit()) {
                        settings.forEach { (key, value) ->
                            when (value) {
                                is String -> putString(key, value)
                                is Boolean -> putBoolean(key, value)
                                is Long -> putLong(key, value)
                                is Double -> putFloat(key, value.toFloat()) // Handle the Double to Float conversion
                                is Int -> putInt(key, value)
                                else -> {} // Handle other types as needed
                            }
                        }
                        apply()
                    }
                }
            }
    }
}