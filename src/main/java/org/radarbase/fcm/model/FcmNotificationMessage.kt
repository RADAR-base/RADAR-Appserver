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
import com.google.firebase.messaging.*
import java.time.Duration
import java.time.Instant
import javax.validation.constraints.NotEmpty

/** @author yatharthranjan
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class FcmNotificationMessage(
    @JsonProperty
    override val to: @NotEmpty String,

    @JsonProperty
    override val condition: String? = null,

    @JsonProperty("message_id")
    override val messageId: @NotEmpty String,

    @JsonProperty("collapse_key")
    override val collapseKey: String? = null,

    @JsonProperty
    override val priority: String? = null,

    @JsonProperty("content_available")
    override val contentAvailable: Boolean? = null,

    @JsonProperty("mutable_content")
    override val mutableContent: Boolean? = null,

    @JsonProperty("time_to_live")
    override val timeToLive: Int = 2419200, // 4 weeks

    @JsonProperty("delivery_receipt_requested")
    override val deliveryReceiptRequested: Boolean = false,

    @JsonProperty("dry_run")
    override val dryRun: Boolean = false,

    @JsonProperty
    override val data: Map<String, String?>? = null,

    // TODO Add specific Notification model and data model classes instead of using Maps.
    @JsonProperty
    val notification: Map<String, Any>,

    ) : FcmDownstreamMessage {
    override fun getAndroidConfig(): AndroidConfig = AndroidConfig.builder()
        .setCollapseKey(collapseKey)
        .setPriority(AndroidConfig.Priority.valueOf(priority ?: "HIGH"))
        .setTtl((timeToLive * 1000).toLong())
        .setNotification(getAndroidNotification())
        .putAllData(data)
        .build()

    override fun getApnsConfig(): ApnsConfig {
        val config = ApnsConfig.builder()
        collapseKey?.let {
            config.putHeader("apns-collapse-id", collapseKey)
        }

        // The date at which the notification is no longer valid. This value is a UNIX epoch
        // expressed in seconds (UTC).
        config.putHeader(
            "apns-expiration", (
                    Instant.now()
                        .plus(Duration.ofSeconds(timeToLive.toLong()))
                        .toEpochMilli() / 1000).toString()
        )

        val apsAlertBuilder = ApsAlert.builder()
        notification["title"]?.let { apsAlertBuilder.setTitle(it.toString()) }
        notification["body"]?.let { apsAlertBuilder.setBody(it.toString()) }
        notification["title_loc_key"]?.let { apsAlertBuilder.setTitleLocalizationKey(it.toString()) }
        notification["title_loc_key"]?.let {
            notification["title_loc_args"]?.let {
                apsAlertBuilder.addTitleLocalizationArg(
                    it.toString()
                )
            }
        }
        notification["body_loc_key"]?.let { apsAlertBuilder.setLocalizationKey(it.toString()) }
        notification["body_loc_key"]?.let {
            notification["body_loc_args"]?.let {
                apsAlertBuilder
                    .addLocalizationArg(
                        it
                            .toString()
                    )
            }
        }

        val apsBuilder = Aps.builder()
        notification["sound"]?.let { apsBuilder.setSound(it.toString()) }
        notification["badge"]?.let { apsBuilder.setBadge(it.toString().toInt()) }

        notification["category"]?.let { apsBuilder.setCategory(it.toString()) }
        notification["thread_id"]?.let { apsBuilder.setThreadId(it.toString()) }

        contentAvailable?.let {
            apsBuilder.setContentAvailable(contentAvailable)
        }
        mutableContent?.let {
            apsBuilder.setMutableContent(mutableContent)
        }
        return config
            .putAllCustomData(data)
            .setAps(apsBuilder.setAlert(apsAlertBuilder.build()).build())
            .putHeader("apns-push-type", "alert")
            .build()


    }

    override fun getWebPushConfig(): WebpushConfig {
        TODO("Not yet implemented")
    }

    private fun getAndroidNotification(): AndroidNotification {
        return AndroidNotification.builder().apply {
            setBody(notification.getOrDefault("body", "").toString())
            setTitle(notification.getOrDefault("title", "").toString())
            notification["android_channel_id"]?.let {
                setChannelId(it.toString())
            }
            notification["color"]?.let {
                setColor(it.toString())
            }
            notification["icon"]?.let {
                setIcon(it.toString())
            }
            notification["image"]?.let {
                setImage(it.toString())
            }
            notification["tag"]?.let {
                setTag(it.toString())
            }
            notification["click_action"]?.let {
                setClickAction(it.toString())
            }
            notification["body_loc_key"]?.let {
                setBodyLocalizationKey(it.toString())
                addBodyLocalizationArg(notification["body_loc_args"].toString())
            }

            notification["title_loc_key"]?.let {
                setTitleLocalizationKey(it.toString())
                addTitleLocalizationArg(notification["title_loc_args"].toString())
            }
            notification["sound"]?.let {
                setSound(it.toString())
            }
        }.build()
    }
}