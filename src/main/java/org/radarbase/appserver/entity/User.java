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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author yatharthranjan
 */
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"subject_id", "fcm_token", "project_id"})})
@Entity
@Getter
@ToString
public class User extends AuditModel{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter private Long id;

    @NotNull
    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @NotNull
    @Column(name = "fcm_token", nullable = false)
    private String fcmToken;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Project project;

    @NotNull
    @Column(name = "enrolment_date")
    private Instant enrolmentDate;

    @OneToOne(cascade = CascadeType.ALL)
    private UserMetrics usermetrics;

    //Timezone offset of the user in seconds
    @NotNull
    @Column(name = "timezone")
    private double timezone;

    public User setSubjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public User setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        return this;
    }

    public User setProject(Project project) {
        this.project = project;
        return this;
    }

    public User setEnrolmentDate(Instant enrolmentDate) {
        this.enrolmentDate = enrolmentDate;
        return this;
    }

    public User setUserMetrics(UserMetrics userMetrics) {
        this.usermetrics = userMetrics;
        return this;
    }
    public User setTimezone(double timezone) {
        this.timezone = timezone;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getSubjectId(), user.getSubjectId()) &&
        Objects.equals(getFcmToken(), user.getFcmToken()) &&
                Objects.equals(getProject(), user.getProject()) &&
                Objects.equals(getEnrolmentDate(), user.getEnrolmentDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubjectId(), getFcmToken(), getProject());
    }
}
