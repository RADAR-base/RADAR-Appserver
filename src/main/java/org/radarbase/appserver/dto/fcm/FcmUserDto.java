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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.radarbase.appserver.entity.User;
import org.springframework.format.annotation.DateTimeFormat;

/** @author yatharthranjan */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FcmUserDto implements Serializable {

  private static final long serialVersionUID = 1L;

  // Unique user key
  private Long id;

  // Project ID to be used in org.radarcns.kafka.ObservationKey record keys
  private String projectId;

  @NotEmpty
  // User ID to be used in org.radarcns.kafka.ObservationKey record keys
  private String subjectId;

  @Email
  // Email address of the user (optional, needed when sending notifications via email)
  private String email;

  // The most recent time when the app was opened
  private Instant lastOpened;

  // The most recent time when a notification for the app was delivered.
  private Instant lastDelivered;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant updatedAt;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant enrolmentDate;

  // Timezone of the user based on tz database names
  @NotNull
  private String timezone;

  private String fcmToken;

  private String language;

  @Size(max = 100)
  private Map<String, String> attributes;

  public FcmUserDto(User user) {
    this.id = user.getId();
    this.projectId = user.getProject().getProjectId();
    this.subjectId = user.getSubjectId();
    this.email = user.getEmailAddress();
    if (user.getUsermetrics() != null) {
      this.lastOpened = user.getUsermetrics().getLastOpened();
      this.lastDelivered = user.getUsermetrics().getLastDelivered();
    }
    this.fcmToken = user.getFcmToken();
    this.enrolmentDate = user.getEnrolmentDate();
    this.timezone = user.getTimezone();
    this.language = user.getLanguage();
    if (user.getCreatedAt() != null) {
      this.createdAt = user.getCreatedAt().toInstant();
    }
    if (user.getUpdatedAt() != null) {
      this.updatedAt = user.getUpdatedAt().toInstant();
    }
    this.attributes = user.getAttributes();
  }

  public FcmUserDto setId(Long id) {
    this.id = id;
    return this;
  }

  public FcmUserDto setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public FcmUserDto setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public FcmUserDto setProjectId(String projectId) {
    this.projectId = projectId;
    return this;
  }

  public FcmUserDto setSubjectId(String subjectId) {
    this.subjectId = subjectId;
    return this;
  }

  public FcmUserDto setEmail(String email) {
      this.email = email;
      return this;
  }

  public FcmUserDto setLastOpened(Instant lastOpened) {
    this.lastOpened = lastOpened;
    return this;
  }

  public FcmUserDto setLastDelivered(Instant lastDelivered) {
    this.lastDelivered = lastDelivered;
    return this;
  }

  public FcmUserDto setFcmToken(String fcmToken) {
    this.fcmToken = fcmToken;
    return this;
  }

  public FcmUserDto setEnrolmentDate(Instant enrolmentDate) {
    this.enrolmentDate = enrolmentDate;
    return this;
  }

  public FcmUserDto setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }

  public FcmUserDto setLanguage(String language) {
    this.language = language;
    return this;
  }

  public FcmUserDto setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }
}
