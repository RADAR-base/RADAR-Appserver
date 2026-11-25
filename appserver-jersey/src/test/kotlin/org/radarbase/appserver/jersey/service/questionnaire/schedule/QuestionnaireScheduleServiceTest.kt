package org.radarbase.appserver.jersey.service.questionnaire.schedule

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.event.state.TaskState
import org.radarbase.appserver.jersey.service.FcmNotificationService
import org.radarbase.appserver.jersey.service.TaskService
import java.sql.Timestamp
import java.time.Instant

class QuestionnaireScheduleServiceTest {
    @Test
    fun `regenerating reminders uses the saved occurrence and skips completed and overdue reminders`() = runBlocking {
        val user = User()
        val tasks = mockk<TaskService>()
        val notifications = mockk<FcmNotificationService>()
        val service = QuestionnaireScheduleService(
            mockk(), mockk(), mockk(), mockk(), tasks, notifications, mockk(relaxed = true), mockk(),
        )
        val now = Instant.now()
        fun occurrence(time: Instant) = Task().apply {
            name = "Survey"
            timestamp = Timestamp.from(time)
        }
        fun reminder(occurrence: Task, time: Instant) = Notification().apply {
            task = occurrence
            scheduledTime = time
        }

        val previousTime = now.minusSeconds(86400)
        val previous = occurrence(previousTime)
        val current = occurrence(now)
        val savedPrevious = occurrence(previousTime).apply {
            id = 1
            status = TaskState.COMPLETED
        }
        val savedCurrent = occurrence(now).apply { id = 2 }
        val future = now.plusSeconds(3600)
        val initial = reminder(current, future)
        val upcoming = reminder(current, future.plusSeconds(3600))
        val schedule = AssessmentSchedule(
            tasks = listOf(previous, current),
            notifications = listOf(initial),
            reminders = listOf(reminder(previous, future), reminder(current, previousTime), upcoming),
        )
        coEvery { tasks.addTasks(any(), user) } returns emptyList()
        coEvery { tasks.getTasksByUser(user) } returns listOf(savedPrevious, savedCurrent)
        val batches = mutableListOf<List<Notification>>()
        coEvery { notifications.addNotifications(capture(batches), user) } returns emptyList()

        service.saveTasksAndNotifications(user, listOf(schedule))

        assertEquals(listOf(1, 1), batches.map { it.size })
        assertSame(initial, batches[0].single())
        assertSame(upcoming, batches[1].single())
        assertSame(savedCurrent, initial.task)
        assertSame(savedCurrent, upcoming.task)
    }
}
