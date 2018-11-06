package org.radarbase.appserver.entity;

import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.List;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    private String projectId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "project", orphanRemoval = true)
    private List<User> users;

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public List<User> getUsers() {
        return users;
    }
}
