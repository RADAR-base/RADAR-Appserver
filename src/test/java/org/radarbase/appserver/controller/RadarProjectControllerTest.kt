/*
 * Copyright 2018 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.radarbase.appserver.dto.ProjectDto
import org.radarbase.appserver.dto.ProjectDtos
import org.radarbase.appserver.service.ProjectService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI

@ExtendWith(SpringExtension::class)
@WebMvcTest(RadarProjectController::class)
@AutoConfigureMockMvc(addFilters = false)
class RadarProjectControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var projectService: ProjectService

    companion object {
        private const val PROJECT_ID_JSON_PATH = "$.projectId"
        const val ID_JSON_PATH = "$.id"
        const val PROJECT_ID = "test-project"
    }

    @BeforeEach
    fun setUp() {
        val projectDto = ProjectDto().apply {
            projectId = PROJECT_ID
            id = 1L
        }

        given(projectService.getAllProjects())
            .willReturn(ProjectDtos().withProjects(listOf(projectDto)))

        given(projectService.getProjectById(1L)).willReturn(projectDto)

        given(projectService.getProjectByProjectId(PROJECT_ID)).willReturn(projectDto)

        val projectDtoNew = ProjectDto().apply { projectId = "$PROJECT_ID-new" }

        given(projectService.addProject(projectDtoNew)).willReturn(projectDtoNew.apply { id = 2L })

        val projectDtoUpdated = ProjectDto().apply {
            projectId = "$PROJECT_ID-updated"
            id = 1L
        }

        given(projectService.updateProject(projectDtoUpdated)).willReturn(projectDtoUpdated)
    }

    @Test
    fun addProject() {
        val projectDtoNew = ProjectDto().apply {
            projectId = "$PROJECT_ID-new"
            id = 2L
        }

        mockMvc.perform(
            MockMvcRequestBuilders.post(URI("/projects"))
                .content(objectMapper.writeValueAsString(projectDtoNew))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath(PROJECT_ID_JSON_PATH, `is`("$PROJECT_ID-new")))
            .andExpect(jsonPath(ID_JSON_PATH, `is`(2)))
    }

    @Test
    fun updateProject() {
        val projectDtoUpdated = ProjectDto().apply {
            id = 1L
            projectId = "$PROJECT_ID-updated"
        }

        mockMvc.perform(
            MockMvcRequestBuilders.put(URI("/projects/$PROJECT_ID"))
                .content(objectMapper.writeValueAsString(projectDtoUpdated))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(PROJECT_ID_JSON_PATH, `is`("$PROJECT_ID-updated")))
            .andExpect(jsonPath(ID_JSON_PATH, `is`(1)))
    }

    @Test
    fun getAllProjects() {
        mockMvc.perform(
            MockMvcRequestBuilders.get(URI("/projects"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.projects[0].projectId", `is`(PROJECT_ID)))
            .andExpect(jsonPath("$.projects[0].id", `is`(1)))
    }

    @Test
    fun getProjectsUsingId() {
        mockMvc.perform(
            MockMvcRequestBuilders.get(URI("/projects/project?id=1"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(PROJECT_ID_JSON_PATH, `is`(PROJECT_ID)))
            .andExpect(jsonPath(ID_JSON_PATH, `is`(1)))
    }

    @Test
    fun getProjectsUsingProjectId() {
        mockMvc.perform(
            MockMvcRequestBuilders.get(URI("/projects/$PROJECT_ID"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(PROJECT_ID_JSON_PATH, `is`(PROJECT_ID)))
            .andExpect(jsonPath(ID_JSON_PATH, `is`(1)))
    }
}
