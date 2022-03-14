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

package org.radarbase.appserver.controller.exception;

import java.util.Map;
import org.hibernate.exception.ConstraintViolationException;
import org.radarbase.appserver.exception.NotificationAlreadyExistsException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

  private static final DefaultErrorAttributes DEFAULT_ERROR_ATTRIBUTES =
      new DefaultErrorAttributes();

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleConstraintViolationException(
      RuntimeException ex, WebRequest request) {

    Map<String, Object> body =
        DEFAULT_ERROR_ATTRIBUTES.getErrorAttributes(
            request, ErrorAttributeOptions.of(Include.STACK_TRACE));
    body.put("message", "A Constraint was violated while Persisting. " + body.get("message"));
    body.put("status", HttpStatus.CONFLICT.value());
    return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.CONFLICT, request);
  }

  @Override
  public ResponseEntity<Object> handleMethodArgumentNotValid(
       MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    Map<String, Object> body =
        DEFAULT_ERROR_ATTRIBUTES.getErrorAttributes(
            request, ErrorAttributeOptions.of(Include.STACK_TRACE));
    body.put("status", status);
    return handleExceptionInternal(ex, body, headers, status, request);
  }

  @ExceptionHandler(NotificationAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public @ResponseBody NotificationAlreadyExistsException handleException(
      NotificationAlreadyExistsException e) {
    return e;
  }
}
