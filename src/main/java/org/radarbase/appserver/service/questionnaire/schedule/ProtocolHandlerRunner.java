package org.radarbase.appserver.service.questionnaire.schedule;

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class ProtocolHandlerRunner {
    private transient List<ProtocolHandler> protocolHandlers = new ArrayList<>();

    public AssessmentSchedule runProtocolHandlers(Assessment assessment, User user) {
        AssessmentSchedule assessmentSchedule = new AssessmentSchedule();
        for (ProtocolHandler leaf : this.protocolHandlers) {
            assessmentSchedule = leaf.handle(assessmentSchedule, assessment, user);
        }
        return assessmentSchedule;
    }

    public void addProtocolHandler(ProtocolHandler protocolHandler) {
        if (protocolHandler != null) this.protocolHandlers.add(protocolHandler);
    }
}
