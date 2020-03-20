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

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import scala.Array;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
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
        // manager.createUser(users.username("default").password("radar").roles("USER","ADMIN").build());
        return manager;
    }

    @Configuration
    @Order(1)
    public static class AdminWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            
            http.antMatcher("/v3/api-docs**")
                    .authorizeRequests()
                    .anyRequest()
                    .permitAll()
                    // .hasRole("ADMIN")
                    .and()
                    .httpBasic();
        }
    }

    @Configuration
    public static class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // Allow all actuator endpoints.
            http.requestMatcher(EndpointRequest.toAnyEndpoint())
                    .authorizeRequests()
                    .anyRequest()
                    .permitAll()
                    .and()
                    .csrf()
                    .disable();
      /*http
      .authorizeRequests()
      .anyRequest().permitAll()
      .and()
      //.formLogin();
      .csrf().disable();*/

            // .authenticated().and().httpBasic()..anyRequest().permitAll();

            // Added because of
            // https://stackoverflow.com/questions/53395200/h2-console-is-not-showing-in-browser
           http.headers().frameOptions().disable();

        }


    }

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public class SimpleCorsFilter implements Filter {

        public SimpleCorsFilter() {
        }

        @Override
        public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
            HttpServletResponse response = (HttpServletResponse) res;
            HttpServletRequest request = (HttpServletRequest) req;
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers", "x-requested-with, authorization, content-type");

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                chain.doFilter(req, res);
            }
        }

        @Override
        public void init(FilterConfig filterConfig) {
        }

        @Override
        public void destroy() {
        }
    }

}