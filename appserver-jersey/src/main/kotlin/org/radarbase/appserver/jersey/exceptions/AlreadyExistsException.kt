package org.radarbase.appserver.jersey.exceptions

import jakarta.ws.rs.core.Response
import org.radarbase.jersey.exception.HttpApplicationException

class AlreadyExistsException(code: String, message: String) : HttpApplicationException(
    Response.Status.INTERNAL_SERVER_ERROR,
    code,
    message,
)
