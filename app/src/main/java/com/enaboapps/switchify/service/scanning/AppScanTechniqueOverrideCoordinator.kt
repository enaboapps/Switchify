package com.enaboapps.switchify.service.scanning

import com.enaboapps.switchify.service.remotebridge.SwitchifyRemoteLauncher
import com.enaboapps.switchify.service.techniques.AccessTechnique
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong

internal fun interface AppScanTechniquePolicy {
    fun techniqueFor(packageName: String): String?
}

internal object DefaultAppScanTechniquePolicy : AppScanTechniquePolicy {
    override fun techniqueFor(packageName: String): String? =
        when (packageName) {
            SwitchifyRemoteLauncher.REMOTE_PACKAGE -> AccessTechnique.Technique.ITEM_SCAN
            else -> null
        }
}

internal class AppScanTechniqueOverrideCoordinator(
    private val controller: ScanModeController,
    private val policy: AppScanTechniquePolicy,
    private val menuActions: AppScanTechniqueOverrideMenuActions =
        AppScanTechniqueOverrideMenuActions(),
    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) {
    private val requestedGeneration = AtomicLong()
    private var foregroundPackage: String? = null
    private var session: TemporaryScanModeSession? = null
    private var isClosingMenuForOverride = false

    suspend fun onForegroundApplicationChanged(packageName: String?) {
        if (packageName == null) return
        val generation = requestedGeneration.incrementAndGet()
        withContext(uiDispatcher) {
            if (!isGenerationCurrent(generation) || packageName == foregroundPackage) {
                return@withContext
            }
            session?.close()
            session = null
            foregroundPackage = packageName
            val targetTechnique = policy.techniqueFor(packageName) ?: return@withContext
            isClosingMenuForOverride = true
            try {
                menuActions.closeIfOpen { isCurrent(generation, packageName) }
            } finally {
                isClosingMenuForOverride = false
            }
            if (!isCurrent(generation, packageName)) return@withContext
            startSession(targetTechnique)
        }
    }

    fun refreshForegroundOverride() {
        if (isClosingMenuForOverride) return
        val packageName = foregroundPackage ?: return
        val targetTechnique = policy.techniqueFor(packageName) ?: return
        if (controller.isTemporaryTechniqueActive() &&
            controller.currentTechnique() == targetTechnique
        ) return
        session?.close()
        session = null
        startSession(targetTechnique)
    }

    fun clear() {
        requestedGeneration.incrementAndGet()
        session?.close()
        session = null
        foregroundPackage = null
        isClosingMenuForOverride = false
    }

    private fun startSession(targetTechnique: String) {
        if (controller.currentTechnique() == AccessTechnique.Technique.MENU) return
        TemporaryScanModeSession(controller, targetTechnique).also {
            if (it.start()) session = it
        }
    }

    private fun isGenerationCurrent(generation: Long): Boolean {
        return requestedGeneration.get() == generation
    }

    private fun isCurrent(generation: Long, packageName: String): Boolean {
        return requestedGeneration.get() == generation && foregroundPackage == packageName
    }
}
