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

package org.radarbase.appserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
public class MultiHttpSecurityConfig {
    @Value("${radar.admin.user}")
    private transient String adminUsername;

    @Value("${radar.admin.password}")
    private transient String adminPassword;

    @Bean
    public UserDetailsService userDetailsService() {

        // ensure the passwords are encoded properly
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        User.UserBuilder users = User.builder().passwordEncoder(encoder::encode);
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(
                users
                        .username(adminUsername)
                        .password(adminPassword)
                        .roles("ADMIN")
                        .authorities("ROLE_SYS_ADMIN")
                        .build());
        return manager;
    }

    @Configuration
    @Order(1)
    public static class AdminWebSecurityConfigurationAdapter {
        @Bean
        public SecurityFilterChain apiDocsFilterChain(HttpSecurity http) throws Exception {
            http.securityMatcher("/v3/api-docs**")
                    .cors()
                    .and()
                    .authorizeHttpRequests()
                    .anyRequest()
                    .permitAll()
                    // .hasRole("ADMIN")
                    .and()
                    .httpBasic()
                    .and()
                    .csrf()
                    .disable();

            return http.build();
        }
    }

    @Configuration
    public static class WebSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            // Allow all actuator endpoints.
            http.securityMatcher(EndpointRequest.toAnyEndpoint())
                    .cors()
                    .and()
                    .authorizeHttpRequests()
                    .anyRequest()
                    .permitAll()
                    .and()
                    .csrf()
                    .disable();

            http.headers().frameOptions().disable();

            return http.build();
        }
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
                // NOTE: allowedHeaders("*") is causing CORS issues so this has been removed (empty allows all headers by default)
            }
        };
    }

}
