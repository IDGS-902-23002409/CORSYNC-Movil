package com.sakura.aura.utils

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel : ViewModel() {

    private val _isLightTheme = MutableStateFlow(false)
    val isLightTheme: StateFlow<Boolean> = _isLightTheme.asStateFlow()

    fun toggleTheme() {
        _isLightTheme.value = !_isLightTheme.value
    }

    fun setLight() { _isLightTheme.value = true  }
    fun setDark()  { _isLightTheme.value = false }
}