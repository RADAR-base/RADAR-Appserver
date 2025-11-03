/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.microservices.gateway.utils

import jakarta.ws.rs.core.Response
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import java.net.URI

object Utils {
    fun handleProxyResponse(proxy: ProxyResponse): Response {
        return if (proxy.status == 201 && !proxy.location.isNullOrBlank()) {
            val created = Response.created(URI(proxy.location ?: ""))
            if (!proxy.contentType.isNullOrBlank()) created.type(proxy.contentType)
            if (proxy.body != null) created.entity(proxy.body)
            created.build()
        } else {
            val builder = Response.status(proxy.status)
            if (!proxy.contentType.isNullOrBlank()) builder.type(proxy.contentType)
            if (proxy.body != null) builder.entity(proxy.body)
            builder.build()
        }
    }
}
