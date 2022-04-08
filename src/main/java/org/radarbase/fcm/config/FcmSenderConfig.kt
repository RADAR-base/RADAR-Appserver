package org.radarbase.fcm.config;

import org.radarbase.fcm.downstream.FcmSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FcmSenderConfig {

  private final transient FcmServerConfig fcmServerConfig;

  public FcmSenderConfig(FcmServerConfig fcmServerConfig) {
    this.fcmServerConfig = fcmServerConfig;
  }

  @Bean("fcmSenderProps")
  @SuppressWarnings("unchecked")
  public FcmSender getFcmSender() throws Exception {
    Class<? extends FcmSender> senderClass =
        (Class<? extends FcmSender>) Class.forName(fcmServerConfig.getFcmsender());
    return senderClass.getConstructor().newInstance();
  }
}
