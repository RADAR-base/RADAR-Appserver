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

package org.radarbase.appserver.jersey.exception.handler

import jakarta.inject.Singleton
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.radarbase.appserver.jersey.exception.entity.ErrorResponse
import org.slf4j.LoggerFactory

@Provider
@Singleton
class UnhandledExceptionMapper(
    @Context private val uriInfo: UriInfo,
    @Context private val requestContext: ContainerRequestContext,
) : ExceptionMapper<Exception> {

    override fun toResponse(exception: Exception): Response {
        logger.error("[500] {} {} → {}", requestContext.method, uriInfo.path, exception.message, exception)

        val errorResponse = ErrorResponse(
            error = "internal_server_error",
            description = exception.message ?: "An unexpected error occurred.",
        )

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type("application/json; charset=utf-8")
            .entity(errorResponse)
            .build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UnhandledExceptionMapper::class.java)
    }
}
