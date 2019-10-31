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

package org.radarbase.appserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * {@link Entity} for persisting notifications. The corresponding DTO is {@link FcmNotificationDto}.
 * This also includes information for scheduling the notification through the Firebase Cloud
 * Messaging(FCM) system.
 *
 * @see Scheduled
 * @see org.radarbase.appserver.service.scheduler.NotificationSchedulerService
 * @see org.radarbase.appserver.service.fcm.FcmMessageReceiverService
 * @author yatharthranjan
 */
@Entity
@Table(name = "data_messages")
@Getter
@ToString
public class DataMessage extends Message<DataMessage> {

  @Nullable
  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "key", nullable = true)
  @Column(name = "value")
  private Map<String, String> data;

  public DataMessage setAdditionalData(Map<String, String> data) {
    this.data = data;
    return this;
  }

}
