package org.radarbase.fcm.downstream;

import org.radarbase.fcm.model.FcmDownstreamMessage;

public class DisabledFcmSender implements FcmSender {
    @Override
    public void send(FcmDownstreamMessage message) throws Exception {
        // do nothing
    }

    @Override
    public boolean doesProvideDeliveryReceipt() {
        return false;
    }
}
