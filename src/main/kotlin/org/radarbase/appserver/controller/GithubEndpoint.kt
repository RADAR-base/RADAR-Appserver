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
package org.radarbase.appserver.controller

import org.radarbase.appserver.config.AuthConfig
import org.radarbase.appserver.service.GithubService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import radar.spring.auth.common.Authorized
import java.net.MalformedURLException

@CrossOrigin
@RestController
class GithubEndpoint(private val githubService: GithubService) {

    @Authorized(permission = AuthConfig.AuthPermissions.READ, entity = AuthConfig.AuthEntities.SUBJECT)
    @GetMapping(
        ("/" +
                PathsUtil.GITHUB_PATH
                + "/" +
                PathsUtil.GITHUB_CONTENT_PATH)
    )
    fun getGithubContent(
        @RequestParam url: String
    ): ResponseEntity<String> {
        return try {
            ResponseEntity.ok().body<String>(this.githubService.getGithubContent(url))
        } catch (e: MalformedURLException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body<String?>(e.message)
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.BAD_GATEWAY).body<String>("Error getting content from Github.")
        }
    }
}
