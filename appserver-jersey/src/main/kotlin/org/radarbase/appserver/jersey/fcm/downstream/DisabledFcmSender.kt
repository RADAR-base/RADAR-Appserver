package org.radarbase.appserver.jersey.fcm.downstream

import org.radarbase.appserver.jersey.fcm.model.FcmDownstreamMessage

class DisabledFcmSender : FcmSender {
    @Throws(Exception::class)
    override fun send(downstreamMessage: FcmDownstreamMessage) {
        // do nothing
    }

    override fun doesProvideDeliveryReceipt(): Boolean {
        return false
    }
}
