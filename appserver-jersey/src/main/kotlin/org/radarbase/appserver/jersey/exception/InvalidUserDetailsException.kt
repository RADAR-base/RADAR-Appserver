package org.radarbase.appserver.jersey.exception

import jakarta.ws.rs.core.Response
import org.radarbase.jersey.exception.HttpApplicationException

class InvalidUserDetailsException(message: String) : HttpApplicationException(
    Response.Status.INTERNAL_SERVER_ERROR,
    "invalid_user_details",
    message,
)
