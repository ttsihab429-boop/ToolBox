package com.example.localization

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    BANGLA("bn", "Bangla", "বাংলা");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code == code } ?: ENGLISH
    }
}
