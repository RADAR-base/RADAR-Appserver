package org.radarbase.appserver.service.questionnaire.protocol;

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.event.state.TaskState;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.QuestionnaireScheduleService;
import org.radarbase.appserver.service.TaskService;
import org.radarbase.appserver.service.questionnaire.notification.NotificationGeneratorService;
import org.radarbase.appserver.service.questionnaire.notification.NotificationType;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompletedQuestionnaireHandler implements ProtocolHandler {
    private transient TaskService taskService;
    private transient List<Task> prevTasks;

    public CompletedQuestionnaireHandler(TaskService taskService, List<Task> prevTasks) {
        this.prevTasks = prevTasks;
        this.taskService = taskService;
    }

    @Override
    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        List<Task> tasks = markTasksAsCompleted(assessmentSchedule.getTasks(), prevTasks, user);
        return assessmentSchedule;
    }


    @Transactional
    public List<Task> markTasksAsCompleted(List<Task> currentTasks, List<Task> previousTasks, User user) {
        currentTasks.parallelStream().forEach( newTask -> {
            Optional<Task> matching = previousTasks.parallelStream().filter( u -> areMatchingTasks(newTask, u)).findFirst();
            if (matching.isPresent()) {
                Task matchingTask = matching.get();
                if (matchingTask.getStatus().equals(TaskState.COMPLETED))
                    taskService.updateTaskStatus(newTask, TaskState.COMPLETED);
            }
        });
        return currentTasks;
    }

    private boolean areMatchingTasks(Task a, Task b) {
        return a.getTimestamp().equals(b.getTimestamp()) && a.getName().equals(b.getName());
    }

}
