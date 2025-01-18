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

package org.radarbase.appserver.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.radarbase.appserver.dto.ProjectDto
import org.radarbase.appserver.dto.ProjectDtos
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.mapper.ProjectMapper
import org.radarbase.appserver.repository.ProjectRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@DataJpaTest
class ProjectServiceTest {

    @Autowired
    private lateinit var projectService: ProjectService

    @MockBean
    private lateinit var projectRepository: ProjectRepository

    @BeforeEach
    fun setUp() {
        val project = Project().apply {
            this.projectId = PROJECT_ID
            createdAt = Date()
            updatedAt = Date()
        }

        Mockito.`when`(projectRepository.findByProjectId(PROJECT_ID)).thenReturn(project.apply { id = 1L })
        Mockito.`when`(projectRepository.findAll()).thenReturn(listOf(project.apply { id = 1L }))
        Mockito.`when`(projectRepository.findById(1L)).thenReturn(Optional.of(project.apply { id = 1L }))

        val projectNew = Project().apply {
            projectId = "$PROJECT_ID-new"
            createdAt = Date()
            updatedAt = Date()
        }

        Mockito.`when`(projectRepository.saveAndFlush(projectNew)).thenReturn(projectNew.apply { id = 2L })

        val projectUpdated = Project().apply {
            projectId = "$PROJECT_ID-updated"
            id = 1L
            createdAt = Date()
            updatedAt = Date()
        }

        Mockito.`when`(projectRepository.save(projectUpdated)).thenReturn(projectUpdated)
    }

    @Test
    fun getAllProjects() {
        val projectDtos: ProjectDtos = projectService.getAllProjects()


        println("Checkpoint project: $projectDtos")
        assertEquals(PROJECT_ID, projectDtos.projects[0].projectId)
        assertEquals(1L, projectDtos.projects[0].id)
    }

    @Test
    fun getProjectById() {
        val projectDto: ProjectDto = projectService.getProjectById(1L)

        assertEquals(PROJECT_ID, projectDto.projectId)
        assertEquals(1L, projectDto.id)
    }

    @Test
    fun getProjectByProjectId() {
        val projectDto: ProjectDto = projectService.getProjectByProjectId(PROJECT_ID)

        assertEquals(PROJECT_ID, projectDto.projectId)
        assertEquals(1L, projectDto.id)
    }

    @Test
    fun addProject() {
        val projectDtoNew = ProjectDto().apply { projectId = "$PROJECT_ID-new" }

        val projectDto: ProjectDto = projectService.addProject(projectDtoNew)

        assertEquals("$PROJECT_ID-new", projectDto.projectId)
        assertEquals(2L, projectDto.id)
    }

    @Test
    fun updateProject() {
        val projectDtoUpdated = ProjectDto().apply {
            projectId = "$PROJECT_ID-updated"
            id = 1L
        }

        val projectDto: ProjectDto = projectService.updateProject(projectDtoUpdated)

        assertEquals("$PROJECT_ID-updated", projectDto.projectId)
        assertEquals(1L, projectDto.id)
    }

    @TestConfiguration
    class ProjectServiceTestConfig {

        @Autowired
        private lateinit var projectRepository: ProjectRepository

        private val projectMapper = ProjectMapper()

        @Bean
        fun projectServiceBeanConfig(): ProjectService {
            return ProjectService(projectRepository, projectMapper)
        }
    }

    companion object {
        private const val PROJECT_ID = "PROJECT_ID"
    }
}
