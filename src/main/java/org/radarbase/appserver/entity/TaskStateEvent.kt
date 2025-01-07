package org.radarbase.appserver.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.radarbase.appserver.event.state.TaskState
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
