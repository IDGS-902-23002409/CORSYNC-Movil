package com.sakura.aura.ui.theme


import androidx.compose.runtime.compositionLocalOf
import com.sakura.aura.utils.ThemeViewModel

val LocalThemeViewModel = compositionLocalOf<ThemeViewModel> {
    error("ThemeViewModel no fue provisto. Asegúrate de envolverlo en CompositionLocalProvider.")
}