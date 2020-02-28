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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private Boolean completed = false;

    @NotNull
    private Instant timestamp;

    @NotNull
    private String name;

    private int estimatedCompletionTime;

    @NotNull
    private Long completionWindow;

    @NotNull
    private String warning;

    @NotNull
    private Boolean isClinical = false;

    @NotNull
    private Instant timeCompleted;

    @NotNull
    private Boolean showInCalendar = true;

    @NotNull
    private Boolean isDemo = false;

    @NotNull
    private int order;

    @NoArgsConstructor
    public static class TaskBuilder {
        transient Long id;
        transient Boolean completed;
        transient Instant timestamp;
        transient String name;
        transient int estimatedCompletionTime;
        transient Long completionWindow;
        transient String warning;
        transient Boolean isClinical;
        transient Instant timeCompleted;
        transient Boolean showInCalendar;
        transient Boolean isDemo;
        transient int order;

        public TaskBuilder(Task task) {
            this.id = task.getId();
            this.completed = task.getCompleted();
            this.timestamp = task.getTimestamp();
            this.name = task.getName();
            this.estimatedCompletionTime = task.getEstimatedCompletionTime();
            this.completionWindow = task.getCompletionWindow();
            this.warning = task.getWarning();
            this.isClinical = task.getIsClinical();
            this.timeCompleted = task.getTimeCompleted();
            this.showInCalendar = task.getShowInCalendar();
            this.isDemo = task.getIsDemo();
            this.order = task.getOrder();
        }

        public TaskBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TaskBuilder completed(Boolean completed) {
            this.completed = completed;
            return this;
        }

        public TaskBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TaskBuilder name(String name) {
            this.name = name;
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

        public TaskBuilder timeCompleted(Instant timeCompleted) {
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

        public TaskBuilder order(int order) {
            this.order = order;
            return this;
        }

        public Task build() {
            Task task = new Task();
            task.setId(this.id);
            task.setCompleted(this.completed);
            task.setTimestamp(this.timestamp);
            task.setName(this.name);
            task.setEstimatedCompletionTime(this.estimatedCompletionTime);
            task.setCompletionWindow(this.completionWindow);
            task.setWarning(this.warning);
            task.setIsClinical(this.isClinical);
            task.setTimeCompleted(this.timeCompleted);
            task.setShowInCalendar(this.showInCalendar);
            task.setIsDemo(this.isDemo);
            task.setOrder(this.order);

            return task;
        }
    }

}