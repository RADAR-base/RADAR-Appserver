package org.radarbase.appserver.jersey.fcm.model

import com.fasterxml.jackson.annotation.JsonProperty

class FcmDataMessage : FcmDownstreamMessage() {
    @JsonProperty
    var data: Map<String?, String?>? = null

}
