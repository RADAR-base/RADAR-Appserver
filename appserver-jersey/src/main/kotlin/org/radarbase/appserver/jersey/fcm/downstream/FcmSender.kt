package org.radarbase.appserver.jersey.fcm.downstream

import org.radarbase.appserver.jersey.fcm.model.FcmDownstreamMessage

interface FcmSender {
    @Throws(Exception::class)
    fun send(downstreamMessage: FcmDownstreamMessage)

    fun doesProvideDeliveryReceipt(): Boolean
}
