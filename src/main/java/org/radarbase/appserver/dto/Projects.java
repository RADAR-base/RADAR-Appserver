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

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Wrapper for a list of {@link ProjectDto} objects.
 *
 * @author yatharthranjan
 */
@Data
public class Projects {

  private List<ProjectDto> projects;

  public Projects() {
    this.projects = new ArrayList<>();
  }

  public List<ProjectDto> getProjects() {
    return projects;
  }

  public Projects setProjects(List<ProjectDto> projects) {
    this.projects = projects;
    return this;
  }

  public Projects addProject(ProjectDto project) {
    this.projects.add(project);
    return this;
  }
}
