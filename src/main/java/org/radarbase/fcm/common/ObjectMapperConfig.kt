package org.radarbase.fcm.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for providing a singleton [ObjectMapper] instance.
 * The same instance can be reused throughout the application.
 */
@Configuration
class ObjectMapperConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(
                KotlinModule.Builder()
                    .configure(KotlinFeature.NullIsSameAsDefault, true)
                    .build()
            )
            registerModule(JavaTimeModule())
        }
    }
}
