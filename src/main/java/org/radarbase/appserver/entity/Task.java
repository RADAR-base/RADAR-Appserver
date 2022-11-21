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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.radarbase.appserver.dto.protocol.AssessmentType;
import org.radarbase.appserver.event.state.TaskState;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoField;

@Entity
@Table(
        name = "tasks"
)
@Getter
@Setter
@ToString
@NoArgsConstructor
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class Task extends AuditModel implements Serializable {
    private static final long serialVersionUID = 90L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    private Boolean completed;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp timestamp;

    @NotNull
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AssessmentType type;

    private int estimatedCompletionTime;

    @NotNull
    private Long completionWindow;

    private String warning;

    private Boolean isClinical;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp timeCompleted;

    @NotNull
    private Boolean showInCalendar;

    @NotNull
    private Boolean isDemo;

    @NotNull
    private int priority;

    @NotNull
    private int nQuestions;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskState status = TaskState.UNKNOWN;

    @NoArgsConstructor
    public static class TaskBuilder {
        transient Long id;
        transient Boolean completed = false;
        transient @NotNull Timestamp timestamp;
        transient String name;
        transient AssessmentType type;
        transient int estimatedCompletionTime;
        transient Long completionWindow;
        transient String warning;
        transient Boolean isClinical = false;
        transient Timestamp timeCompleted;
        transient Boolean showInCalendar = true;
        transient Boolean isDemo = false;
        transient int priority = 0;
        transient int nQuestions;
        transient User user;

        public TaskBuilder(Task task) {
            this.id = task.getId();
            this.completed = task.getCompleted();
            this.timestamp = task.getTimestamp();
            this.name = task.getName();
            this.type = task.getType();
            this.estimatedCompletionTime = task.getEstimatedCompletionTime();
            this.completionWindow = task.getCompletionWindow();
            this.warning = task.getWarning();
            this.isClinical = task.getIsClinical();
            this.timeCompleted = task.getTimeCompleted();
            this.showInCalendar = task.getShowInCalendar();
            this.isDemo = task.getIsDemo();
            this.priority = task.getPriority();
            this.nQuestions = task.getNQuestions();
            this.user = task.getUser();
        }

        public TaskBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TaskBuilder completed(Boolean completed) {
            this.completed = completed;
            return this;
        }

        public TaskBuilder timestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TaskBuilder timestamp(Instant timestamp) {
            this.timestamp = Timestamp.from(timestamp);
            return this;
        }

        public TaskBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TaskBuilder type(AssessmentType type) {
            this.type = type;
            return this;
        }

        public TaskBuilder estimatedCompletionTime(int estimatedCompletionTime) {
            this.estimatedCompletionTime = estimatedCompletionTime;
            return this;
        }

        public TaskBuilder completionWindow(Long completionWindow) {
            this.completionWindow = completionWindow;
            return this;
        }

        public TaskBuilder warning(String warning) {
            this.warning = warning;
            return this;
        }

        public TaskBuilder isClinical(Boolean isClinical) {
            this.isClinical = isClinical;
            return this;
        }

        public TaskBuilder timeCompleted(Timestamp timeCompleted) {
            this.timeCompleted = timeCompleted;
            return this;
        }

        public TaskBuilder showInCalendar(Boolean showInCalendar) {
            this.showInCalendar = showInCalendar;
            return this;
        }

        public TaskBuilder isDemo(Boolean isDemo) {
            this.isDemo = isDemo;
            return this;
        }

        public TaskBuilder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public TaskBuilder nQuestions(int nQuestions) {
            this.nQuestions = nQuestions;
            return this;
        }

        public TaskBuilder user(User user) {
            this.user = user;
            return this;
        }

        public Task build() {
            Task task = new Task();
            task.setId(this.id);
            task.setCompleted(this.completed);
            task.setTimestamp(this.timestamp);
            task.setName(this.name);
            task.setType(this.type);
            task.setEstimatedCompletionTime(this.estimatedCompletionTime);
            task.setCompletionWindow(this.completionWindow);
            task.setWarning(this.warning);
            task.setIsClinical(this.isClinical);
            task.setTimeCompleted(this.timeCompleted);
            task.setShowInCalendar(this.showInCalendar);
            task.setIsDemo(this.isDemo);
            task.setPriority(this.priority);
            task.setNQuestions(this.nQuestions);
            task.setUser(this.user);

            return task;
        }
    }

}
