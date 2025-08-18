/*
 *  Copyright 2018 King's College London
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.radarbase.appserver.dto.protocol

/**
 * Data Transfer object (DTO) for Protocol. A project may represent a `Protocol` for scheduling
 * questionnaires.
 *
 * @see <a href="https://github.com/RADAR-base/RADAR-aRMT-protocols">aRMT Protocols</a>
 */
data class Protocol(
    var version: String? = null,
    var schemaVersion: String? = null,
    var name: String? = null,
    var healthIssues: List<String>? = null,
    var protocols: List<Assessment>? = null,
) {
    fun hasAssessment(assessment: String?): Boolean {
        return protocols?.any { it.name == assessment } == true
    }
}
