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

package org.radarbase.appserver.dto.fcm;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** @author yatharthranjan */
@Getter
@EqualsAndHashCode
@ToString
public class FcmNotifications {

  private List<FcmNotificationDto> notifications;

  public FcmNotifications() {
    notifications = new ArrayList<>();
  }

  public FcmNotifications setNotifications(List<FcmNotificationDto> notifications) {
    this.notifications = notifications;
    return this;
  }

  public FcmNotifications addNotification(FcmNotificationDto notificationDto) {

    if (!notifications.contains(notificationDto)) {
      this.notifications.add(notificationDto);
    }
    return this;
  }
}