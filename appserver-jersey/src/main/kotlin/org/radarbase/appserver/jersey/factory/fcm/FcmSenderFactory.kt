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
