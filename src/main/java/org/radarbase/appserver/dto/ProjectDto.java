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

import java.io.Serializable;
import lombok.Data;

/**
 * Data Transfer object (DTO) for projects. A project may represent a Management Portal project.
 *
 * @see <a href="https://github.com/RADAR-base/ManagementPortal">Management Portal</a>
 * @author yatharthranjan
 */
@Data
public class ProjectDto implements Serializable {

  // TODO add updated and created at

  private static final long serialVersionUID = 2L;

  private Long id;

  private String projectId;

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
}
