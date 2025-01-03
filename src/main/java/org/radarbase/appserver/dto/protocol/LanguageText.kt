/*
 *  Copyright 2018 King's College London
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.radarbase.appserver.dto.protocol

import java.util.*

/**
 * Data Transfer object (DTO) for LanguageText. Handles multi-language support for text.
 *
 * @author yatharthranjan
 */
data class LanguageText(
    var en: String? = null,
    var it: String? = null,
    var nl: String? = null,
    var da: String? = null,
    var de: String? = null,
    var es: String? = null
) {
    fun getText(languageCode: String): String {
        return when (languageCode.lowercase(Locale.getDefault())) {
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