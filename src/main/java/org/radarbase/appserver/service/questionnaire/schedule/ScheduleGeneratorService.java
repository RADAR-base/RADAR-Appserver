package org.radarbase.appserver.service.questionnaire.schedule;

import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolGenerator;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public interface ScheduleGeneratorService {

    Schedule handleProtocol(Schedule schedule, Protocol protocol);

    Schedule handleRepeatProtocol(Schedule schedule, Protocol protocol);

    Schedule handleRepeatQuestionnaire(Schedule schedule, Protocol protocol);

    Schedule handleClinicalProtocol(Schedule schedule, Protocol protocol);

    Schedule getScheduleBySubjectId(String subjectId);

    default Schedule generateScheduleForUser(User user, ProtocolGenerator protocolGenerator) {
        Protocol protocol = protocolGenerator.getProtocol(user.getProject().getProjectId());
        Schedule schedule = new Schedule(user.getEnrolmentDate(), user.getTimezone());
        schedule = handleProtocol(schedule, protocol);
        schedule = handleRepeatProtocol(schedule, protocol);
        schedule = handleRepeatQuestionnaire(schedule, protocol);
        schedule = handleClinicalProtocol(schedule, protocol);
        return schedule;
    }
}
