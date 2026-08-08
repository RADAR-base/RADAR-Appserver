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

package org.radarbase.appserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Data Transfer object (DTO) for projects. A project may represent a Management Portal project.
 *
 * @see <a href="https://github.com/RADAR-base/ManagementPortal">Management Portal</a>
 * @author yatharthranjan
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDto implements Serializable {

  private static final long serialVersionUID = 2L;

  private Long id;

  private String projectId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant updatedAt;

  public Long getId() {
    return id;
  }

  public String getProjectId() {
    return projectId;
  }

  public ProjectDto setId(Long id) {
    this.id = id;
    return this;
  }

  public ProjectDto setProjectId(String projectId) {
    this.projectId = projectId;
    return this;
  }

  public ProjectDto setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public ProjectDto setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }
}
