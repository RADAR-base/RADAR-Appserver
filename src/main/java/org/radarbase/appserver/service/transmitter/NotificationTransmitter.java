package org.radarbase.appserver.service.transmitter;

import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.exception.MessageTransmitException;

public interface NotificationTransmitter {
    // Can Sneakily throw MessageTransmitException
    void send(Notification notification);
}
