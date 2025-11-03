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

package org.radarbase.appserver.microservices.gateway.config

import org.radarbase.appserver.microservices.core.config.CoreAuthConfig
import org.radarbase.appserver.microservices.core.config.Validation
import org.radarbase.jersey.config.ConfigLoader.copyEnv
import org.radarbase.jersey.config.ConfigLoader.copyOnChange
import org.radarbase.jersey.enhancer.EnhancerFactory
import java.net.URI

data class GatewayConfig(
    val resourceConfig: Class<out EnhancerFactory>,
    val auth: CoreAuthConfig,
    val externalPrefix: String = "",
    val routes: Set<ServiceRoute>,
    val server: GatewayServerConfig,
) : Validation {
    fun withEnv(): GatewayConfig = this
        .copyOnChange(
            server,
            {
                it.withEnv()
            },
            {
                copy(server = it)
            },
        )
        .copyEnv("APPSERVER_PROJECT_SERVICE_BASE_URL") { projectBase ->
            updateOrAddRoute("project") { prevConfig ->
                ServiceRoute(
                    name = prevConfig?.name ?: "project",
                    baseUrl = projectBase,
                    path = prevConfig?.path ?: "projects",
                )
            }.let { updated ->
                copy(routes = updated)
            }
        }
        .copyEnv("APPSERVER_USER_SERVICE_BASE_URL") { userBase ->
            updateOrAddRoute("user") { prevConfig ->
                ServiceRoute(
                    name = prevConfig?.name ?: "user",
                    baseUrl = userBase,
                    path = prevConfig?.path ?: "users",
                )
            }.let {
                copy(routes = it)
            }
        }

    private fun updateOrAddRoute(name: String, createOrUpdate: (ServiceRoute?) -> ServiceRoute): Set<ServiceRoute> {
        val existing = routes.firstOrNull { it.name == name }
        val updatedRoute = createOrUpdate(existing)
        return routes.filter { it.name != name }.plus(updatedRoute).toSet()
    }

    override fun validate() {
        routes.forEach { route ->
            val baseUri = try {
                URI.create(route.baseUrl.removeSuffix("/"))
            } catch (ex: Exception) {
                throw IllegalArgumentException("Invalid baseUrl for route '${route.name}': ${route.baseUrl}", ex)
            }
            require(!baseUri.scheme.isNullOrBlank() && !baseUri.host.isNullOrBlank()) {
                "route '${route.name}' has invalid baseUrl (must include scheme and host): ${route.baseUrl}"
            }

            val normalizedPath = route.path.trim().trim('/')
            require(normalizedPath.isNotBlank()) { "route '${route.name}' path cannot be empty" }
        }
    }
}
