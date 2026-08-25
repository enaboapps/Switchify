package com.enaboapps.switchify.service.scanning.preferences

import android.content.SharedPreferences
import com.enaboapps.switchify.backend.preferences.PreferenceManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ScanPreferenceChangeCoordinator(
    private val source: ScanPreferenceChangeSource,
    private val scope: CoroutineScope,
    private val uiDispatcher: CoroutineDispatcher,
    private val applyPlan: (ScanPreferenceUpdatePlan) -> Unit,
    private val onApplied: () -> Unit,
    private val batchDelayMillis: Long = 16L
) {
    private val pendingKeys = mutableSetOf<String>()
    private val lock = Any()
    private var batchJob: Job? = null
    @Volatile
    private var started = false
    @Volatile
    private var generation = 0L

    fun start() {
        if (started) return
        started = true
        generation++
        source.start(::enqueue)
    }

    fun stop() {
        if (!started) return
        started = false
        generation++
        source.stop()
        synchronized(lock) {
            batchJob?.cancel()
            batchJob = null
            pendingKeys.clear()
        }
    }

    fun enqueue(key: String) {
        if (!started || ScanPreferencePolicy.effectFor(key) == null) return
        synchronized(lock) {
            pendingKeys.add(key)
            if (batchJob?.isActive == true) return
            val scheduledGeneration = generation
            batchJob = scope.launch {
                while (started && generation == scheduledGeneration) {
                    delay(batchDelayMillis)
                    val keys = synchronized(lock) {
                        pendingKeys.toSet().also { pendingKeys.clear() }
                    }
                    val plan = ScanPreferencePolicy.reduce(keys)
                    withContext(uiDispatcher) {
                        if (!started || generation != scheduledGeneration || plan.isEmpty) return@withContext
                        applyPlan(plan)
                        onApplied()
                    }
                    val drained = synchronized(lock) {
                        pendingKeys.isEmpty().also {
                            if (it) batchJob = null
                        }
                    }
                    if (drained) return@launch
                }
            }
        }
    }
}

internal interface ScanPreferenceChangeSource {
    fun start(onChanged: (String) -> Unit)
    fun stop()
}

internal class PreferenceManagerScanPreferenceChangeSource(
    private val preferenceManager: PreferenceManager
) : ScanPreferenceChangeSource {
    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun start(onChanged: (String) -> Unit) {
        if (listener != null) return
        listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key != null) onChanged(key)
        }.also(preferenceManager::registerChangeListener)
    }

    override fun stop() {
        listener?.let(preferenceManager::unregisterChangeListener)
        listener = null
    }
}
