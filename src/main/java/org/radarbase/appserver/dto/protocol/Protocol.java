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

package org.radarbase.appserver.dto.protocol;

import java.util.List;
import lombok.Data;

/**
 * Data Transfer object (DTO) for Protocol. A project may represent a Protcol for scheduling
 * questionnaires.
 *
 * @see <a href="https://github.com/RADAR-base/RADAR-aRMT-protocols">aRMT Protocols</a>
 * @author yatharthranjan
 */
@Data
public class Protocol {

  private String version;

  private String schemaVersion;

  private String name;

  private List<String> healthIssues;

  private List<Assessment> protocols;

  public boolean hasAssessment(String assessment) {
    return this.protocols.stream().anyMatch(a -> a.getName().equals(assessment));
  }
}
