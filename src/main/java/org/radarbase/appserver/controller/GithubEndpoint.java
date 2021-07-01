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

package org.radarbase.appserver.controller;

import org.radarbase.appserver.config.AuthConfig;
import org.radarbase.appserver.service.protocol.GithubClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import radar.spring.auth.common.Authorized;
import radar.spring.auth.common.PermissionOn;

import java.io.IOException;
import java.net.MalformedURLException;

@RestController
public class GithubEndpoint {

    private transient GithubClient githubClient;

    @Autowired
    public GithubEndpoint(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    @Authorized(
            permission = AuthConfig.AuthPermissions.READ,
            entity = AuthConfig.AuthEntities.SUBJECT)
    @GetMapping("/" +
            PathsUtil.GITHUB_PATH
            + "/" +
            PathsUtil.GITHUB_CONTENT_PATH)
    public ResponseEntity getGithubContent(@RequestParam() String url
    ) {
        try {
            return ResponseEntity.ok().body(this.githubClient.getGithubContent(url));
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error getting content from Github.");
        }
    }
}
