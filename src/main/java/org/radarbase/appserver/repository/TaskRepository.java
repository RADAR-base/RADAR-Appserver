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

import org.radarbase.appserver.dto.protocol.AssessmentType;
import org.radarbase.appserver.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @author yatharthranjan
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findByUserId(Long userId);

    List<Task> findByUserIdAndType(Long userId, AssessmentType type);

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    void deleteByUserId(Long userId);

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    void deleteByUserIdAndType(Long userId, AssessmentType type);

    boolean existsByIdAndUserId(Long id, Long userId);

    boolean existsById(Long id);

    boolean existsByUserIdAndNameAndTimestamp(
            Long userId,
            String name,
            Timestamp timestamp);

    Optional<Task> findByIdAndUserId(long id, long userId);
}
