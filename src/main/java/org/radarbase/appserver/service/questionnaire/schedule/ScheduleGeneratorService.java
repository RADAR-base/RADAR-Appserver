package org.radarbase.appserver.service.questionnaire.schedule;

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolGenerator;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public interface ScheduleGeneratorService {

    ProtocolHandler getProtocolHandler(Assessment assessment);

    ProtocolHandler getRepeatProtocolHandler(Assessment assessment);

    ProtocolHandler getRepeatQuestionnaireHandler(Assessment assessment);

    ProtocolHandler getNotificationHandler(Assessment assessment);

    ProtocolHandler getReminderHandler(Assessment assessment);

    default Schedule generateScheduleForUser(User user, Protocol protocol) {
        List<Assessment> assessments = protocol.getProtocols();

        List<AssessmentSchedule> assessmentSchedules = assessments.parallelStream().map(assessment -> 
            this.generateSingleAssessmentSchedule(assessment, user)).collect(Collectors.toList());

        return new Schedule(assessmentSchedules, user, protocol.getVersion());
    }

    default AssessmentSchedule generateSingleAssessmentSchedule(Assessment assessment, User user) {
        ProtocolHandlerRunner protocolHandlerRunner =
                new ProtocolHandlerRunner();
        protocolHandlerRunner.addProtocolHandler(this.getProtocolHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getRepeatProtocolHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getRepeatQuestionnaireHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getNotificationHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getReminderHandler(assessment));
        return protocolHandlerRunner.runProtocolHandlers(assessment, user);
    }

}
