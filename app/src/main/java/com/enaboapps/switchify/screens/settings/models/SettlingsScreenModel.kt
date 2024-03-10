package com.enaboapps.switchify.screens.settings.models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enaboapps.switchify.preferences.PreferenceManager
import kotlinx.coroutines.launch

class SettingsScreenModel(context: Context) : ViewModel() {
    private val preferenceManager = PreferenceManager(context)

    // Initialize MutableLiveData with initial values from PreferenceManager
    private val _scanRate = MutableLiveData<Long>().apply {
        value = preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
    }
    val scanRate: LiveData<Long> = _scanRate

    private val _refineScanRate = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_REFINE_SCAN_RATE)
    }
    val refineScanRate: LiveData<Long> = _refineScanRate

    private val _pauseOnFirstItem = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM)
    }
    val pauseOnFirstItem: LiveData<Boolean> = _pauseOnFirstItem

    private val _pauseOnFirstItemDelay = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM_DELAY)
    }
    val pauseOnFirstItemDelay: LiveData<Long> = _pauseOnFirstItemDelay

    private val _autoSelect = MutableLiveData<Boolean>().apply {
        value = preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT)
    }
    val autoSelect: LiveData<Boolean> = _autoSelect

    private val _autoSelectDelay = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY)
    }
    val autoSelectDelay: LiveData<Long> = _autoSelectDelay

    private val _assistedSelection = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_ASSISTED_SELECTION)
    }
    val assistedSelection: LiveData<Boolean> = _assistedSelection

    private val _restrictCursorToKeyboard = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_RESTRICT_CURSOR_TO_KEYBOARD)
    }
    val restrictCursorToKeyboard: LiveData<Boolean> = _restrictCursorToKeyboard

    private val _rowColumnScan = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_ROW_COLUMN_SCAN)
    }
    val rowColumnScan: LiveData<Boolean> = _rowColumnScan


    private val pauseScanOnSwitchHoldThreshold: Long = 400


    // Update methods now update MutableLiveData which in turn updates the UI
    fun setScanRate(rate: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE, rate)
            _scanRate.postValue(rate)
        }
        // If rate < pauseScanOnSwitchHoldThreshold, set pauseScanOnSwitchHold to true
        if (rate < pauseScanOnSwitchHoldThreshold) {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD,
                true
            )
        }
    }

    fun setRefineScanRate(rate: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(
                PreferenceManager.Keys.PREFERENCE_KEY_REFINE_SCAN_RATE,
                rate
            )
            _refineScanRate.postValue(rate)
        }
        // If rate < pauseScanOnSwitchHoldThreshold, set pauseScanOnSwitchHold to true
        if (rate < pauseScanOnSwitchHoldThreshold) {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD,
                true
            )
        }
    }

    fun setPauseOnFirstItem(pause: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM,
                pause
            )
            _pauseOnFirstItem.postValue(pause)
        }
    }

    fun setPauseOnFirstItemDelay(delay: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(
                PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM_DELAY,
                delay
            )
            _pauseOnFirstItemDelay.postValue(delay)
        }
    }

    fun setAutoSelect(autoSelect: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT,
                autoSelect
            )
            _autoSelect.postValue(autoSelect)
        }
    }

    fun setAutoSelectDelay(delay: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(
                PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY,
                delay
            )
            _autoSelectDelay.postValue(delay)
        }
    }

    fun setAssistedSelection(assistedSelection: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_ASSISTED_SELECTION,
                assistedSelection
            )
            _assistedSelection.postValue(assistedSelection)
        }
    }

    fun setRestrictCursorToKeyboard(restrictCursorToKeyboard: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_RESTRICT_CURSOR_TO_KEYBOARD,
                restrictCursorToKeyboard
            )
            _restrictCursorToKeyboard.postValue(restrictCursorToKeyboard)
        }
    }

    fun setRowColumnScan(rowColumnScan: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_ROW_COLUMN_SCAN,
                rowColumnScan
            )
            _rowColumnScan.postValue(rowColumnScan)
        }
    }
}