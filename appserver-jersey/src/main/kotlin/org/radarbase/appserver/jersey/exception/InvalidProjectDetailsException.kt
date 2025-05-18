package org.radarbase.appserver.jersey.exception

import jakarta.ws.rs.core.Response
import org.radarbase.jersey.exception.HttpApplicationException

class InvalidProjectDetailsException(message: String) : HttpApplicationException(
    Response.Status.INTERNAL_SERVER_ERROR,
    "invalid_project_details",
    message,
)
