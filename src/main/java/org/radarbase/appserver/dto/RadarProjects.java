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

package org.radarbase.appserver.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yatharthranjan
 */
public class RadarProjects {

    private List<RadarProjectDto> projects;

    public RadarProjects() {
        this.projects = new ArrayList<>();
    }

    public List<RadarProjectDto> getProjects() {
        return projects;
    }

    public RadarProjects setProjects(List<RadarProjectDto> projects) {
        this.projects = projects;
        return this;
    }

    public RadarProjects addProject(RadarProjectDto project) {
        this.projects.add(project);
        return this;
    }
}
