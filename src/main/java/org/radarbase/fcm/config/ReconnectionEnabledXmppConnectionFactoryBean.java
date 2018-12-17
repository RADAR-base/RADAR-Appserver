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

import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.sm.predicates.ForEveryStanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.gcm.packet.GcmPacketExtension;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.integration.xmpp.config.XmppConnectionFactoryBean;

/**
 * An extended class of {@link XmppConnectionFactoryBean} with support for Reconnection and connection draining implementation.
 * It reconnects in all scenarios that the FCM xmpp server disconnects (either via the client or the server). It also implements a
 * Back off strategy as recommended by google when connection draining is reported by the FCM Server. The FCM server reports connection draining
 * as a message before closing the connection. This needs to set in the Message receiver via the {@link #setIsConnectionDraining(Boolean)}.
 *
 * @see org.radarbase.fcm.upstream.XmppFcmReceiver
 *
 * @author yatharthranjan
 */
@Slf4j
public class ReconnectionEnabledXmppConnectionFactoryBean extends XmppConnectionFactoryBean implements ConnectionListener, ReconnectionListener {

    private Boolean isConnectionDraining;
    private Boolean isPresenceUnavailable;

    @Override
    public void start() {
        this.isPresenceUnavailable = false;

        super.start();

        XMPPTCPConnection connection = getConnection();
        connection.addConnectionListener(this);

        ReconnectionManager manager = ReconnectionManager.getInstanceFor(connection);
        manager.enableAutomaticReconnection();
        manager.addReconnectionListener(this);

        connection.addStanzaInterceptor(stanza -> {

            /**
             * If the server requested to terminate connection, then the smack library disconnects the connection with
             * a presence unavailable message as can be seen in {@link XMPPTCPConnection.PacketReader#parsePackets()}
             * {@code // We received a closing stream element from the server without us
             * // sending a closing stream element first. This means that the
             * // server wants to terminate the session, therefore disconnect
             * // the connection
             * disconnect(); }
             *
             * which calls the {@link AbstractXMPPConnection#disconnect()}.{@link AbstractXMPPConnection#disconnect(Presence)}.
             * We need to handle this and reconnect if the server asks for disconnection.
             */
            if(stanza instanceof Presence && ! ((Presence)stanza).isAvailable()) {
                logger.info("Reconnect after server requested disconnection.");
                //reconnect();
                isPresenceUnavailable = true;
            }
            log.info("Sent: {}", stanza.toXML(GcmPacketExtension.NAMESPACE));
        }, ForEveryStanza.INSTANCE);

    }

    /**
     * Notification that the connection has been successfully connected to the remote endpoint (e.g. the XMPP server).
     * <p>
     * Note that the connection is likely not yet authenticated and therefore only limited operations like registering
     * an account may be possible.
     * </p>
     *
     * @param connection the XMPPConnection which successfully connected to its endpoint.
     */
    @Override
    public void connected(XMPPConnection connection) {
        log.info("Connection established.");
    }

    /**
     * Notification that the connection has been authenticated.
     *
     * @param connection the XMPPConnection which successfully authenticated.
     * @param resumed    true if a previous XMPP session's stream was resumed.
     */
    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        log.info("User authenticated.");
        // This is the last step after a connection or reconnection
        setIsConnectionDraining(false);
    }

    /**
     * Notification that the connection was closed normally.
     */
    @Override
    public void connectionClosed() {
        log.info("Connection closed. The current connectionDraining flag is: {}", isConnectionDraining);
        if (isConnectionDraining || isPresenceUnavailable) {
            super.stop();
            reconnect();
        }
    }

    synchronized void reconnect() {
        log.info("Initiating reconnection ...");
        final BackOffStrategy backoff = new BackOffStrategy(5, 1000);
        while (backoff.shouldRetry()) {
            try {
                start();
                backoff.doNotRetry();
            } catch (BeanInitializationException e) {
                log.info("The notifier server could not reconnect after the connection draining message.");
                backoff.errorOccured();
            }
        }
    }

    public void setIsConnectionDraining(Boolean isConnectionDraining) {
        this.isConnectionDraining = isConnectionDraining;
    }

    /**
     * Notification that the connection was closed due to an exception. When
     * abruptly disconnected it is possible for the connection to try reconnecting
     * to the server.
     *
     * @param e the exception.
     */
    @Override
    public void connectionClosedOnError(Exception e) {
        log.info("Connection closed on error.");
    }

    /**
     * The connection will retry to reconnect in the specified number of seconds.
     * <p>
     * Note: This method is only called if {@link ReconnectionManager#isAutomaticReconnectEnabled()} returns true, i.e.
     * only when the reconnection manager is enabled for the connection.
     * </p>
     *
     * @param seconds remaining seconds before attempting a reconnection.
     */
    @Override
    public void reconnectingIn(int seconds) {
        log.info("Reconnecting in {} ...", seconds);
    }

    /**
     * An attempt to connect to the server has failed. The connection will keep trying reconnecting to the server in a
     * moment.
     * <p>
     * Note: This method is only called if {@link ReconnectionManager#isAutomaticReconnectEnabled()} returns true, i.e.
     * only when the reconnection manager is enabled for the connection.
     * </p>
     *
     * @param e the exception that caused the reconnection to fail.
     */
    @Override
    public void reconnectionFailed(Exception e) {
        log.info("Reconnection failed! Error: {}", e.getMessage());
    }
}