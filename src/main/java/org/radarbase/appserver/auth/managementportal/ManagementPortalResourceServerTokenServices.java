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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.radarcns.auth.authentication.TokenValidator;
import org.radarcns.auth.exception.TokenValidationException;
import org.radarcns.auth.token.RadarToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ResourceServerTokenServices} for using with Auth from Management Portal.
 * Uses {@link TokenValidator} from the radar-auth to validate the token. Also encapsulated the
 * {@link RadarToken} in the Auth object using the {@link ManagementPortalOAuth2Authentication}
 *
 * @author yatharthranjan
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "managementportal.security.enabled", havingValue = "true")
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class ManagementPortalResourceServerTokenServices implements ResourceServerTokenServices {

  private static final TokenValidator tokenValidator;

  static {
    tokenValidator = new TokenValidator();
  }

  private transient ClientDetailsService clientDetailsService;

  public OAuth2Authentication loadAuthentication(String accessTokenValue)
      throws AuthenticationException, InvalidTokenException {
    RadarToken accessToken;
    try {
      accessToken = tokenValidator.validateAccessToken(accessTokenValue);
    } catch (TokenValidationException exc) {
      throw new InvalidTokenException(exc.toString(), exc);
    }

    if (accessToken == null) {
      throw new InvalidTokenException("Invalid access token: " + accessTokenValue);
    } else if (accessToken.getExpiresAt().before(new Date())) {
      throw new InvalidTokenException("Access token expired: " + accessTokenValue);
    }

    Set<GrantedAuthority> grantedAuthorities =
        accessToken.getAuthorities().stream()
            .map(a -> (GrantedAuthority) () -> a)
            .collect(Collectors.toSet());
    OAuth2Request request =
        new OAuth2Request(
            null,
            null,
            grantedAuthorities,
            true,
            new HashSet<>(accessToken.getScopes()),
            new HashSet<>(accessToken.getAudience()),
            null,
            null,
            null);

    OAuth2Authentication result = new OAuth2Authentication(request, null);
    log.debug(result.toString());

    if (result == null) {
      // in case of race condition
      throw new InvalidTokenException("Invalid access token: " + accessTokenValue);
    }
    if (clientDetailsService != null) {
      String clientId = result.getOAuth2Request().getClientId();
      try {
        clientDetailsService.loadClientByClientId(clientId);
      } catch (ClientRegistrationException e) {
        throw new InvalidTokenException("Client not valid: " + clientId, e);
      }
    }
    return new ManagementPortalOAuth2Authentication(result, accessToken);
  }

  /**
   * Retrieve the full access token details from just the value.
   *
   * @param accessToken the token value
   * @return the full access token with client id etc.
   */
  @Override
  public OAuth2AccessToken readAccessToken(String accessToken) {
    RadarToken accessTokenRadar = tokenValidator.validateAccessToken(accessToken);
    OAuth2AccessToken oAuth2AccessToken =
        new OAuth2AccessToken() {
          @Override
          public Map<String, Object> getAdditionalInformation() {
            Map<String, Object> info = new HashMap<>();
            info.put("sources", accessTokenRadar.getSources());
            info.put("radar-token", accessTokenRadar);
            return info;
          }

          @Override
          public Set<String> getScope() {
            return new HashSet<>(accessTokenRadar.getScopes());
          }

          @Override
          public OAuth2RefreshToken getRefreshToken() {
            return null;
          }

          @Override
          public String getTokenType() {
            return BEARER_TYPE.toLowerCase(Locale.UK);
          }

          @Override
          public boolean isExpired() {
            return accessTokenRadar.getExpiresAt().before(new Date());
          }

          @Override
          public Date getExpiration() {
            return accessTokenRadar.getExpiresAt();
          }

          @Override
          public int getExpiresIn() {
            return Long.valueOf(
                    (accessTokenRadar.getExpiresAt().getTime() - System.currentTimeMillis())
                        / 1000L)
                .intValue();
          }

          @Override
          public String getValue() {
            return accessTokenRadar.getToken();
          }
        };

    return oAuth2AccessToken;
    //    return new DefaultOAuth2AccessToken(accessToken);
  }
}
