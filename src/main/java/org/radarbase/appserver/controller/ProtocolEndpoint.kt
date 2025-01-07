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
package org.radarbase.appserver.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.radarbase.appserver.config.AuthConfig.AuthEntities
import org.radarbase.appserver.config.AuthConfig.AuthPermissions
import org.radarbase.appserver.dto.protocol.Protocol
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolGenerator
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import radar.spring.auth.common.Authorized
import radar.spring.auth.common.PermissionOn
import java.io.IOException

@Suppress("unused")
@CrossOrigin
@RestController
class ProtocolEndpoint (private val protocolGenerator: ProtocolGenerator) {

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
    @GetMapping("/" + PathsUtil.PROTOCOL_PATH)
    fun  getProtocols(): @Size(max = 100) Map<String, Protocol> {
        return this.protocolGenerator.retrieveAllProtocols()
    }

    @GetMapping(
        value = [("/"
                + PathsUtil.PROJECT_PATH
                + "/"
                + PathsUtil.PROJECT_ID_CONSTANT
                + "/"
                + PathsUtil.USER_PATH
                + "/"
                + PathsUtil.SUBJECT_ID_CONSTANT
                + "/"
                + PathsUtil.PROTOCOL_PATH)]
    )
    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.PROJECT)
    fun getProtocolUsingProjectIdAndSubjectId(
        @PathVariable @Valid projectId: String, @PathVariable @Valid subjectId: String
    ): Protocol {
        return this.protocolGenerator.getProtocolForSubject(subjectId)
    }

    @GetMapping(
        ("/"
                + PathsUtil.PROJECT_PATH
                + "/"
                + PathsUtil.PROJECT_ID_CONSTANT
                + "/"
                + PathsUtil.PROTOCOL_PATH)
    )
    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
    @Throws(IOException::class)
    fun getProtocolUsingProjectId(
        @PathVariable projectId: @Valid String
    ): Protocol {
        return this.protocolGenerator.getProtocol(projectId)
    }
}
