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

package org.radarbase.appserver.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import jakarta.validation.constraints.NotNull;
import org.radarbase.appserver.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** @author yatharthranjan */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdAndTaskId(Long userId, Long taskId);

    List<Notification> findByTaskId(Long id);

    void deleteByUserId(Long userId);

    void deleteByTaskId(Long taskId);

    void deleteByUserIdAndTaskId(Long userId, Long taskId);

    boolean existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
            Long userId,
            String sourceId,
            Instant scheduledTime,
            String title,
            String body,
            String type,
            int ttlSeconds);

    Optional<Notification> findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
            Long userId,
            String sourceId,
            Instant scheduledTime,
            String title,
            String body,
            String type,
            int ttlSeconds);

    boolean existsByIdAndUserId(Long id, Long userId);

    boolean existsById(@NotNull Long id);

    boolean existsByTaskId(Long taskId);

    void deleteByFcmMessageId(String fcmMessageId);

    void deleteByIdAndUserId(Long id, Long userId);

    Optional<Notification> findByFcmMessageId(String fcmMessageId);

    Optional<Notification> findByIdAndUserId(long id, long userId);
}
