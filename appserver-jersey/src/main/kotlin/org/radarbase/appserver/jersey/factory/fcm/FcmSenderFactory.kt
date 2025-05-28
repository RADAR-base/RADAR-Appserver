/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.jersey.factory.fcm

import com.google.common.base.Supplier
import com.google.firebase.FirebaseOptions
import jakarta.inject.Inject
import org.radarbase.appserver.jersey.config.FcmServerConfig
import org.radarbase.appserver.jersey.fcm.downstream.AdminSdkFcmSender
import org.radarbase.appserver.jersey.fcm.downstream.DisabledFcmSender
import org.radarbase.appserver.jersey.fcm.downstream.FcmSender

class FcmSenderFactory @Inject constructor(
    private val firebaseOptions: FirebaseOptions,
    private val serverConfig: FcmServerConfig,
) : Supplier<FcmSender> {
    override fun get(): FcmSender {
        var sender = serverConfig.fcmsender
        if (sender == null) {
            sender = "rest"
        }
        return when (sender) {
            "rest", "org.radarbase.appserver.jersey.fcm.downstream.AdminSdkFcmSender" -> AdminSdkFcmSender(
                firebaseOptions,
            )

            "disabled" -> DisabledFcmSender()
            else -> throw IllegalStateException("Unknown FCM sender type $sender")
        }
    }
}
