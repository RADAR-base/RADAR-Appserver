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

package org.radarbase.appserver.jersey.config

data class AuthConfig(
    val resourceName: String = "res_Appserver",
    val issuer: String? = null,
    val managementPortalUrl: String = "http://localhost:8081/managementportal",
    val publicKeyUrls: List<String>? = null,
) : Validation {
    override fun validate() {
        check(managementPortalUrl != null || !publicKeyUrls.isNullOrEmpty()) {
            "At least one of auth.publicKeyUrls or auth.managementPortalUrl must be configured"
        }
    }
}
