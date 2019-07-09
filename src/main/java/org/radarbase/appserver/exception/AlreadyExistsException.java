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

package org.radarbase.appserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested {@link javax.swing.text.html.parser.Entity} or resource that
 * needs to be added/created already exists. If accessed by REST API then gives a HTTP status {@link
 * HttpStatus#ALREADY_REPORTED}.
 *
 * @author yatharthranjan
 */
@ResponseStatus(HttpStatus.ALREADY_REPORTED)
public class AlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = -793674245766939L;

  public AlreadyExistsException(String message) {
    super(message);
  }

  public AlreadyExistsException(String message, Object object) {
    super(message + " " + object.toString());
  }
}
