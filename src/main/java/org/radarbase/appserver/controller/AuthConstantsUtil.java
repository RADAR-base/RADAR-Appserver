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

package org.radarbase.appserver.controller;

public class AuthConstantsUtil {
  public static final String PROJECT_ID = "projectId";
  public static final String SUBJECT_ID = "subjectId";
  public static final String ACCESSOR = "#";
  public static final String USER_DTO_SUBJECT_ID = "userDto.getSubjectId()";
  public static final String PERMISSION_ON_SUBJECT_MEASUREMENT_CREATE =
      "hasPermissionOnSubject(T(org.radarcns.auth.authorization.Permission).MEASUREMENT_CREATE, ";
  public static final String PERMISSION_ON_PROJECT_MEASUREMENT_CREATE =
      "hasPermissionOnProject(T(org.radarcns.auth.authorization.Permission).MEASUREMENT_CREATE, ";
  public static final String PERMISSION_ON_SUBJECT_SUBJECT_READ =
      "hasPermissionOnSubject(T(org.radarcns.auth.authorization.Permission).SUBJECT_READ, ";
  public static final String PERMISSION_ON_PROJECT_SUBJECT_READ =
      "hasPermissionOnProject(T(org.radarcns.auth.authorization.Permission).SUBJECT_READ, ";
  public static final String IS_ADMIN = "hasAuthority('ROLE_SYS_ADMIN') or hasRole('ADMIN')";
}
