package com.enaboapps.switchify

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.enaboapps.switchify.utils.AppLauncher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppLauncherTest {
    @Test
    fun testAppLaunchByDisplayName() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val appLauncher = AppLauncher(context)
        appLauncher.launchAppByDisplayName("Settings")
    }

    @Test
    fun testFindPackageNameByDisplayName() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val appLauncher = AppLauncher(context)
        val packageName = appLauncher.findPackageNameByDisplayName("Settings")
        assert(packageName != null)
    }

    @Test
    fun testLaunchAppByPackageName() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val appLauncher = AppLauncher(context)
        appLauncher.launchAppByPackageName("com.android.settings")
    }
}