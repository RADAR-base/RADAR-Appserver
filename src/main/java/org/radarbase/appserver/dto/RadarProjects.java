package org.radarbase.appserver.dto;

import java.util.List;

public class RadarProjects {

    private List<RadarProjectDto> projects;

    public List<RadarProjectDto> getProjects() {
        return projects;
    }

    public RadarProjects setProjects(List<RadarProjectDto> projects) {
        this.projects = projects;
        return this;
    }
}
