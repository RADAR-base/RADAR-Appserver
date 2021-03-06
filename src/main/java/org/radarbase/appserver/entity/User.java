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

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.radarbase.appserver.dto.fcm.FcmUserDto;

/**
 * {@link Entity} for persisting users. The corresponding DTO is {@link FcmUserDto}. A {@link
 * Project} can have multiple {@link User} (Many-to-One).
 *
 * @author yatharthranjan
 */
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"subject_id", "fcm_token", "project_id"})
        })
@Entity
@Getter
@ToString
public class User extends AuditModel implements Serializable {

    private static final long serialVersionUID = -87395866328519L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @NotNull
    @Column(name = "fcm_token", nullable = false, unique = true)
    private String fcmToken;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Project project;

    @NotNull
    @Column(name = "enrolment_date")
    private Instant enrolmentDate;

    @OneToOne(cascade = CascadeType.ALL)
    private UserMetrics usermetrics;

    // Timezone of the user based on tz database names
    @NotNull
    @Column(name = "timezone")
    private String timezone;

    @NotEmpty
    @Column(name = "language")
    private String language;

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

    public User setTimezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    public User setLanguage(String language) {
        this.language = language;
        return this;
    }

    public User setId(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(getSubjectId(), user.getSubjectId())
                && Objects.equals(getFcmToken(), user.getFcmToken())
                && Objects.equals(getProject(), user.getProject())
                && Objects.equals(getEnrolmentDate(), user.getEnrolmentDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubjectId(), getFcmToken(), getProject());
    }
}
