package org.radarbase.appserver.service.questionnaire.protocol;

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.event.state.TaskState;
import org.radarbase.appserver.service.TaskService;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

public class CompletedQuestionnaireHandler implements ProtocolHandler {
    private transient List<Task> prevTasks;
    private transient String prevTimezone;

    public CompletedQuestionnaireHandler(List<Task> prevTasks, String prevTimezone) {
        this.prevTasks = prevTasks;
        this.prevTimezone = prevTimezone;
    }

    @Override
    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        String currentTimezone = user.getTimezone();
        this.markTasksAsCompleted(assessmentSchedule.getTasks(), prevTasks, currentTimezone, prevTimezone);
        return assessmentSchedule;
    }


    @Transactional
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public List<Task> markTasksAsCompleted(List<Task> currentTasks, List<Task> previousTasks, String currentTimezone, String prevTimezone) {
        currentTasks.parallelStream().forEach(newTask -> {
            Optional<Task> matching;
            if (!currentTimezone.equals(prevTimezone)) {
                Timestamp prevTimestamp = getPreviousTimezoneEquivalent(newTask.getTimestamp(), currentTimezone, prevTimezone);
                matching = previousTasks.parallelStream().filter( u -> areMatchingTasks(newTask, u, prevTimestamp)).findFirst();
            }
            else {
                matching = previousTasks.parallelStream().filter( u -> areMatchingTasks(newTask, u)).findFirst();
            }
            if (matching.isPresent()) {
                Task matchingTask = matching.get();
                if (matchingTask.getStatus().equals(TaskState.COMPLETED)) {
                    newTask.setCompleted(true);
                    newTask.setTimeCompleted(matchingTask.getTimeCompleted());
                    newTask.setStatus(TaskState.COMPLETED);
                }
            }
        });
        return currentTasks;
    }

    private boolean areMatchingTasks(Task a, Task b) {
        return a.getTimestamp().equals(b.getTimestamp()) && a.getName().equals(b.getName());
    }

    private boolean areMatchingTasks(Task a, Task b, Timestamp bTimestamp) {
        return a.getTimestamp().equals(bTimestamp) && a.getName().equals(b.getName());
    }

    private Timestamp getPreviousTimezoneEquivalent(Timestamp taskTimestamp, String newTimezone, String prevTimezone) {
        int timezoneDiff = TimeZone.getTimeZone(newTimezone).getRawOffset() - TimeZone.getTimeZone(prevTimezone).getRawOffset();
        Timestamp prevTimestamp = new Timestamp(taskTimestamp.getTime() + timezoneDiff);

        return prevTimestamp;
    }

}
