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
package org.radarbase.appserver.dto.protocol

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.radarbase.appserver.util.Base64Deserializer

/**
 * Data Transfer object (DTO) for Protocol. A project may represent a Protcol for scheduling
 * questionnaires.
 *
 * @see [aRMT Protocols](https://github.com/RADAR-base/RADAR-aRMT-protocols)
 *
 * @author yatharthranjan
 */
data class GithubContent(
    @field:JsonDeserialize(using = Base64Deserializer::class)
    var content: String? = null,

    var sha: String? = null,

    var size: String? = null,

    var url: String? = null,

    var node_id: String? = null,

    var encoding: String? = null,
)
