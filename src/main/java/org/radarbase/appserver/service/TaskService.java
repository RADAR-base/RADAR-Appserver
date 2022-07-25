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

package org.radarbase.appserver.service;

import org.radarbase.appserver.dto.protocol.AssessmentType;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.exception.AlreadyExistsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.TaskRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link Service} for interacting with the {@link Task} {@link javax.persistence.Entity} using
 * the {@link TaskRepository}.
 *
 * @author yatharthranjan
 */
@Service
public class TaskService {
    private static final String INVALID_SUBJECT_ID_MESSAGE =
            "The supplied Subject ID is invalid. No user found. Please Create a User First.";

    private final transient TaskRepository taskRepository;
    private final transient UserRepository userRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Task> getAllProjects() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        Optional<Task> task = taskRepository.findById(id);

        if (task.isPresent()) {
            return task.get();
        } else {
            throw new NotFoundException("Task not found with id" + id);
        }
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksBySubjectId(String subjectId) {
        Optional<User> user = this.userRepository.findBySubjectId(subjectId);
        if (user.isEmpty()) {
            throw new NotFoundException(INVALID_SUBJECT_ID_MESSAGE);
        }
        List<Task> tasks = taskRepository.findByUserId(user.get().getId());
        return tasks;
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksBySubjectIdAndType(String subjectId, AssessmentType type) {
        Optional<User> user = this.userRepository.findBySubjectId(subjectId);
        if (user.isEmpty()) {
            throw new NotFoundException(INVALID_SUBJECT_ID_MESSAGE);
        }
        List<Task> tasks = taskRepository.findByUserIdAndType(user.get().getId(), type);
        return tasks;
    }

    public List<Task> getTasksByUser(User user) {
        return taskRepository.findByUserId(user.getId());
    }

    @Transactional
    public List<Task> getTasksBySpecification(Specification<Task> spec) {
        return taskRepository.findAll(spec);
    }

    @Transactional
    public void deleteTasksBySpecification(Specification<Task> spec) {
        List<Task> tasks = taskRepository.findAll(spec);
        taskRepository.deleteAll(tasks);
    }

    @Transactional
    public Task addTask(Task task) {
        User user = task.getUser();
        if (!this.taskRepository.existsByUserIdAndNameAndTimestamp(user.getId(), task.getName(), task.getTimestamp())) {
            Task saved = this.taskRepository.saveAndFlush(task);
            user.getUsermetrics().setLastOpened(Instant.now());
            this.userRepository.save(user);
            return saved;
        } else throw new AlreadyExistsException(
                "The Task Already exists. Please Use update endpoint", task);
    }

    @Transactional
    public List<Task> addTasks(List<Task> tasks, User user) {
        List<Task> newTasks = tasks.stream()
       .filter(task -> !this.taskRepository.existsByUserIdAndNameAndTimestamp(user.getId(), task.getName(), task.getTimestamp()))
        .collect(Collectors.toList());

        List<Task> saved = this.taskRepository.saveAll(newTasks);
        this.taskRepository.flush();

        return saved;
    }

}
