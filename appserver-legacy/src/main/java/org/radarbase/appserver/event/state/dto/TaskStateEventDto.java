package org.radarbase.appserver.event.state.dto;

import lombok.Getter;
import lombok.ToString;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.event.state.TaskState;
import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Map;

@Getter
@ToString
public class TaskStateEventDto extends ApplicationEvent {
    private static final long serialVersionUID = 327842183571948L;

    private final Task task;
    private final TaskState state;
    private final Map<String, String> additionalInfo;
    private final Instant time;

    public TaskStateEventDto(@NonNull Object source,
                             @NonNull Task task,
                             @NonNull TaskState state,
                             Map<String, String> additionalInfo,
                             @NonNull Instant time) {
        super(source);
        this.task = task;
        this.state = state;
        this.additionalInfo = additionalInfo;
        this.time = time;
    }
}
