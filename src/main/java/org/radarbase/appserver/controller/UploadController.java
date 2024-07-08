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
import org.radarbase.appserver.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import radar.spring.auth.common.Authorized;
import radar.spring.auth.common.PermissionOn;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Resource Endpoint for uploading assets to a data store.
 *
 * @author Pim van Nierop
 */
@CrossOrigin
@RestController
@ConditionalOnProperty(value = "radar.file-upload.enabled", havingValue = "true")
@Slf4j
public class UploadController {

  @Autowired
  private transient StorageService storageService;

  @Authorized(
      permission = AuthPermissions.CREATE,
      entity = AuthEntities.MEASUREMENT,
      permissionOn = PermissionOn.SUBJECT
  )
  @PostMapping(
      "/" + PathsUtil.PROJECT_PATH + "/" + PathsUtil.PROJECT_ID_CONSTANT +
      "/" + PathsUtil.USER_PATH + "/" + PathsUtil.SUBJECT_ID_CONSTANT +
      "/" + PathsUtil.FILE_PATH +
      "/" + PathsUtil.TOPIC_PATH + "/" + PathsUtil.TOPIC_ID_CONSTANT +
      "/upload")
  public ResponseEntity<?> subjectFileUpload(
      @RequestParam("file") MultipartFile file,
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @PathVariable String topicId) throws URISyntaxException {

    log.info("Storing file for project: {}, subject: {}, topic: {}", projectId, subjectId, topicId);

    String filePath = storageService.store(file, projectId, subjectId, topicId);
    return ResponseEntity.created(new URI(filePath)).build();
  }

}
