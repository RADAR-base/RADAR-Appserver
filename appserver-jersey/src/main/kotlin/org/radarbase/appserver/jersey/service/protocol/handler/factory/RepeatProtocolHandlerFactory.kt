package org.radarbase.appserver.jersey.service.protocol.handler.factory

import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.handler.impl.SimpleRepeatProtocolHandler

object RepeatProtocolHandlerFactory {
    fun getRepeatProtocolHandler(protocolHandlerType: RepeatProtocolHandlerType): ProtocolHandler {
        return SimpleRepeatProtocolHandler()
    }
}
