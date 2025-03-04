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

package org.radarbase.appserver.config

import org.radarbase.auth.token.RadarToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import radar.spring.auth.common.AuthAspect
import radar.spring.auth.common.AuthValidator
import radar.spring.auth.common.Authorization
import radar.spring.auth.config.ManagementPortalAuthProperties
import radar.spring.auth.managementportal.ManagementPortalAuthValidator
import radar.spring.auth.managementportal.ManagementPortalAuthorization

@Configuration
@ConditionalOnProperty(name = ["security.radar.managementportal.enabled"], havingValue = "true")
@EnableAspectJAutoProxy
class AuthConfig {
    @Value("\${security.radar.managementportal.url}")
    @Transient
    private val baseUrl: String? = null

    @Value("\${security.oauth2.resource.id}")
    @Transient
    private val resourceName: String? = null

    @Bean
    fun getAuthProperties(): ManagementPortalAuthProperties {
            val validatorConfig = TokenVerifierPublicKeyConfig.readFromFileOrClasspath()
            return ManagementPortalAuthProperties(baseUrl!!, resourceName!!, validatorConfig.publicKeyEndpoints)
        }

    /**
     * First tries to load config from radar-is.yml config file. If any issues, then uses the default
     * MP oauth token key endpoint.
     *
     * @param managementPortalAuthProperties
     * @return
     */
    @Bean
    fun getAuthValidator(
        @Autowired managementPortalAuthProperties: ManagementPortalAuthProperties
    ): AuthValidator<RadarToken> {
        return ManagementPortalAuthValidator(managementPortalAuthProperties)
    }

    @Bean
    fun getAuthorization(): Authorization<RadarToken> {
        return ManagementPortalAuthorization()
    }

    @Bean
    fun getAuthAspect(
        @Autowired authValidator: AuthValidator<RadarToken>,
        @Autowired authorization: Authorization<RadarToken>
    ): AuthAspect<RadarToken> {
        return AuthAspect<RadarToken>(authValidator, authorization)
    }

    interface AuthEntities {
        companion object {
            const val MEASUREMENT: String = "MEASUREMENT"
            const val PROJECT: String = "PROJECT"
            const val SUBJECT: String = "SUBJECT"
            const val SOURCE: String = "SOURCE"
        }
    }

    interface AuthPermissions {
        companion object {
            const val READ: String = "READ"
            const val CREATE: String = "CREATE"
            const val UPDATE: String = "UPDATE"
        }
    }
}