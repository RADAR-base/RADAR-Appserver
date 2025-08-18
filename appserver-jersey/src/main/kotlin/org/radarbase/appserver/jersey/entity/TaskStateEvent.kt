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

package org.radarbase.appserver.jersey.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.radarbase.appserver.jersey.event.state.TaskState
import java.time.Instant

@Entity
@Table(name = "task_state_events")
class TaskStateEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var state: TaskState? = null

    @field:NotNull
    @Column(nullable = false)
    var time: Instant? = null

    @Column(name = "associated_info", length = 1250)
    var associatedInfo: String? = null

    @field:NotNull
    @field:JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "task_id", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    var task: Task? = null

    constructor()

    constructor(task: Task?, state: TaskState?, time: Instant?, associatedInfo: String?) {
        this.state = state
        this.time = time
        this.associatedInfo = associatedInfo
        this.task = task
    }
}
