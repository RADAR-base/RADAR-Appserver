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

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import javax.validation.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class NotificationRepositoryTest {
  @Autowired private TestEntityManager entityManager;

  @Autowired private NotificationRepository notificationRepository;

  private Long id;
  private User user;
  private Instant scheduledTime;

  /**
   * Insert a Notification Before each test.
   */
  @Before
  public void initNotification() {
    //given
    Project project = new Project().setProjectId("test-project");
    entityManager.persist(project);

   this.user = new User()
        .setFcmToken("xxxx")
        .setEnrolmentDate(Instant.now())
        .setProject(project)
        .setTimezone(0d)
        .setLanguage("en")
        .setSubjectId("test-user");
    entityManager.persist(this.user);

    this.scheduledTime = Instant.now().plus(Duration.ofSeconds(100));

    Notification notification = new Notification()
        .setUser(user)
        .setBody("Test notif")
        .setTitle("Testing")
        .setFcmMessageId("12345")
        .setScheduledTime(this.scheduledTime)
        .setSourceId("test")
        .setTtlSeconds(86400)
        .setDelivered(false);

    this.id = (long) entityManager.persistAndGetId(notification);
    entityManager.flush();
  }

  @Test
  public void whenInsertWithTransientUser_thenThrowException() {
    //given
    Notification notification = new Notification()
        .setUser(new User())
        .setBody("Test notif")
        .setTitle("Testing")
        .setFcmMessageId("12345")
        .setScheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
        .setSourceId("test")
        .setTtlSeconds(86400)
        .setDelivered(false);

    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
      entityManager.persist(notification);
      entityManager.flush();
    });

    assertTrue(ex.getMessage().contains("Not-null property references a transient value"));
  }

  @Test
  public void whenInsertWithoutUser_thenThrowException() {
    //given
    Notification notification = new Notification()
        .setBody("Test notif")
        .setTitle("Testing")
        .setFcmMessageId("12345")
        .setScheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
        .setSourceId("test")
        .setTtlSeconds(86400)
        .setDelivered(false);

    assertThrows(ConstraintViolationException.class, () -> {
      entityManager.persist(notification);
      entityManager.flush();
    });
  }

  @Test
  public void whenInsertWithUserButTransientProject_thenThrowException() {
    //given
    User user = new User()
        .setFcmToken("xxxx")
        .setEnrolmentDate(Instant.now())
        .setProject(new Project())
        .setTimezone(0d)
        .setLanguage("en")
        .setSubjectId("test-user");


    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
      entityManager.persist(user);
      entityManager.flush();
    });

    assertTrue(ex.getMessage().contains("Not-null property references a transient value"));

    Notification notification = new Notification()
        .setUser(user)
        .setBody("Test notif")
        .setTitle("Testing")
        .setFcmMessageId("12345")
        .setScheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
        .setSourceId("test")
        .setTtlSeconds(86400)
        .setDelivered(false);

    ex = assertThrows(IllegalStateException.class, () -> {
      entityManager.persist(notification);
      entityManager.flush();
    });

    assertTrue(ex.getMessage().contains("Not-null property references a transient value"));
  }

  @Test
  public void whenExistsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds_thenReturnTrue() {
    // when
    boolean exists = notificationRepository
        .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
            this.user.getId(),
            "test",
            this.scheduledTime,
            "Testing",
            "Test notif",
            null,
            86400);

    // then
    assertTrue(exists);
    assertTrue(notificationRepository.existsById(this.id));
  }

  @Test
  public void whenDeleteNotificationByFcmMessageId_thenExistsFalse() {
    // when
    notificationRepository.deleteByFcmMessageId("12345");

    //then
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