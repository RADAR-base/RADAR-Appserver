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

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.radarbase.appserver.converter.ProjectConverter;
import org.radarbase.appserver.dto.ProjectDto;
import org.radarbase.appserver.dto.ProjectDtos;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
class ProjectServiceTest {

  @Autowired private ProjectService projectService;

  @MockBean private ProjectRepository projectRepository;

  @BeforeEach
  void setUp() {
    Project project = new Project().setProjectId("test-project");

    when(projectRepository.findByProjectId("test-project"))
        .thenReturn(Optional.of(project.setId(1L)));

    when(projectRepository.findAll()).thenReturn(List.of(project.setId(1L)));

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project.setId(1L)));

    Project projectNew = new Project().setProjectId("test-project-new");

    when(projectRepository.save(projectNew)).thenReturn(projectNew.setId(2L));

    Project projectUpdated = new Project().setProjectId("test-project-updated").setId(1L);

    when(projectRepository.save(projectUpdated)).thenReturn(projectUpdated);
  }

  @Test
  void getAllProjects() {
    ProjectDtos projectDtos = projectService.getAllProjects();

    assertEquals("test-project", projectDtos.getProjects().get(0).getProjectId());
    assertEquals(Long.valueOf(1L), projectDtos.getProjects().get(0).getId());

  }

  @Test
  void getProjectById() {
    ProjectDto projectDto = projectService.getProjectById(1L);

    assertEquals("test-project", projectDto.getProjectId());
    assertEquals(Long.valueOf(1L), projectDto.getId());
  }

  @Test
  void getProjectByProjectId() {
    ProjectDto projectDto = projectService.getProjectByProjectId("test-project");

    assertEquals("test-project", projectDto.getProjectId());
    assertEquals(Long.valueOf(1L), projectDto.getId());
  }

  @Test
  void addProject() {
    ProjectDto projectDtoNew = new ProjectDto().setProjectId("test-project-new");

    ProjectDto projectDto = projectService.addProject(projectDtoNew);

    assertEquals("test-project-new", projectDto.getProjectId());
    assertEquals(Long.valueOf(2L), projectDto.getId());

  }

  @Test
  void updateProject() {

    ProjectDto projectDtoUpdated = new ProjectDto().setProjectId("test-project-updated").setId(1L);

    ProjectDto projectDto = projectService.updateProject(projectDtoUpdated);

    assertEquals("test-project-updated", projectDto.getProjectId());
    assertEquals(Long.valueOf(1L), projectDto.getId());

  }

  @TestConfiguration
  static class ProjectServiceTestConfig {

    @Autowired private ProjectRepository projectRepository;

    private final ProjectConverter projectConverter = new ProjectConverter();

    @Bean
    public ProjectService projectServiceBeanConfig() {
      return new ProjectService(projectRepository, projectConverter);
    }
  }
}
