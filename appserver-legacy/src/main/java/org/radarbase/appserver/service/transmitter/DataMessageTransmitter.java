package org.radarbase.appserver.service.transmitter;

import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.exception.MessageTransmitException;

public interface DataMessageTransmitter {
    void send(DataMessage dataMessage) throws MessageTransmitException;
}
