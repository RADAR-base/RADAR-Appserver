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

package org.radarbase.appserver.controller.exception

import org.hibernate.exception.ConstraintViolationException
import org.radarbase.appserver.exception.NotificationAlreadyExistsException
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.error.ErrorAttributeOptions.Include
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class CustomExceptionHandler : ResponseEntityExceptionHandler() {

    companion object {
        private val DEFAULT_ERROR_ATTRIBUTES = DefaultErrorAttributes()
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        ex: RuntimeException, request: WebRequest
    ): ResponseEntity<Any>? {
        val body = DEFAULT_ERROR_ATTRIBUTES.getErrorAttributes(
            request, ErrorAttributeOptions.of(Include.STACK_TRACE)
        )
        body["message"] = "A Constraint was violated while Persisting. ${body["message"]}"
        body["status"] = HttpStatus.CONFLICT.value()
        return handleExceptionInternal(ex, body, HttpHeaders(), HttpStatus.CONFLICT, request)
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val body = DEFAULT_ERROR_ATTRIBUTES.getErrorAttributes(
            request, ErrorAttributeOptions.of(Include.STACK_TRACE)
        )
        body["status"] = status
        return handleExceptionInternal(ex, body, headers, status, request)
    }

    @ExceptionHandler(NotificationAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    fun handleException(e: NotificationAlreadyExistsException): NotificationAlreadyExistsException {
        return e
    }
}