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

import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;
/**
 * @author yatharthranjan
 */
@Table(name = "project")
@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "project_id", unique = true, nullable = false)
    private String projectId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "project", orphanRemoval = true)
    @UniqueElements
    private Set<User> users;

    public Project() {
        this.users = Collections.synchronizedSet(new HashSet<>());
    }

    public Long getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Project setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public Project setUsers(Set<User> users) {
        this.users = users;
        return this;
    }

    public Project addUser(User user) {
        this.users.add(user);
        return this;
    }

    public Project addUsers(Set<User> users) {
        this.users.addAll(users);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project project = (Project) o;
        return Objects.equals(getProjectId(), project.getProjectId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProjectId());
    }
}
