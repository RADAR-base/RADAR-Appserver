package org.radarbase.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseOptions;
import org.radarbase.fcm.downstream.AdminSdkFcmSender;
import org.radarbase.fcm.downstream.DisabledFcmSender;
import org.radarbase.fcm.downstream.FcmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class FcmSenderConfig {
  private static final Logger logger = LoggerFactory.getLogger(FcmSenderConfig.class);

  private final FcmServerConfig serverConfig;

  private final BeanFactory beanFactory;

  public FcmSenderConfig(FcmServerConfig serverConfig, BeanFactory beanFactory) {
    this.serverConfig = serverConfig;
    this.beanFactory = beanFactory;
  }

  @Bean
  public FcmSender fcmSenderProps() {
    String sender = serverConfig.getFcmsender();
    if (sender == null) {
      sender = "rest";
    }
    return switch (sender) {
      case "rest", "org.radarbase.fcm.downstream.AdminSdkFcmSender" ->
              new AdminSdkFcmSender(beanFactory.getBean(
                      "firebaseOptions", FirebaseOptions.class));
      case "disabled" -> new DisabledFcmSender();
      default -> throw new IllegalStateException("Unknown FCM sender type " + sender);
    };
  }

  /**
   * Create firebase options from settings. They are either read from the credentials in
   * FcmServerConfig.credentials base64 encoded string, or from the path specified by the
   * environment variable GOOGLE_APPLICATION_CREDENTIALS.
   * @return initialized firebase options.
   * @throws IOException if the given credentials cannot be read or parsed.
   */
  @Lazy
  @Bean
  public FirebaseOptions firebaseOptions() throws IOException {
    GoogleCredentials googleCredentials = null;

    if (serverConfig.getCredentials() != null) {
      try {
        // read base64 encoded value directly
        byte[] decodedCredentials = Base64.getDecoder().decode(serverConfig.getCredentials());
        try (ByteArrayInputStream input = new ByteArrayInputStream(decodedCredentials)) {
          googleCredentials = GoogleCredentials.fromStream(input);
        }
      } catch (IllegalArgumentException ex) {
        logger.error("Cannot load credentials from fcmserver.credentials", ex);
      }
    }

    if (googleCredentials == null) {
      // read from path specified with environment variable GOOGLE_APPLICATION_CREDENTIALS
      googleCredentials = GoogleCredentials.getApplicationDefault();
    }
    return FirebaseOptions.builder()
            .setCredentials(googleCredentials)
            .build();
  }
}
