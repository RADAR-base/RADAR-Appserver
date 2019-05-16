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

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.radarbase.appserver.dto.ProjectDto;
import org.radarbase.appserver.dto.ProjectDtos;
import org.radarbase.appserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(RadarProjectController.class)
class RadarProjectControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private ProjectService projectService;

  @BeforeEach
  void setUp() {

    ProjectDto projectDto = new ProjectDto().setProjectId("test-project").setId(1L);

    given(projectService.getAllProjects())
        .willReturn(new ProjectDtos().setProjects(List.of(projectDto)));

    given(projectService.getProjectById(1L)).willReturn(projectDto);

    given(projectService.getProjectByProjectId("test-project")).willReturn(projectDto);

    ProjectDto projectDtoNew = new ProjectDto().setProjectId("test-project-new");

    given(projectService.addProject(projectDtoNew)).willReturn(projectDtoNew.setId(2L));

    ProjectDto projectDtoUpdated = new ProjectDto().setProjectId("test-project-updated").setId(1L);

    given(projectService.updateProject(projectDtoUpdated)).willReturn(projectDtoUpdated);
  }

  @Test
  void addProject() throws Exception {
    ProjectDto projectDtoNew = new ProjectDto().setProjectId("test-project-new").setId(2L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(new URI("/projects"))
                .content(objectMapper.writeValueAsString(projectDtoNew))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.projectId", is("test-project-new")))
        .andExpect(jsonPath("$.id", is(2)));
  }

  @Test
  void updateProject() throws Exception {
    ProjectDto projectDtoUpdated = new ProjectDto().setId(1L).setProjectId("test-project-updated");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(new URI("/projects"))
                .content(objectMapper.writeValueAsString(projectDtoUpdated))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.projectId", is("test-project-updated")))
        .andExpect(jsonPath("$.id", is(1)));

  }

  @Test
  void getAllProjects() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(new URI("/projects")))
        .andExpect(jsonPath("$.projects[0].projectId", is("test-project")))
        .andExpect(jsonPath("$.projects[0].id", is(1)));
  }

  @Test
  void getProjectsUsingId() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(new URI("/projects/project?id=1")))
        .andExpect(jsonPath("$.projectId", is("test-project")))
        .andExpect(jsonPath("$.id", is(1)));

  }

  @Test
  void getProjectsUsingProjectId() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(new URI("/projects/test-project")))
        .andExpect(jsonPath("$.projectId", is("test-project")))
        .andExpect(jsonPath("$.id", is(1)));
  }
}
