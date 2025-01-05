package org.radarbase.appserver.service

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@DataJpaTest
class ProjectServiceTest {

    @Autowired
    private lateinit var projectService: ProjectService

    private val projectRepository: ProjectRepository = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        val project = Project().apply {
            this.projectId = PROJECT_ID
            createdAt = Date()
            updatedAt = Date()
        }

        every { projectRepository.findByProjectId(PROJECT_ID) } returns project.apply { id = 1L }
        every { projectRepository.findAll() } returns listOf(project.apply { id = 1L })
        every { projectRepository.findByIdOrNull(1L) } returns project.apply { id = 1 }

        val projectNew = Project().apply {
            projectId = "$PROJECT_ID-new"
            createdAt = Date()
            updatedAt = Date()
        }

        every { projectRepository.save(projectNew) } returns projectNew.apply { id = 2L }

        val projectUpdated = Project().apply {
            projectId = "$PROJECT_ID-updated"
            id = 1L
            createdAt = Date()
            updatedAt = Date()
        }

        every { projectRepository.save(projectUpdated) } returns projectUpdated
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
