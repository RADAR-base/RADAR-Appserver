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

import org.radarcns.auth.token.RadarToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * A wrapper around the {@link OAuth2Authentication} class for embedding {@link RadarToken} into the
 * auth state so can be used later for checking permissions.
 *
 * @author yatharthranjan
 */
@ConditionalOnProperty(name = "managementportal.security.enabled", havingValue = "true")
public class ManagementPortalOAuth2Authentication extends OAuth2Authentication {

  private static final long serialVersionUID = -7891278322984383034L;

  private final transient RadarToken radarToken;

  public ManagementPortalOAuth2Authentication(
      OAuth2Authentication oAuth2Authentication, RadarToken radarToken) {
    super(oAuth2Authentication.getOAuth2Request(), oAuth2Authentication.getUserAuthentication());
    this.radarToken = radarToken;
  }

  public RadarToken getRadarToken() {
    return radarToken;
  }
}
