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

package org.radarbase.appserver.converter;

import org.radarbase.appserver.dto.RadarProjectDto;
import org.radarbase.appserver.entity.Project;
import org.radarbase.fcm.dto.FcmUserDto;
import org.radarbase.fcm.dto.FcmUsers;

import java.util.stream.Collectors;

/**
 * @author yatharthranjan
 */
public class ProjectConverter extends Converter<Project, RadarProjectDto> {
    @Override
    public Project dtoToEntity(RadarProjectDto radarProjectDto) {
        return null;
    }

    @Override
    public RadarProjectDto entityToDto(Project project) {

        FcmUsers users = new FcmUsers().setUsers(project.getUsers().parallelStream()
                .map(FcmUserDto::new)
                .collect(Collectors.toList()));


        return new RadarProjectDto().setId(project.getId())
                .setProjectId(project.getProjectId())
                .setFcmUsers(users);
    }
}
