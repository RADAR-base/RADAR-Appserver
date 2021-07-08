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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;

/**
 * Exception thrown when a requested {@link org.radarbase.appserver.entity.Notification} that needs
 * to be added/created already exists.
 *
 * see {@link org.radarbase.appserver.controller.exception.CustomExceptionHandler}
 * @author yatharthranjan
 */
@NoArgsConstructor
@JsonIgnoreProperties({"cause", "stackTrace", "suppressed", "localizedMessage"})
public class NotificationAlreadyExistsException extends RuntimeException {

  @JsonIgnore private static final long serialVersionUID = -79364859476939L;

  private String errorMessage;
  private FcmNotificationDto dto;

  public NotificationAlreadyExistsException(String message) {
    super(message);
  }

  public NotificationAlreadyExistsException(String message, FcmNotificationDto object) {
    super(message + " " + object.toString());
    this.dto = object;
    this.errorMessage = message;
  }

  public FcmNotificationDto getDto() {
    return dto;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
