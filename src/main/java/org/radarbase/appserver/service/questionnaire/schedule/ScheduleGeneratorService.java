package org.radarbase.appserver.service.questionnaire.schedule;

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolGenerator;

import java.util.Iterator;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public interface ScheduleGeneratorService {

    AssessmentSchedule handleProtocol(AssessmentSchedule assessmentSchedule, Assessment assessment, User user);

    AssessmentSchedule handleRepeatProtocol(AssessmentSchedule assessmentSchedule, Assessment assessment, User user);

    AssessmentSchedule handleRepeatQuestionnaire(AssessmentSchedule assessmentSchedule, Assessment assessment, User user);

    AssessmentSchedule handleClinicalProtocol(AssessmentSchedule assessmentSchedule, Assessment assessment, User user);

    AssessmentSchedule handleNotifications(AssessmentSchedule assessmentSchedule, Assessment assessment, User user);

    default Schedule generateScheduleForUser(User user, ProtocolGenerator protocolGenerator) {
        String projectId = user.getProject().getProjectId();
        Protocol protocol = protocolGenerator.getProtocol(projectId);
        Schedule schedule = new Schedule(user);
        Iterator<Assessment> assessmentProtocolIterator = protocol.getProtocols().iterator();
        while (assessmentProtocolIterator.hasNext()) {
            Assessment assessment = assessmentProtocolIterator.next();
            AssessmentSchedule assessmentSchedule = generateSingleAssessmentSchedule(assessment, user);
            schedule.addAssessmentSchedule(assessmentSchedule);
        }
        return schedule;
    }

    default AssessmentSchedule generateSingleAssessmentSchedule(Assessment assessment, User user) {
        AssessmentSchedule assessmentSchedule = new AssessmentSchedule();
        assessmentSchedule = handleProtocol(assessmentSchedule, assessment, user);
        assessmentSchedule = handleRepeatProtocol(assessmentSchedule, assessment, user);
        assessmentSchedule = handleRepeatQuestionnaire(assessmentSchedule, assessment, user);
        assessmentSchedule = handleClinicalProtocol(assessmentSchedule, assessment, user);
//        assessmentSchedule = handleNotifications(assessmentSchedule, assessment, user);
        return assessmentSchedule;
    }
}
