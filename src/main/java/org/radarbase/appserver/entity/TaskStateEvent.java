package org.radarbase.appserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.radarbase.appserver.event.state.TaskState;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Getter
@Table(name = "task_state_events")
@NoArgsConstructor
public class TaskStateEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskState state;

    @NotNull
    @Column(nullable = false)
    private Instant time;

    @Column(name = "associated_info", length = 1250)
    private String associatedInfo;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Task task;


    public TaskStateEvent(
            @NotNull Task task,
            @NotNull TaskState state,
            @NotNull Instant time,
            String associatedInfo
    ) {
        this.state = state;
        this.time = time;
        this.associatedInfo = associatedInfo;
        this.task = task;
    }
}
