package org.radarbase.appserver.jersey.fcm.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotEmpty

abstract class FcmDownstreamMessage : FcmMessage {
    @field:JsonProperty
    @field:NotEmpty
    var to: String? = null

    @field:JsonProperty
    var condition: String? = null

    @field:JsonProperty("message_id")
    @field:NotEmpty
    var messageId: String? = null

    @field:JsonProperty("collapse_key")
    var collapseKey: String? = null

    @field:JsonProperty
    var priority: String? = null

    @field:JsonProperty("content_available")
    var contentAvailable: Boolean? = null

    @field:JsonProperty("mutable_content")
    var mutableContent: Boolean? = null

    @field:JsonProperty("time_to_live")
    var timeToLive: Int? = null

    @field:JsonProperty("delivery_receipt_requested")
    var deliveryReceiptRequested: Boolean? = null

    @field:JsonProperty("dry_run")
    var dryRun: Boolean? = null
}
