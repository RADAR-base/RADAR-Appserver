package org.radarbase.appserver.jersey.dto.protocol

import java.util.Locale

/**
 * Data Transfer object (DTO) for LanguageText. Handles multi-language support for text.
 */
data class LanguageText(
    var en: String? = null,
    var it: String? = null,
    var nl: String? = null,
    var da: String? = null,
    var de: String? = null,
    var es: String? = null,
) {
    fun getText(languageCode: String?): String {
        return when (languageCode?.lowercase(Locale.getDefault())) {
            "en" -> en
            "it" -> it
            "nl" -> nl
            "da" -> da
            "de" -> de
            "es" -> es
            else -> ""
        } ?: ""
    }
}
