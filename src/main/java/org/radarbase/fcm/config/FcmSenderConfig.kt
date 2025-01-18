package org.radarbase.fcm.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseOptions
import org.radarbase.fcm.downstream.AdminSdkFcmSender
import org.radarbase.fcm.downstream.DisabledFcmSender
import org.radarbase.fcm.downstream.FcmSender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*

@Configuration
class FcmSenderConfig(
    @Transient private val serverConfig: FcmServerConfig,
    @Transient private val beanFactory: BeanFactory
) {
    @Bean
    fun fcmSenderProps(): FcmSender {
        var sender = serverConfig.fcmsender
        if (sender == null) {
            sender = "rest"
        }
        return when (sender) {
            "rest", "org.radarbase.fcm.downstream.AdminSdkFcmSender" -> AdminSdkFcmSender(
                beanFactory.getBean<FirebaseOptions?>(
                    "firebaseOptions", FirebaseOptions::class.java
                )
            )

            "disabled" -> DisabledFcmSender()
            else -> throw IllegalStateException("Unknown FCM sender type $sender")
        }
    }

    /**
     * Create firebase options from settings. They are either read from the credentials in
     * FcmServerConfig.credentials base64 encoded string, or from the path specified by the
     * environment variable GOOGLE_APPLICATION_CREDENTIALS.
     * @return initialized firebase options.
     * @throws IOException if the given credentials cannot be read or parsed.
     */
    @Lazy
    @Bean
    @Throws(IOException::class)
    fun firebaseOptions(): FirebaseOptions {
        var googleCredentials: GoogleCredentials? = null

        if (serverConfig.credentials != null) {
            try {
                // read base64 encoded value directly
                val decodedCredentials = Base64.getDecoder().decode(serverConfig.credentials)
                ByteArrayInputStream(decodedCredentials).use { input ->
                    googleCredentials = GoogleCredentials.fromStream(input)
                }
            } catch (ex: IllegalArgumentException) {
                logger.error("Cannot load credentials from fcmserver.credentials", ex)
            }
        }

        if (googleCredentials == null) {
            // read from path specified with environment variable GOOGLE_APPLICATION_CREDENTIALS
            googleCredentials = GoogleCredentials.getApplicationDefault()
        }
        return FirebaseOptions.builder()
            .setCredentials(googleCredentials)
            .build()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FcmSenderConfig::class.java)
    }
}
