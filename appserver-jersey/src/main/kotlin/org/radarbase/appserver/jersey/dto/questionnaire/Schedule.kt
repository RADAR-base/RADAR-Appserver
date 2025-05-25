package org.radarbase.appserver.jersey.dto.questionnaire

import org.radarbase.appserver.jersey.entity.User

@Suppress("unused")
data class Schedule(
    var assessmentSchedules: MutableList<AssessmentSchedule> = mutableListOf(),
    var user: User? = null,
    var version: String = "0.0.0",
    var timezone: String? = user?.timezone,
) {
    constructor(user: User) : this(mutableListOf(), user, "0.0.0", user.timezone)

    constructor(user: User, assessmentSchedules: List<AssessmentSchedule>) : this(
        assessmentSchedules.toMutableList(),
        user,
        "0.0.0",
        user.timezone,
    )

    constructor(
        assessmentSchedules: List<AssessmentSchedule>,
        user: User,
        version: String?,
    ) : this(
        assessmentSchedules.toMutableList(),
        user,
        version ?: "0.0.0",
        user.timezone,
    )

    fun addAssessmentSchedule(assessmentSchedule: AssessmentSchedule): Schedule = apply {
        this.assessmentSchedules.add(assessmentSchedule)
    }

    fun addAssessmentSchedules(assessmentSchedules: List<AssessmentSchedule>): Schedule = apply {
        this.assessmentSchedules.addAll(assessmentSchedules)
    }
}
