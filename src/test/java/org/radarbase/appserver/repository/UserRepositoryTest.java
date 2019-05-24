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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.radarbase.appserver.controller.FcmNotificationControllerTest.USER_ID;
import static org.radarbase.appserver.controller.RadarUserControllerTest.FCM_TOKEN_1;

import java.time.Instant;
import javax.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@EnableJpaAuditing
class UserRepositoryTest {

  @Autowired private transient TestEntityManager entityManager;

  @Autowired private transient UserRepository userRepository;

  private transient Project project;
  private transient Long projectId;
  private transient User user;
  private transient Long userId;

  @BeforeEach
  void setUp() {

    this.project = new Project().setProjectId("test-project");
    this.projectId = entityManager.persistAndGetId(project, Long.class);

    this.user =
        new User()
            .setFcmToken(FCM_TOKEN_1)
            .setEnrolmentDate(Instant.now())
            .setProject(project)
            .setTimezone(0d)
            .setLanguage("en")
            .setSubjectId(USER_ID);
    this.userId = entityManager.persistAndGetId(this.user, Long.class);
    entityManager.flush();
  }

  @Test
  public void whenInsertWithTransientProject_thenThrowException() {
    User user1 =
        new User()
            .setFcmToken(FCM_TOKEN_1)
            .setEnrolmentDate(Instant.now())
            .setProject(new Project())
            .setTimezone(0d)
            .setLanguage("en")
            .setSubjectId(USER_ID);

    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () -> {
              entityManager.persist(user1);
              entityManager.flush();
            });

    assertTrue(ex.getMessage().contains("Not-null property references a transient value"));
  }

  @Test
  public void whenFindUserBySubjectId_thenReturnUser() {

    assertEquals(
        userRepository.findBySubjectId(USER_ID).get(), entityManager.find(User.class, this.userId));
  }

  @Test
  public void whenFindByProjectId_thenReturnUsers() {
    assertEquals(
        userRepository.findByProjectId(this.projectId).get(0),
        entityManager.find(User.class, this.userId));
  }

  @Test
  public void whenFindBySubjectIdAndProjectId_thenReturnUser() {
    assertEquals(
        userRepository.findBySubjectIdAndProjectId(USER_ID, this.projectId).get(),
        entityManager.find(User.class, this.userId));
  }

  @Test
  public void whenFindByFcmToken_thenReturnUser() {
    assertEquals(
        userRepository.findByFcmToken(FCM_TOKEN_1).get(), entityManager.find(User.class, this.userId));
  }

  @Test
  public void whenInsertWithExistingFcmToken_thenThrowException() {
    User user1 =
        new User()
            .setFcmToken(FCM_TOKEN_1)
            .setEnrolmentDate(Instant.now())
            .setProject(this.project)
            .setTimezone(0d)
            .setLanguage("en")
            .setSubjectId(USER_ID + "-2");

    PersistenceException ex =
        assertThrows(
            PersistenceException.class,
            () -> {
              entityManager.persistAndGetId(user1, Long.class);
              entityManager.flush();
            });

    assertEquals(ConstraintViolationException.class, ex.getCause().getClass());
  }
}
