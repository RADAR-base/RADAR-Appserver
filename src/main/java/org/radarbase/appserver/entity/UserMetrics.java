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

import java.io.Serializable;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * {@link Entity} for persisting important metrics about the {@link User}. A {@link User} can have
 * exactly one {@link UserMetrics} (One-to-One).
 *
 * @author yatharthranjan
 */
@Table(name = "user_metrics")
@Entity
@Getter
@ToString
public class UserMetrics extends AuditModel implements Serializable {

  private static final long serialVersionUID = 9182735866328519L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Setter
  private Long id;

  // The most recent time when the app was opened
  @Nullable
  @Column(name = "last_opened")
  private Instant lastOpened;

  // The most recent time when a notification for the app was delivered.
  @Nullable
  @Column(name = "last_delivered")
  private Instant lastDelivered;

  @ToString.Exclude @NonNull @OneToOne(mappedBy = "usermetrics") private User user;

  public UserMetrics(Instant lastOpened, Instant lastDelivered) {
    this.lastOpened = lastOpened;
    this.lastDelivered = lastDelivered;
  }

  public UserMetrics() {}

  public UserMetrics setLastOpened(Instant lastOpened) {
    this.lastOpened = lastOpened;
    return this;
  }

  public UserMetrics setLastDelivered(Instant lastDelivered) {
    this.lastDelivered = lastDelivered;
    return this;
  }

  public UserMetrics setUser(User user) {
    this.user = user;
    return this;
  }
}
