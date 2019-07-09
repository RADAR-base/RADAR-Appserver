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

package org.radarbase.appserver.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.service.QuestionnaireScheduleService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validator for RADAR Active RMT app notifications.
 *
 * @see aRmtNotificationConstraint
 * @author yatharthranjan
 */
public class ARmtNotificationValidator
    implements ConstraintValidator<aRmtNotificationConstraint, Notification> {

  @Autowired private transient QuestionnaireScheduleService scheduleService;

  @Override
  public void initialize(aRmtNotificationConstraint constraintAnnotation) {
    // TODO
  }

  @Override
  public boolean isValid(@Valid Notification notification, ConstraintValidatorContext context) {
    return scheduleService
        .getScheduleForUser(notification.getUser())
        .getNotifications()
        .contains(notification);
  }
}
