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
