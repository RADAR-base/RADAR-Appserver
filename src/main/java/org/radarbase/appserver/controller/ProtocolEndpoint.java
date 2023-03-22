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

package org.radarbase.appserver.controller;

import java.io.IOException;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolGenerator;
import org.radarbase.appserver.config.AuthConfig.AuthEntities;
import org.radarbase.appserver.config.AuthConfig.AuthPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import radar.spring.auth.common.Authorized;
import radar.spring.auth.common.PermissionOn;

@RestController
public class ProtocolEndpoint {

  private transient ProtocolGenerator protocolGenerator;

  @Autowired
  public ProtocolEndpoint(ProtocolGenerator protocolGenerator) {
    this.protocolGenerator = protocolGenerator;
  }

  @GetMapping("/" + PathsUtil.PROTOCOL_PATH)
  @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
  public @Size(max = 100) Map<String, Protocol> getProtocols() {
    return this.protocolGenerator.getAllProtocols();
  }

  @GetMapping(
          value =
                  "/"
                          + PathsUtil.PROJECT_PATH
                          + "/"
                          + PathsUtil.PROJECT_ID_CONSTANT
                          + "/"
                          + PathsUtil.USER_PATH
                          + "/"
                          + PathsUtil.SUBJECT_ID_CONSTANT
                          + "/"
                          + PathsUtil.PROTOCOL_PATH)
  @Authorized(
    permission = AuthPermissions.READ,
    entity = AuthEntities.SUBJECT,
    permissionOn = PermissionOn.PROJECT)
  public Protocol getProtocolUsingProjectIdAndSubjectId(
          @Valid @PathVariable String projectId, @Valid @PathVariable String subjectId) {
    return this.protocolGenerator.getProtocolForSubject(subjectId);
  }

  @GetMapping(
          "/"
                  + PathsUtil.PROJECT_PATH
                  + "/"
                  + PathsUtil.PROJECT_ID_CONSTANT
                  + "/"
                  + PathsUtil.PROTOCOL_PATH)
  @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
  public Protocol getProtocolUsingProjectId(
          @Valid @PathVariable String projectId) throws IOException {
    return this.protocolGenerator.getProtocol(projectId);
  }
}
