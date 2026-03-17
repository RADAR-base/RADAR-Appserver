/*
 * Copyright 2025 King's College London
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

package org.radarbase.appserver.microservices.core.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.radarbase.appserver.microservices.core.dto.protocol.AssessmentType
import org.radarbase.appserver.microservices.core.event.state.TaskState
import java.sql.Timestamp
import java.util.Objects

@Entity
@Table(name = "tasks")
class Task : AuditModel() {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null

    @field:NotNull
    var completed: Boolean? = null

    @field:NotNull
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    var timestamp: Timestamp? = null

    @field:NotNull
    var name: String? = null

    @field:NotNull
    @Enumerated(EnumType.STRING)
    var type: AssessmentType? = null

    var estimatedCompletionTime = 0

    @field:NotNull
    var completionWindow: Long? = null

    var warning: String? = null

    var isClinical: Boolean? = null

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    var timeCompleted: Timestamp? = null

    @field:NotNull
    var showInCalendar: Boolean? = null

    @field:NotNull
    var isDemo: Boolean? = null

    @field:NotNull
    var priority: Int = 0

    @field:NotNull
    var nQuestions: Int = 0

    @field:NotNull
    @JsonIgnore
    @Column(name = "user_id", nullable = false)
    var userId: Long? = null

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TaskState = TaskState.UNKNOWN

    class TaskBuilder(task: Task? = null) {

        var id: Long? = task?.id

        var completed: Boolean? = task?.completed == true

        @field:NotNull
        var timestamp: Timestamp? = task?.timestamp

        var name: String? = task?.name

        var type: AssessmentType? = task?.type

        var estimatedCompletionTime: Int = task?.estimatedCompletionTime ?: 0

        var completionWindow: Long? = task?.completionWindow

        var warning: String? = task?.warning

        var isClinical: Boolean? = task?.isClinical == true

        var timeCompleted: Timestamp? = task?.timeCompleted

        var showInCalendar: Boolean? = task?.showInCalendar != false

        var isDemo: Boolean? = task?.isDemo == true

        var priority: Int = task?.priority ?: 0

        var nQuestions: Int = task?.nQuestions ?: 0

        var userId: Long? = task?.userId

        fun id(id: Long?) = apply {
            this.id = id
        }

        fun completed(completed: Boolean?) = apply {
            this.completed = completed
        }

        fun timestamp(timestamp: Timestamp?) = apply {
            this.timestamp = timestamp
        }

        fun timestamp(timestamp: java.time.Instant?) = apply {
            this.timestamp = timestamp?.let {
                Timestamp.from(it)
            }
        }

        fun name(name: String?) = apply {
            this.name = name
        }

        fun type(type: AssessmentType?) = apply {
            this.type = type
        }

        fun estimatedCompletionTime(estimatedCompletionTime: Int) = apply {
            this.estimatedCompletionTime = estimatedCompletionTime
        }

        fun completionWindow(completionWindow: Long?) = apply {
            this.completionWindow = completionWindow
        }

        fun warning(warning: String?) = apply {
            this.warning = warning
        }

        fun isClinical(isClinical: Boolean?) = apply {
            this.isClinical = isClinical
        }

        fun timeCompleted(timeCompleted: Timestamp?) = apply {
            this.timeCompleted = timeCompleted
        }

        fun showInCalendar(showInCalendar: Boolean?) = apply {
            this.showInCalendar = showInCalendar
        }

        fun isDemo(isDemo: Boolean?) = apply {
            this.isDemo = isDemo
        }

        fun priority(priority: Int) = apply {
            this.priority = priority
        }

        fun nQuestions(nQuestions: Int) = apply {
            this.nQuestions = nQuestions
        }

        fun user(userId: Long?) = apply {
            this.userId = userId
        }

        fun build(): Task {
            val task = Task()
            task.id = id
            task.completed = completed
            task.timestamp = timestamp
            task.name = name
            task.type = type
            task.estimatedCompletionTime = estimatedCompletionTime
            task.completionWindow = completionWindow
            task.warning = warning
            task.isClinical = isClinical
            task.timeCompleted = timeCompleted
            task.showInCalendar = showInCalendar
            task.isDemo = isDemo
            task.priority = priority
            task.nQuestions = nQuestions
            task.userId = userId
            return task
        }
    }

    override fun toString(): String {
        return "Task(id=$id, completed=$completed, timestamp=$timestamp, name=$name, type=$type, estimatedCompletionTime=$estimatedCompletionTime, completionWindow=$completionWindow, warning=$warning, isClinical=$isClinical, timeCompleted=$timeCompleted, showInCalendar=$showInCalendar, isDemo=$isDemo, priority=$priority, nQuestions=$nQuestions, user=$userId, status=$status)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Task) return false
        return userId == other.userId
            && timestamp == other.timestamp
            && name == other.name
    }

    override fun hashCode(): Int = Objects.hash(userId, timestamp, name)
}
