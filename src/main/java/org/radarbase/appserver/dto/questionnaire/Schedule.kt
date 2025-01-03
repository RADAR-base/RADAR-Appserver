/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.radarbase.appserver.dto.questionnaire

import org.radarbase.appserver.entity.User

@Suppress("unused")
data class Schedule(
    var assessmentSchedules: MutableList<AssessmentSchedule> = mutableListOf(),
    var user: User? = null,
    var version: String = "0.0.0",
    var timezone: String? = user?.timezone
) {
    constructor(user: User) : this(mutableListOf(), user, "0.0.0", user.timezone)

    constructor(user: User, assessmentSchedules: List<AssessmentSchedule>) : this(
        assessmentSchedules.toMutableList(),
        user,
        "0.0.0",
        user.timezone
    )

    constructor(
        assessmentSchedules: List<AssessmentSchedule>,
        user: User,
        version: String?
    ) : this(
        assessmentSchedules.toMutableList(),
        user,
        version ?: "0.0.0",
        user.timezone
    )

    fun addAssessmentSchedule(assessmentSchedule: AssessmentSchedule): Schedule = apply {
        this.assessmentSchedules.add(assessmentSchedule)
    }

    fun addAssessmentSchedules(assessmentSchedules: List<AssessmentSchedule>): Schedule = apply {
        this.assessmentSchedules.addAll(assessmentSchedules)
    }
}
