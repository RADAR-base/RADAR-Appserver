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

package org.radarbase.appserver.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.radarbase.appserver.dto.ProjectDto;
/**
 * {@link Entity} for persisting projects. The corresponding DTO is {@link ProjectDto}.
 *
 * @author yatharthranjan
 */
@Table(name = "projects")
@Entity
@Getter
@ToString
@NoArgsConstructor
public class Project extends AuditModel implements Serializable {

  private static final long serialVersionUID = 12312466855464L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotNull
  @Column(name = "project_id", unique = true, nullable = false)
  private String projectId;

  public Project setId(Long id) {
    this.id = id;
    return this;
  }

  public Project setProjectId(String projectId) {
    this.projectId = projectId;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Project)) {
      return false;
    }
    Project project = (Project) o;
    return Objects.equals(getProjectId(), project.getProjectId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getProjectId());
  }
}
