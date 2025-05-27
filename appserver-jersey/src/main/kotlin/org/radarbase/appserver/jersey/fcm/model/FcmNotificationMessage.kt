package org.radarbase.appserver.jersey.fcm.model

import com.fasterxml.jackson.annotation.JsonProperty

class FcmNotificationMessage : FcmDownstreamMessage() {

    @JsonProperty
    var notification: Map<String, Any>? = null

    @JsonProperty
    var data: Map<String?, String?>? = null

}
