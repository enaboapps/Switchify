package com.enaboapps.switchify.service.menu.menus.system

import android.content.Context
import android.media.AudioManager
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu

class VolumeControlMenu(private val accessibilityService: SwitchifyAccessibilityService) : BaseMenu(accessibilityService, buildVolumeControlMenuItems(accessibilityService)) {

    companion object {
        private fun buildVolumeControlMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            val audioManager = accessibilityService.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return listOf(
                MenuItem("Volume Up", closeOnSelect = false, action = {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_ACCESSIBILITY, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                }),
                MenuItem("Volume Down", closeOnSelect = false, action = {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_ACCESSIBILITY, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                })
            )
        }
    }
}