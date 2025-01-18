package org.radarbase.fcm.downstream

import org.radarbase.fcm.model.FcmDownstreamMessage

class DisabledFcmSender : FcmSender {
    @Throws(Exception::class)
    override fun send(message: FcmDownstreamMessage) {
        // do nothing
    }

    override fun doesProvideDeliveryReceipt(): Boolean {
        return false
    }
}
