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

package org.radarbase.fcm.config;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLSocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.gcm.provider.GcmExtensionProvider;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.radarbase.fcm.downstream.FcmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.event.EventListener;
import org.springframework.integration.config.EnableIntegration;

/**
 * Configuration class providing all the beans and config required by the FCM XMPP client.
 *
 * @author yatharthranjan
 */
@Configuration
@EnableIntegration
@ImportResource({"classpath:inbound-xmpp.xml", "classpath:outbound-xmpp.xml"})
@Slf4j
public class FcmAppConfiguration implements PingFailedListener {

  private transient ReconnectionEnabledXmppConnectionFactoryBean connectionFactoryBean;

  @Autowired private transient FcmServerConfig fcmServerConfig;

  @Bean("xmppConnection")
  public ReconnectionEnabledXmppConnectionFactoryBean xmppConnection() throws Exception {

    String domain = "gcm.googleapis.com";
    XMPPTCPConnectionConfiguration connectionConfiguration =
        XMPPTCPConnectionConfiguration.builder()
            .setHost(fcmServerConfig.getHost())
            .setPort(fcmServerConfig.getPort())
            .setUsernameAndPassword(
                fcmServerConfig.getSenderId() + "@" + domain, fcmServerConfig.getServerKey())
            .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
            .setSendPresence(false)
            .setSocketFactory(SSLSocketFactory.getDefault())
            .setXmppDomain(domain)
            .build();
    connectionFactoryBean = new ReconnectionEnabledXmppConnectionFactoryBean();
    connectionFactoryBean.setConnectionConfiguration(connectionConfiguration);
    connectionFactoryBean.setSubscriptionMode(null);
    connectionFactoryBean.setAutoStartup(true);

    return connectionFactoryBean;
  }

  @PreDestroy
  public void springPreDestroy() {
    if (connectionFactoryBean.isRunning()) {
      connectionFactoryBean.stop();
    }
  }

  @Bean("gcmExtension")
  public GcmExtensionProvider getExtensionProvider() {
    return new GcmExtensionProvider();
  }

  @EventListener(ApplicationReadyEvent.class)
  public void registerPing() throws Exception {
    // Set the ping interval
    log.info("Setting the PING interval to 100 seconds");
    final PingManager pingManager = PingManager.getInstanceFor(connectionFactoryBean.getObject());
    pingManager.setPingInterval(100);
    pingManager.registerPingFailedListener(this);
  }

  @Bean("fcmSenderProps")
  @SuppressWarnings("unchecked")
  public FcmSender getFcmSender() throws Exception {
    Class<? extends FcmSender> senderClass =
        (Class<? extends FcmSender>) Class.forName(fcmServerConfig.getFcmsender());
    return senderClass.getConstructor().newInstance();
  }

  @Override
  public void pingFailed() {
    log.info("The ping failed, restarting the ping interval again ...");
    try {
      final PingManager pingManager = PingManager.getInstanceFor(connectionFactoryBean.getObject());
      pingManager.setPingInterval(100);
    } catch (Exception exc) {
      log.warn("The ping interval cannot be started.", exc);
    }
  }
}
