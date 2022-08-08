package org.radarbase.appserver.service.questionnaire.schedule;

import liquibase.pro.packaged.L;
import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolGenerator;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public interface ScheduleGeneratorService {

    ProtocolHandler getProtocolHandler(Assessment assessment);

    ProtocolHandler getRepeatProtocolHandler(Assessment assessment);

    ProtocolHandler getRepeatQuestionnaireHandler(Assessment assessment);

    ProtocolHandler getNotificationHandler(Assessment assessment);

    ProtocolHandler getReminderHandler(Assessment assessment);

    ProtocolHandler getCompletedQuestionnaireHandler(Assessment assessment, List<Task> prevTasks);

    default Schedule generateScheduleForUser(User user, Protocol protocol, Schedule prevSchedule) {
        List<Assessment> assessments = protocol.getProtocols();
        List<AssessmentSchedule> prevAssessmentSchedules = prevSchedule.getAssessmentSchedules();

        List<AssessmentSchedule> assessmentSchedules = assessments.parallelStream().map(
                assessment -> {
                    Optional<AssessmentSchedule> prevAssessmentSchedule =
                            prevAssessmentSchedules.stream().filter(a -> a.getName() == assessment.getName()).findFirst();
                    return this.generateSingleAssessmentSchedule(assessment, user, prevAssessmentSchedule.isPresent() ? prevAssessmentSchedule.get().getTasks() : Collections.emptyList());
                }
        ).collect(Collectors.toList());

        return new Schedule(assessmentSchedules, user, protocol.getVersion());
    }

    default AssessmentSchedule generateSingleAssessmentSchedule(Assessment assessment, User user, List<Task> previousTasks) {
        ProtocolHandlerRunner protocolHandlerRunner =
                new ProtocolHandlerRunner();
        protocolHandlerRunner.addProtocolHandler(this.getProtocolHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getRepeatProtocolHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getRepeatQuestionnaireHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getNotificationHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getReminderHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getCompletedQuestionnaireHandler(assessment, previousTasks));
        return protocolHandlerRunner.runProtocolHandlers(assessment, user);
    }

}
