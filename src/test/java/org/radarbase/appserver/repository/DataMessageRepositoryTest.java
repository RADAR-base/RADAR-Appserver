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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EnableJpaAuditing
public class DataMessageRepositoryTest {
    public static final Long DATA_MESSAGE_ID = 12345L;
    public static final String DATA_MESSAGE_FCM_MESSAGE_ID = "12345";
    public static final String DATA_MESSAGE_SOURCE_ID = "test";
    @Autowired
    private transient TestEntityManager entityManager;
    @Autowired
    private transient DataMessageRepository dataMessageRepository;
    private transient Long id;
    private transient User user;
    private transient Instant scheduledTime;

    /**
     * Insert a DataMessage Before each test.
     */
    @Before
    public void initDataMessage() {
        // given
        Project project = new Project().setProjectId("test-project");
        entityManager.persist(project);

        this.user =
                new User()
                        .setFcmToken("xxxx")
                        .setEnrolmentDate(Instant.now())
                        .setProject(project)
                        .setTimezone(0d)
                        .setLanguage("en")
                        .setSubjectId("test-user");
        entityManager.persist(this.user);

        this.scheduledTime = Instant.now().plus(Duration.ofSeconds(100));

        DataMessage dataMessage =
                new DataMessage.DataMessageBuilder()
                        .id(DATA_MESSAGE_ID)
                        .user(user)
                        .fcmMessageId(DATA_MESSAGE_FCM_MESSAGE_ID)
                        .scheduledTime(this.scheduledTime)
                        .sourceId(DATA_MESSAGE_SOURCE_ID)
                        .ttlSeconds(86400)
                        .delivered(false)
                        .build();

        this.id = (Long) entityManager.persistAndGetId(dataMessage);
        entityManager.flush();
    }

    @Test
    public void whenInsertWithTransientUser_thenThrowException() {
        // given
        DataMessage dataMessage =
                new DataMessage.DataMessageBuilder()
                        .id(DATA_MESSAGE_ID)
                        .user(new User())
                        .fcmMessageId(DATA_MESSAGE_FCM_MESSAGE_ID)
                        .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
                        .sourceId(DATA_MESSAGE_SOURCE_ID)
                        .ttlSeconds(86400)
                        .delivered(false)
                        .build();

        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () -> {
                            entityManager.persist(dataMessage);
                            entityManager.flush();
                        });

        assertTrue(ex.getMessage().contains("Not-null property references a transient value"));
    }

    @Test
    public void whenInsertWithoutUser_thenThrowException() {
        // given
        DataMessage dataMessage =
                new DataMessage.DataMessageBuilder()
                        .id(DATA_MESSAGE_ID)
                        .fcmMessageId(DATA_MESSAGE_FCM_MESSAGE_ID)
                        .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
                        .sourceId(DATA_MESSAGE_SOURCE_ID)
                        .ttlSeconds(86400)
                        .delivered(false)
                        .build();

        assertThrows(
                ConstraintViolationException.class,
                () -> {
                    entityManager.persist(dataMessage);
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
                        .setTimezone(0d)
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

        DataMessage dataMessage =
                new DataMessage.DataMessageBuilder()
                        .id(DATA_MESSAGE_ID)
                        .user(user)
                        .fcmMessageId(DATA_MESSAGE_FCM_MESSAGE_ID)
                        .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
                        .sourceId(DATA_MESSAGE_SOURCE_ID)
                        .ttlSeconds(86400)
                        .delivered(false)
                        .build();

        ex =
                assertThrows(
                        IllegalStateException.class,
                        () -> {
                            entityManager.persist(dataMessage);
                            entityManager.flush();
                        });

        assertTrue(ex.getMessage().contains("Not-null property references a transient value"));
    }

    @Test
    public void whenExists_thenReturnTrue() {
        // when
        boolean exists =
                dataMessageRepository
                        .existsByUserIdAndSourceIdAndScheduledTimeAndTtlSeconds(
                                this.user.getId(),
                                DATA_MESSAGE_SOURCE_ID,
                                this.scheduledTime,
                                86400);

        // then
        assertTrue(exists);
        assertTrue(dataMessageRepository.existsById(this.id));
    }

    @Test
    public void whenDeleteDataMessageById_thenExistsFalse() {
        // when
        dataMessageRepository.deleteById(DATA_MESSAGE_ID);

        // then
        DataMessage dataMessage = entityManager.find(DataMessage.class, this.id);
        assertNull(dataMessage);
    }

    @Test
    public void whenDeleteDataMessageByUserId_thenExistsFalse() {
        // when
        dataMessageRepository.deleteByUserId(this.user.getId());

        // then
        DataMessage dataMessage = entityManager.find(DataMessage.class, this.id);
        assertNull(dataMessage);
    }
}
