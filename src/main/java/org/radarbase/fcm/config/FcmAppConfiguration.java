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

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.gcm.provider.GcmExtensionProvider;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.radarbase.fcm.downstream.FcmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.event.EventListener;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.xmpp.config.XmppConnectionFactoryBean;
import org.springframework.integration.xmpp.support.DefaultXmppHeaderMapper;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLSocketFactory;

@Configuration
@EnableIntegration
@ImportResource({"classpath:inbound-xmpp.xml", "classpath:outbound-xmpp.xml"})
public class FcmAppConfiguration implements PingFailedListener {

    private static final Logger logger = LoggerFactory.getLogger(FcmAppConfiguration.class);

    private XmppConnectionFactoryBean connectionFactoryBean;

    private XMPPTCPConnectionConfiguration connectionConfiguration;

    @Autowired
    private FcmServerConfig fcmServerConfig;

    @Bean("xmppConnection")
    public XmppConnectionFactoryBean xmppConnection() throws Exception {

        String domain = "gcm.googleapis.com";
        connectionConfiguration = XMPPTCPConnectionConfiguration.builder()
                .setHost("fcm-xmpp.googleapis.com").setPort(5236)
                .setUsernameAndPassword(fcmServerConfig.getSenderId() + "@" + domain, fcmServerConfig.getServerKey())//"xxxxxxxxxxxxxxxxx")
                .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                .setSendPresence(false)
                .setSocketFactory(SSLSocketFactory.getDefault())
                .setXmppDomain(domain)
                .build();
        connectionFactoryBean = new XmppConnectionFactoryBean();
        connectionFactoryBean.setConnectionConfiguration(connectionConfiguration);
        connectionFactoryBean.setSubscriptionMode(null);
        connectionFactoryBean.setAutoStartup(true);

        return connectionFactoryBean;
    }

    @PreDestroy
    public void springPreDestroy() {
        if(connectionFactoryBean.isRunning()) {
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
        logger.info("Setting the PING interval to 100 seconds");
        final PingManager pingManager = PingManager.getInstanceFor(connectionFactoryBean.getObject());
        pingManager.setPingInterval(100);
        pingManager.registerPingFailedListener(this);
    }


/*    @EventListener(ApplicationReadyEvent.class)
    public void registerReconnection() throws Exception {
        // Set the ping interval
        logger.info("Setting the PING interval to 100 seconds");
        final PingManager pingManager = PingManager.getInstanceFor(connectionFactoryBean.getObject());
        pingManager.setPingInterval(100);
        pingManager.registerPingFailedListener(this);
    }*/


    @Bean("fcmSenderProps")
    public FcmSender getFcmSender() throws ClassNotFoundException, ClassCastException, InstantiationException, IllegalAccessException {
        Class<? extends FcmSender> senderClass = (Class<? extends FcmSender>) Class.forName(fcmServerConfig.getFcmsender());
        return senderClass.newInstance();
    }

    @Override
    public void pingFailed() {
        logger.info("The ping failed, restarting the ping interval again ...");
        try {
            final PingManager pingManager = PingManager.getInstanceFor(connectionFactoryBean.getObject());
            pingManager.setPingInterval(100);
        } catch (Exception exc) {
            logger.warn("The ping interval cannot be started.", exc);
        }
    }
}