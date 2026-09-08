/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.jersey.dto.protocol

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * Data Transfer object (DTO) for Assessment. A project may represent a Protocol for scheduling
 * questionnaires.
 *
 * @see <a href="https://github.com/RADAR-base/RADAR-aRMT-protocols">aRMT Protocols</a>
 * @see Protocol
 */

@Serializable
data class Assessment(
    var name: String? = null,
    @SerialName("type") @JsonIgnore private var _type: AssessmentType? = null,
    @Serializable(with = ShowIntroductionSerializer::class)
    var showIntroduction: ShowIntroduction = ShowIntroduction.NEVER,
    var questionnaire: DefinitionInfo? = null,
    var startText: LanguageText? = null,
    var endText: LanguageText? = null,
    var warn: LanguageText? = null,
    var estimatedCompletionTime: Int? = null,
    var protocol: AssessmentProtocol? = null,
    @Column(name = "\"order\"")
    var order: Int = 0,
    var nQuestions: Int = 0,
    var showInCalendar: Boolean = true,
    var isDemo: Boolean = false,
) {
    var type: AssessmentType?
        @JsonProperty("type") get() {
            return _type
                ?: if (protocol?.clinicalProtocol != null) AssessmentType.CLINICAL else AssessmentType.SCHEDULED
        }
        set(value) {
            _type = value
        }
}

enum class ShowIntroduction {
    ALWAYS,
    ONCE,
    NEVER,
}

internal object ShowIntroductionSerializer : KSerializer<ShowIntroduction> {
    override val descriptor = PrimitiveSerialDescriptor("ShowIntroduction", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ShowIntroduction {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return ShowIntroduction.valueOf(decoder.decodeString().uppercase())
        val element = jsonDecoder.decodeJsonElement() as? JsonPrimitive
            ?: return ShowIntroduction.NEVER
        val bool = element.booleanOrNull
        if (bool != null) {
            return if (bool) ShowIntroduction.ONCE else ShowIntroduction.NEVER
        }
        return when (element.content) {
            "always" -> ShowIntroduction.ALWAYS
            "once" -> ShowIntroduction.ONCE
            "never" -> ShowIntroduction.NEVER
            else -> ShowIntroduction.NEVER
        }
    }

    override fun serialize(encoder: Encoder, value: ShowIntroduction) {
        encoder.encodeString(value.name.lowercase())
    }
}
