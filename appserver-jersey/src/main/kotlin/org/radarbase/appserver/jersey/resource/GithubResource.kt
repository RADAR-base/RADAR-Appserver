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

package org.radarbase.appserver.jersey.resource

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.MediaType.TEXT_PLAIN
import jakarta.ws.rs.core.Response
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.service.github.GithubService
import org.radarbase.appserver.jersey.utils.Paths.GITHUB_CONTENT_PATH
import org.radarbase.appserver.jersey.utils.Paths.GITHUB_PATH
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.service.AsyncCoroutineService
import java.net.MalformedURLException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Path("/$GITHUB_PATH")
class GithubResource @Inject constructor(
    private val githubService: GithubService,
    private val asyncService: AsyncCoroutineService,
    appserverConfig: AppserverConfig,
) {
    private val requestTimeout: Duration = appserverConfig.server.requestTimeout.seconds

    @GET
    @Path("/$GITHUB_CONTENT_PATH")
    @Produces(TEXT_PLAIN)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun getGithubContent(
        @QueryParam("url") url: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            try {
                Response.ok(githubService.getGithubContent(url)).build()
            } catch (ex: MalformedURLException) {
                Response.status(Response.Status.BAD_REQUEST).entity(ex.message).build()
            } catch (ex: Exception) {
                Response.status(Response.Status.BAD_GATEWAY).entity(
                    "Error while fetching content from github: ${ex.message}",
                ).build()
            }
        }
    }
}
