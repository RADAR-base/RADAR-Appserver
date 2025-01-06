/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */
package org.radarbase.appserver.service.questionnaire.protocol

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.radarbase.appserver.dto.protocol.Assessment
import org.radarbase.appserver.dto.protocol.AssessmentProtocol
import org.radarbase.appserver.dto.protocol.LanguageText
import org.radarbase.appserver.dto.protocol.Protocol
import org.radarbase.appserver.dto.protocol.ReminderTimePeriod
import org.radarbase.appserver.dto.protocol.RepeatProtocol
import org.radarbase.appserver.dto.protocol.TimePeriod
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = [GithubProtocolFetcherStrategy::class, DefaultProtocolGenerator::class]
)
class DefaultProtocolGeneratorTest {

    @Autowired
    private lateinit var protocolGenerator: DefaultProtocolGenerator

    @MockBean
    private lateinit var protocolFetcherStrategy: GithubProtocolFetcherStrategy

    @BeforeEach
    fun setUp() {
        val repeatProtocol = RepeatProtocol().apply {
            amount = 10
            unit = "day"
        }

        val timePeriod = TimePeriod().apply {
            amount = 1
            unit = "day"
        }

        val reminderTimePeriod = ReminderTimePeriod(0).apply {
            amount = 1
            unit = "day"
        }

        val assessmentProtocol = AssessmentProtocol().apply {
            completionWindow = timePeriod
            reminders = reminderTimePeriod
            this.repeatProtocol = repeatProtocol
        }

        val assessment = Assessment().apply {
            estimatedCompletionTime = 3
            name = "PHQ8"
            protocol = assessmentProtocol
            startText = LanguageText()
        }

        val protocol = Protocol().apply {
            version = "1.0"
            name = "Test"
            schemaVersion = "2.0"
            protocols = listOf(assessment)
        }

        `when`(protocolFetcherStrategy.fetchProtocols()).thenReturn(mapOf("user1" to protocol))
    }

    @Test
    fun `get all protocols`() {
        val protocol = protocolGenerator.retrieveAllProtocols()["user1"]
        assertEquals("Test", protocol?.name)
        assertEquals("1.0", protocol?.version)
        assertEquals("PHQ8", protocol?.protocols?.first()?.name)
    }

    @Test
    fun `get protocol for subject`() {
        val protocol = protocolGenerator.getProtocolForSubject("user1")
        assertEquals("Test", protocol.name)
        assertEquals("1.0", protocol.version)
        assertEquals("PHQ8", protocol.protocols?.first()?.name)
    }
}

