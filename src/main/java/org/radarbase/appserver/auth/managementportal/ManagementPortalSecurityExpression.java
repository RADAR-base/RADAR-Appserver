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

package org.radarbase.appserver.auth.managementportal;

import lombok.extern.slf4j.Slf4j;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.auth.token.RadarToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

/** @author yatharthranjan */
@ConditionalOnProperty(name = "managementportal.security.enabled", havingValue = "true")
@Slf4j
public class ManagementPortalSecurityExpression extends SecurityExpressionRoot
    implements MethodSecurityExpressionOperations {

  private Object returnObject;
  private Object filterObject;

  public ManagementPortalSecurityExpression(Authentication authentication) {
    super(authentication);
  }

  public boolean hasPermission(Permission permission) {
    RadarToken radarToken = getRadarToken(authentication);
    if (radarToken != null) {
      return radarToken.hasPermission(permission);
    } else {
      return false;
    }
  }

  public boolean hasPermissionOnProject(Permission permission, String projectId) {
    RadarToken radarToken = getRadarToken(authentication);
    if (radarToken != null) {
      return radarToken.hasPermissionOnProject(permission, projectId);
    } else {
      return false;
    }
  }

  public boolean hasPermissionOnSubject(Permission permission, String projectId, String subjectId) {
    RadarToken radarToken = getRadarToken(authentication);
    if (radarToken != null) {
      return radarToken.hasPermissionOnSubject(permission, projectId, subjectId);
    } else {
      return false;
    }
  }

  public boolean hasPermissionOnSource(
      Permission permission, String projectId, String subjectId, String sourceId) {
    RadarToken radarToken = getRadarToken(authentication);
    if (radarToken != null) {
      return radarToken.hasPermissionOnSource(permission, projectId, subjectId, sourceId);
    } else {
      return false;
    }
  }

  @Override
  public Object getFilterObject() {
    return filterObject;
  }

  @Override
  public void setFilterObject(Object filterObject) {
    this.filterObject = filterObject;
  }

  @Override
  public Object getReturnObject() {
    return this.returnObject;
  }

  @Override
  public void setReturnObject(Object returnObject) {
    this.returnObject = returnObject;
  }

  @Override
  public Object getThis() {
    return this;
  }

  private RadarToken getRadarToken(Authentication authentication) {
    if (authentication instanceof ManagementPortalOAuth2Authentication) {
      ManagementPortalOAuth2Authentication mpauth =
          (ManagementPortalOAuth2Authentication) authentication;
      return mpauth.getRadarToken();
    } else {
      log.warn(
          "Authentication needs to be an instance of {}",
          ManagementPortalOAuth2Authentication.class.getName());
      return null;
    }
  }
}
