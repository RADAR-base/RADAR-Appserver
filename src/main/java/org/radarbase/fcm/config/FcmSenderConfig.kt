package org.radarbase.fcm.config

import org.radarbase.fcm.downstream.FcmSender
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class FcmSenderConfig(private val fcmServerConfig: FcmServerConfig) {
    @Throws(Exception::class)
    @Bean("fcmSenderProps")
    open fun fcmSender(): FcmSender {
        val senderClass = Class.forName(fcmServerConfig.fcmsender) as Class<out FcmSender>
        return senderClass.getConstructor().newInstance()
    }
}