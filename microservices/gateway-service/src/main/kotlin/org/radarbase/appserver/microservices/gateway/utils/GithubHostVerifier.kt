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

import java.net.URI

object GithubHostVerifier {
    private val allowedGitHubHosts = setOf("github.com", "raw.githubusercontent.com", "api.github.com")

    fun isAllowedGithubUrl(url: String): Boolean {
        return try {
            val u = URI(url)
            val schemeOk = u.scheme?.lowercase() == "https"
            val hostOk = u.host?.lowercase() in allowedGitHubHosts
            schemeOk && hostOk
        } catch (_: Exception) {
            false
        }
    }

}
