package org.radarbase.appserver.service.questionnaire.schedule;

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolGenerator;

import java.util.Iterator;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public interface ScheduleGeneratorService {

    ProtocolHandler getProtocolHandler(Assessment assessment);

    ProtocolHandler getRepeatProtocolHandler(Assessment assessment);

    ProtocolHandler getRepeatQuestionnaireHandler(Assessment assessment);

    ProtocolHandler getNotificationHandler(Assessment assessment);


    default Schedule generateScheduleForUser(User user, ProtocolGenerator protocolGenerator) {
        Protocol protocol = protocolGenerator.getProtocol(user.getProject().getProjectId());
        Schedule schedule = new Schedule(user);
        Iterator<Assessment> assessmentProtocolIterator = protocol.getProtocols().iterator();
        while (assessmentProtocolIterator.hasNext()) {
            Assessment assessment = assessmentProtocolIterator.next();
            AssessmentSchedule assessmentSchedule = this.generateSingleAssessmentSchedule(assessment, user);
            schedule.addAssessmentSchedule(assessmentSchedule);
        }
        return schedule;
    }

    default AssessmentSchedule generateSingleAssessmentSchedule(Assessment assessment, User user) {
        ProtocolHandlerRunner protocolHandlerRunner =
                new ProtocolHandlerRunner();
        protocolHandlerRunner.addProtocolHandler(this.getProtocolHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getRepeatProtocolHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getRepeatQuestionnaireHandler(assessment));
        protocolHandlerRunner.addProtocolHandler(this.getNotificationHandler(assessment));
        return protocolHandlerRunner.runProtocolHandlers(assessment, user);
    }

}
