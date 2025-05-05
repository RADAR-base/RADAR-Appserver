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

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableWebSecurity
class MultiHttpSecurityConfig {
    @Value("\${radar.admin.user}")
    @Transient
    private val adminUsername: String? = null

    @Value("\${radar.admin.password}")
    @Transient
    private val adminPassword: String? = null

    @Bean
    fun userDetailsService(): UserDetailsService {
        // ensure the passwords are encoded properly

        val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

        val users = User.builder().passwordEncoder(encoder::encode)
        val manager = InMemoryUserDetailsManager()
        manager.createUser(
            users
                .username(adminUsername)
                .password(adminPassword)
                .roles("ADMIN")
                .authorities("ROLE_SYS_ADMIN")
                .build()
        )
        return manager
    }

    @Configuration
    @Order(1)
    class AdminWebSecurityConfigurationAdapter {
        @Bean
        @Throws(Exception::class)
        fun apiDocsFilterChain(http: HttpSecurity): SecurityFilterChain? {
            http.securityMatcher("/v3/api-docs**")
                .cors()
                .and()
                .authorizeHttpRequests()
                .anyRequest()
                .permitAll() // .hasRole("ADMIN")
                .and()
                .httpBasic()
                .and()
                .csrf()
                .disable()

            return http.build()
        }
    }

    @Configuration
    class WebSecurityConfig {
        @Bean
        @Throws(Exception::class)
        fun filterChain(http: HttpSecurity): SecurityFilterChain? {
            // Allow all actuator endpoints.
            http.securityMatcher(EndpointRequest.toAnyEndpoint())
                .cors()
                .and()
                .authorizeHttpRequests()
                .anyRequest()
                .permitAll()
                .and()
                .csrf()
                .disable()

            http.headers().frameOptions().disable()

            return http.build()
        }
    }

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedOrigins("*")
                    .allowedHeaders("*")
            }
        }
    }
}
