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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.radarbase.appserver.controller.FcmNotificationControllerTest.PROJECT_ID;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.radarbase.appserver.converter.ProjectConverter;
import org.radarbase.appserver.dto.ProjectDTO;
import org.radarbase.appserver.dto.ProjectDTOs;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Disabled
@ExtendWith(SpringExtension.class)
@DataJpaTest
class ProjectServiceTest {

  @Autowired private transient ProjectService projectService;

  @MockBean private transient ProjectRepository projectRepository;

  @BeforeEach
  void setUp() {
    Project project = new Project().setProjectId(PROJECT_ID);
    project.setCreatedAt(new Date());
    project.setUpdatedAt(new Date());

    when(projectRepository.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(project.setId(1L)));

    when(projectRepository.findAll()).thenReturn(List.of(project.setId(1L)));

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project.setId(1L)));

    Project projectNew = new Project().setProjectId(PROJECT_ID + "-new");
    projectNew.setCreatedAt(new Date());
    projectNew.setUpdatedAt(new Date());

    when(projectRepository.save(projectNew)).thenReturn(projectNew.setId(2L));

    Project projectUpdated = new Project().setProjectId(PROJECT_ID + "-updated").setId(1L);
    projectUpdated.setCreatedAt(new Date());
    projectUpdated.setUpdatedAt(new Date());

    when(projectRepository.save(projectUpdated)).thenReturn(projectUpdated);
  }

  @Test
  void getAllProjects() {
    ProjectDtos projectDtos = projectService.getAllProjects();

    assertEquals(PROJECT_ID, projectDtos.getProjects().get(0).getProjectId());
    assertEquals(Long.valueOf(1L), projectDtos.getProjects().get(0).getId());
  }

  @Test
  void getProjectById() {
    ProjectDto projectDto = projectService.getProjectById(1L);

    assertEquals(PROJECT_ID, projectDto.getProjectId());
    assertEquals(Long.valueOf(1L), projectDto.getId());
  }

  @Test
  void getProjectByProjectId() {
    ProjectDto projectDto = projectService.getProjectByProjectId(PROJECT_ID);

    assertEquals(PROJECT_ID, projectDto.getProjectId());
    assertEquals(Long.valueOf(1L), projectDto.getId());
  }

  @Test
  void addProject() {
    ProjectDto projectDtoNew = new ProjectDto().setProjectId(PROJECT_ID + "-new");

    ProjectDto projectDto = projectService.addProject(projectDtoNew);

    assertEquals(PROJECT_ID + "-new", projectDto.getProjectId());
    assertEquals(Long.valueOf(2L), projectDto.getId());
  }

  @Test
  void updateProject() {

    ProjectDto projectDtoUpdated = new ProjectDto().setProjectId(PROJECT_ID + "-updated").setId(1L);

    ProjectDto projectDto = projectService.updateProject(projectDtoUpdated);

    assertEquals(PROJECT_ID + "-updated", projectDto.getProjectId());
    assertEquals(Long.valueOf(1L), projectDto.getId());
  }

  @TestConfiguration
  static class ProjectServiceTestConfig {

    @Autowired private transient ProjectRepository projectRepository;

    private final transient ProjectConverter projectConverter = new ProjectConverter();

    @Bean
    public ProjectService projectServiceBeanConfig() {
      return new ProjectService(projectRepository, projectConverter);
    }
  }
}
