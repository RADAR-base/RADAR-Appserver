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

package org.radarbase.appserver.service.questionnaire.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.radarbase.appserver.dto.protocol.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = WebEnvironment.NONE,
        classes = {GithubProtocolFetcherStrategy.class, DefaultProtocolGenerator.class})
class DefaultProtocolGeneratorTest {

    @Autowired private transient DefaultProtocolGenerator protocolGenerator;

    @MockBean private transient GithubProtocolFetcherStrategy protocolFetcherStrategy;

    @BeforeEach
    void setUp() throws IOException {

        protocolGenerator.init();

        RepeatProtocol repeatProtocol = new RepeatProtocol();
        repeatProtocol.setAmount(10);
        repeatProtocol.setUnit("day");

        TimePeriod timePeriod = new TimePeriod();
        timePeriod.setAmount(1);
        timePeriod.setUnit("day");

        ReminderTimePeriod reminderTimePeriod = new ReminderTimePeriod(0);
        reminderTimePeriod.setAmount(1);
        reminderTimePeriod.setUnit("day");

        AssessmentProtocol assessmentProtocol = new AssessmentProtocol();
        assessmentProtocol.setCompletionWindow(timePeriod);
        assessmentProtocol.setReminders(reminderTimePeriod);
        assessmentProtocol.setRepeatProtocol(repeatProtocol);

        Assessment assessment = new Assessment();
        assessment.setEstimatedCompletionTime(3);
        assessment.setName("PHQ8");
        assessment.setProtocol(assessmentProtocol);
        assessment.setStartText(new LanguageText());

        Protocol protocol = new Protocol();
        protocol.setVersion("1.0");
        protocol.setName("Test");
        protocol.setSchemaVersion("2.0");
        protocol.setProtocols(List.of(assessment));

        when(protocolFetcherStrategy.fetchProtocols()).thenReturn(Map.of("user1", protocol));
    }

    @Test
    void getAllProtocols() {

        Protocol protocol = protocolGenerator.getAllProtocols().get("user1");
        assertEquals("Test", protocol.getName());
        assertEquals("1.0", protocol.getVersion());
        assertEquals("PHQ8", protocol.getProtocols().get(0).getName());
    }

    @Test
    void getProtocol() {

        Protocol protocol = protocolGenerator.getProtocolForSubject("user1");
        assertEquals("Test", protocol.getName());
        assertEquals("1.0", protocol.getVersion());
        assertEquals("PHQ8", protocol.getProtocols().get(0).getName());
    }
}
