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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.radarbase.appserver.controller.RadarUserControllerTest.TIMEZONE;

import java.time.Duration;
import java.time.Instant;
import jakarta.persistence.PersistenceException;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Disabled
@ExtendWith(SpringExtension.class)
@DataJpaTest
@EnableJpaAuditing
public class NotificationRepositoryTest {
    public static final String NOTIFICATION_BODY = "Test notif";
    public static final String NOTIFICATION_TITLE = "Testing";
    public static final String NOTIFICATION_FCM_MESSAGE_ID = "12345";
    public static final String NOTIFICATION_SOURCE_ID = "test";
    private static final String TIMEZONE = "Europe/London";
    @Autowired
    private transient TestEntityManager entityManager;
    @Autowired
    private transient NotificationRepository notificationRepository;
    private transient Long id;
    private transient User user;
    private transient Instant scheduledTime;

    /**
     * Insert a Notification Before each test.
     */
    @BeforeEach
    public void initNotification() {
        // given
        Project project = new Project().setProjectId("test-project");
        entityManager.persist(project);

        this.user =
                new User()
                        .setFcmToken("xxxx")
                        .setEnrolmentDate(Instant.now())
                        .setProject(project)
                        .setTimezone(TIMEZONE)
                        .setLanguage("en")
                        .setSubjectId("test-user");
        entityManager.persist(this.user);

        this.scheduledTime = Instant.now().plus(Duration.ofSeconds(100));

        Notification notification =
                new Notification.NotificationBuilder()
                        .user(user)
                        .body(NOTIFICATION_BODY)
                        .title(NOTIFICATION_TITLE)
                        .fcmMessageId(NOTIFICATION_FCM_MESSAGE_ID)
                        .scheduledTime(this.scheduledTime)
                        .sourceId(NOTIFICATION_SOURCE_ID)
                        .ttlSeconds(86400)
                        .delivered(false)
                        .build();

        this.id = (Long) entityManager.persistAndGetId(notification);
        entityManager.flush();
    }

    @Test
    public void whenInsertWithTransientUser_thenThrowException() {
        // given
        Notification notification =
                new Notification.NotificationBuilder()
                        .user(new User())
                        .body(NOTIFICATION_BODY)
                        .title(NOTIFICATION_TITLE)
                        .fcmMessageId(NOTIFICATION_FCM_MESSAGE_ID)
                        .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
                        .sourceId(NOTIFICATION_SOURCE_ID)
                        .ttlSeconds(86400)
                        .delivered(false)
                        .build();

        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () -> {
                            entityManager.persist(notification);
                            entityManager.flush();
                        });

        assertTrue(ex.getMessage().contains("Not-null property references a transient value"));
    }

    @Test
    public void whenInsertWithoutUser_thenThrowException() {
        // given
        Notification notification =
                new Notification.NotificationBuilder()
                        .body(NOTIFICATION_BODY)
                        .title(NOTIFICATION_TITLE)
                        .fcmMessageId(NOTIFICATION_FCM_MESSAGE_ID)
                        .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
                        .sourceId(NOTIFICATION_SOURCE_ID)
                        .ttlSeconds(86400)
                        .delivered(false)
                        .build();

        assertThrows(
                ConstraintViolationException.class,
                () -> {
                    entityManager.persist(notification);
                    entityManager.flush();
                });
    }

    @Test
    public void whenInsertWithUserButTransientProject_thenThrowException() {
        // given
        User user =
                new User()
                        .setFcmToken("xxxx")
                        .setEnrolmentDate(Instant.now())
                        .setProject(new Project())
                        .setTimezone(TIMEZONE)
                        .setLanguage("en")
                        .setSubjectId("test-user");

        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () -> {
                            entityManager.persist(user);
                            entityManager.flush();
                        });

        assertTrue(ex.getMessage().contains("Not-null property references a transient value"));

        Notification notification =
                new Notification.NotificationBuilder()
                        .user(user)
                        .body(NOTIFICATION_BODY)
                        .title(NOTIFICATION_TITLE)
                        .fcmMessageId(NOTIFICATION_FCM_MESSAGE_ID)
                        .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
                        .sourceId(NOTIFICATION_SOURCE_ID)
                        .ttlSeconds(86400)
                        .delivered(false)
                        .build();

        ex =
                assertThrows(
                        IllegalStateException.class,
                        () -> {
                            entityManager.persist(notification);
                            entityManager.flush();
                        });

        assertTrue(ex.getSuppressed().length > 0);
        assertTrue(ex.getSuppressed()[0].getMessage().contains("Not-null property references a transient value"));
    }

    @Test
    public void whenExists_thenReturnTrue() {
        // when
        boolean exists =
                notificationRepository
                        .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                                this.user.getId(),
                                NOTIFICATION_SOURCE_ID,
                                this.scheduledTime,
                                NOTIFICATION_TITLE,
                                NOTIFICATION_BODY,
                                null,
                                86400);

        // then
        assertTrue(exists);
        assertTrue(notificationRepository.existsById(this.id));
    }

    @Test
    public void whenDeleteNotificationById_thenExistsFalse() {
        // when
        notificationRepository.deleteById(this.id);

        // then
        Notification notification = entityManager.find(Notification.class, this.id);
        assertNull(notification);
    }

    @Test
    public void whenDeleteNotificationByUserId_thenExistsFalse() {
        // when
        notificationRepository.deleteByUserId(this.user.getId());

        // then
        Notification notification = entityManager.find(Notification.class, this.id);
        assertNull(notification);
    }
}
