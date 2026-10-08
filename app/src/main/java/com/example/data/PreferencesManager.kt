package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("toolbox_prefs", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString(KEY_LANG, AppLanguage.ENGLISH.code) ?: "en")
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(
        prefs.getString(KEY_THEME, "dark") ?: "dark"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_HAPTIC, true)
    )
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _animationsEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_ANIMATIONS, true)
    )
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()

    private val _trackRecents = MutableStateFlow(
        prefs.getBoolean(KEY_TRACK_RECENTS, true)
    )
    val trackRecents: StateFlow<Boolean> = _trackRecents.asStateFlow()

    private val _favorites = MutableStateFlow(
        prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    )
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _recentToolIds = MutableStateFlow(
        loadRecents()
    )
    val recentToolIds: StateFlow<List<String>> = _recentToolIds.asStateFlow()

    private val _memoContent = MutableStateFlow(
        prefs.getString(KEY_MEMO, "") ?: ""
    )
    val memoContent: StateFlow<String> = _memoContent.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString(KEY_LANG, lang.code).apply()
        _language.value = lang
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME, mode).apply()
        _themeMode.value = mode
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _hapticEnabled.value = enabled
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
        _animationsEnabled.value = enabled
    }

    fun setTrackRecents(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TRACK_RECENTS, enabled).apply()
        _trackRecents.value = enabled
    }

    fun toggleFavorite(toolId: String) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(toolId)) {
            current.remove(toolId)
        } else {
            current.add(toolId)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        _favorites.value = current
    }

    fun isFavorite(toolId: String): Boolean =
        _favorites.value.contains(toolId)

    fun recordToolUsed(toolId: String) {
        if (!_trackRecents.value) return
        val current = _recentToolIds.value.toMutableList()
        current.remove(toolId)
        current.add(0, toolId)
        if (current.size > 20) {
            current.removeAt(current.lastIndex)
        }
        val serialized = current.joinToString(",")
        prefs.edit().putString(KEY_RECENTS, serialized).apply()
        _recentToolIds.value = current
    }

    fun clearRecents() {
        prefs.edit().remove(KEY_RECENTS).apply()
        _recentToolIds.value = emptyList()
    }

    fun clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply()
        _favorites.value = emptySet()
    }

    fun saveMemo(text: String) {
        prefs.edit().putString(KEY_MEMO, text).apply()
        _memoContent.value = text
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        _favorites.value = emptySet()
        _recentToolIds.value = emptyList()
        _memoContent.value = ""
        _themeMode.value = "dark"
        _language.value = AppLanguage.ENGLISH
        _hapticEnabled.value = true
        _animationsEnabled.value = true
        _trackRecents.value = true
    }

    private fun loadRecents(): List<String> {
        val raw = prefs.getString(KEY_RECENTS, null) ?: return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    companion object {
        private const val KEY_LANG = "key_lang"
        private const val KEY_THEME = "key_theme"
        private const val KEY_HAPTIC = "key_haptic"
        private const val KEY_ANIMATIONS = "key_animations"
        private const val KEY_TRACK_RECENTS = "key_track_recents"
        private const val KEY_FAVORITES = "key_favorites"
        private const val KEY_RECENTS = "key_recents"
        private const val KEY_MEMO = "key_memo"
    }
}
