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
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.WebpushConfig
import javax.validation.constraints.NotEmpty

/** @author yatharthranjan
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
interface FcmDownstreamMessage : FcmMessage {
    val to: @NotEmpty String?
    val condition: String?
    val messageId: @NotEmpty String?
    val collapseKey: String?
    val priority: String?
    val contentAvailable: Boolean?
    val mutableContent: Boolean?
    val timeToLive: Int
    val deliveryReceiptRequested: Boolean
    val dryRun: Boolean
    val data: Map<String, String>?

    fun getAndroidConfig(): AndroidConfig
    fun getApnsConfig(): ApnsConfig
    fun getWebPushConfig(): WebpushConfig
}