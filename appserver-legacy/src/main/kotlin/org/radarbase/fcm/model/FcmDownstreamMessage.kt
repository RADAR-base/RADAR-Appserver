/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */
package org.radarbase.fcm.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotEmpty

/** @author yatharthranjan
 */
@Suppress("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @Suppress("UNCHECKED_CAST")
    abstract class Builder<T : Builder<T>> {
        protected var to: String? = null
        protected var condition: String? = null
        protected var messageId: String? = null
        protected var collapseKey: String? = null
        protected var priority: String? = null
        protected var contentAvailable: Boolean? = null
        protected var mutableContent: Boolean? = null
        protected var timeToLive: Int? = null
        protected var deliveryReceiptRequested: Boolean? = null
        protected var dryRun: Boolean? = null

        fun to(to: String) = apply {
            this.to = to
        } as T

        fun condition(condition: String?) = apply {
            this.condition = condition
        } as T

        fun messageId(messageId: String) = apply {
            this.messageId = messageId
        } as T

        fun collapseKey(collapseKey: String?) = apply {
            this.collapseKey = collapseKey
        } as T

        fun priority(priority: String?) = apply {
            this.priority = priority
        } as T

        fun contentAvailable(contentAvailable: Boolean?) = apply {
            this.contentAvailable = contentAvailable
        } as T

        fun mutableContent(mutableContent: Boolean?) = apply {
            this.mutableContent = mutableContent
        } as T

        fun timeToLive(timeToLive: Int?) = apply {
            this.timeToLive = timeToLive
        } as T

        fun deliveryReceiptRequested(deliveryReceiptRequested: Boolean?) =
            apply {
                this.deliveryReceiptRequested = deliveryReceiptRequested
            } as T

        fun dryRun(dryRun: Boolean?) = apply {
            this.dryRun = dryRun
        } as T

        protected fun applyTo(message: FcmDownstreamMessage) {
            message.to = this.to ?: throw IllegalArgumentException("'to' must not be null")
            message.condition = this.condition
            message.messageId = this.messageId ?: throw IllegalArgumentException("'messageId' must not be null")
            message.collapseKey = this.collapseKey
            message.priority = this.priority
            message.contentAvailable = this.contentAvailable
            message.mutableContent = this.mutableContent
            message.timeToLive = this.timeToLive
            message.deliveryReceiptRequested = this.deliveryReceiptRequested
            message.dryRun = this.dryRun
        }

        abstract fun build(): FcmDownstreamMessage
    }
}
