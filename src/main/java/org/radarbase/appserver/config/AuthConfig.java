package org.radarbase.appserver.config;

import org.radarcns.auth.token.RadarToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import radar.spring.auth.common.AuthAspect;
import radar.spring.auth.common.AuthValidator;
import radar.spring.auth.common.Authorization;
import radar.spring.auth.config.ManagementPortalAuthProperties;
import radar.spring.auth.managementportal.ManagementPortalAuthValidator;
import radar.spring.auth.managementportal.ManagementPortalAuthorization;

@Configuration
@ConditionalOnProperty(name = "security.radar.managementportal.enabled", havingValue = "true")
@EnableAspectJAutoProxy
public class AuthConfig {

  @Value("${security.radar.managementportal.url}")
  private transient String baseUrl;

  @Value("${security.oauth2.resource.id}")
  private transient String resourceName;

  @Bean
  public ManagementPortalAuthProperties getAuthProperties() {
    return new ManagementPortalAuthProperties(baseUrl, resourceName);
  }

  @Bean
  AuthValidator<RadarToken> getAuthValidator(
      @Autowired ManagementPortalAuthProperties managementPortalAuthProperties) {
    return new ManagementPortalAuthValidator(managementPortalAuthProperties);
  }

  @Bean
  Authorization<RadarToken> getAuthorization() {
    return new ManagementPortalAuthorization();
  }

  @Bean
  AuthAspect getAuthAspect(
      @Autowired AuthValidator<RadarToken> authValidator,
      @Autowired Authorization<RadarToken> authorization) {
    return new AuthAspect<>(authValidator, authorization);
  }

  public interface AuthEntities {
    String MEASUREMENT = "MEASUREMENT";
    String PROJECT = "PROJECT";
    String SUBJECT = "SUBJECT";
    String SOURCE = "SOURCE";
  }

  public interface AuthPermissions {
    String READ = "READ";
    String CREATE = "CREATE";
    String UPDATE = "UPDATE";
  }
}
