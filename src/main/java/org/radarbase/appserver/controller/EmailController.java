/*
 *
 *  *  Copyright 2024 The Hyve
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package org.radarbase.appserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.config.AuthConfig.AuthEntities;
import org.radarbase.appserver.config.AuthConfig.AuthPermissions;
import org.radarbase.appserver.controller.model.SendEmailRequest;
import org.radarbase.appserver.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import radar.spring.auth.common.Authorized;
import radar.spring.auth.common.PermissionOn;

import java.util.concurrent.CompletableFuture;

/**
 * Endpoint for sending emails from app on behalf of the study subject.
 *
 * @author Pim van Nierop
 */
@CrossOrigin
@RestController
@ConditionalOnProperty(value = "send-email.enabled", havingValue = "true")
@Slf4j
public class EmailController {

  @Autowired
  private EmailService emailService;

  @Authorized(
      permission = AuthPermissions.UPDATE,
      entity = AuthEntities.SUBJECT,
      permissionOn = PermissionOn.SUBJECT
  )
  @PostMapping(
      "/email/" + PathsUtil.PROJECT_PATH + "/" + PathsUtil.PROJECT_ID_CONSTANT +
      "/" + PathsUtil.USER_PATH + "/" + PathsUtil.SUBJECT_ID_CONSTANT)
  public CompletableFuture<Boolean> subjectSendEmail(
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @RequestBody SendEmailRequest sendEmailRequest) {

    log.info("Sending email for project: {}, subject: {}", projectId, subjectId);

    return emailService.send(sendEmailRequest);
  }

}