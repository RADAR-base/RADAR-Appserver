package org.radarbase.appserver.jersey.service.protocol.handler.factory

import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.handler.impl.ClinicalProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.handler.impl.SimpleProtocolHandler

object ProtocolHandlerFactory {
    fun getProtocolHandler(protocolType: ProtocolHandlerType): ProtocolHandler {
        return when (protocolType) {
            ProtocolHandlerType.CLINICAL -> ClinicalProtocolHandler()
            else -> SimpleProtocolHandler()
        }
    }
}
