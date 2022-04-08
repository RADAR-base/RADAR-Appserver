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
package org.radarbase.fcm.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.config.AbstractFactoryBean
import org.springframework.stereotype.Component

/**
 * A Factory Bean that provides [ObjectMapper] so that can be [ ] and same instance can be used everywhere.
 *
 * @see AbstractFactoryBean for more details
 *
 * @author yatharthranjan
 */
@Component
class ObjectMapperFactory : AbstractFactoryBean<ObjectMapper>() {
    // TODO Remove this if can directly autowire from Spring context
    override fun getObjectType(): Class<*> {
        return ObjectMapper::class.java
    }

    override fun createInstance(): ObjectMapper {
        return ObjectMapper()
            .registerModule(
                KotlinModule.Builder()
                    .configure(KotlinFeature.NullIsSameAsDefault, true)
                    .build()
            )
            .registerModule(JavaTimeModule())
    }
}