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

package org.radarbase.appserver.service;

import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.protocol.ProtocolGenerator;
import org.radarbase.appserver.util.CachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuestionnaireScheduleService {

  private transient ProtocolGenerator protocolGenerator;

  private CachedMap<String, Schedule> subjectScheduleMap;

  @Autowired
  public QuestionnaireScheduleService(ProtocolGenerator protocolGenerator) {
    this.protocolGenerator = protocolGenerator;
    protocolGenerator.init();
    // log.info(String.valueOf(protocolGenerator.getProtocol("RADAR-MDD-KCL-s1").toString()));
  }

  // Use cached map of schedule of user
  public void getProtocolForProject(String projectId) {}

  public Schedule getScheduleForUser(User user) {
    return null;
  }

  public void generateScheduleForUser(User user) {}
}
