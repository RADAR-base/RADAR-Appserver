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


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Slf4j
@EnableWebSecurity
public class MultiHttpSecurityConfig {

    @Value("${radar.admin.user}")
    private String adminUsername;

    @Value("${radar.admin.password}")
    private String adminPassword;

    @Bean
    public UserDetailsService userDetailsService() throws Exception {

        // ensure the passwords are encoded properly
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        User.UserBuilder users = User.builder().passwordEncoder(encoder::encode);
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(users.username(adminUsername).password(adminPassword).roles("ADMIN").build());
        //manager.createUser(users.username("default").password("radar").roles("USER","ADMIN").build());
        return manager;
    }

    @Configuration
    @Order(1)
    public static class AdminWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.antMatcher("/actuator/**").authorizeRequests()
                    .anyRequest().hasRole("ADMIN")
                    .and()
                    .httpBasic();
        }

    }

    @Configuration
    public static class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {


            http.requestMatcher(EndpointRequest.toAnyEndpoint()).authorizeRequests()
                    .anyRequest().permitAll().and().csrf().disable();
            /*http
                    .authorizeRequests()
                    .anyRequest().permitAll()
                    .and()
                    //.formLogin();
                    .csrf().disable();*/

            //.authenticated().and().httpBasic()..anyRequest().permitAll();

            // Added because of https://stackoverflow.com/questions/53395200/h2-console-is-not-showing-in-browser
            http.headers().frameOptions().disable();
        }
    }
}