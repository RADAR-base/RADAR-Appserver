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

package org.radarbase.appserver.jersey.utils.annotation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import java.beans.Introspector
import java.beans.PropertyDescriptor

@Target(CLASS)
@Retention(RUNTIME)
@Constraint(validatedBy = [CheckExactlyOneNotNull.Validator::class])
annotation class CheckExactlyOneNotNull(
    val fieldNames: Array<String>,
) {
    class Validator : ConstraintValidator<CheckExactlyOneNotNull, Any> {
        private lateinit var fieldNames: Array<String>

        override fun initialize(constraint: CheckExactlyOneNotNull) {
            fieldNames = constraint.fieldNames
        }

        override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
            if (value == null) return false

            val beanInfo = Introspector.getBeanInfo(value::class.java, Any::class.java)
            val props: Array<PropertyDescriptor> = beanInfo.propertyDescriptors

            return try {
                var nonNullCount = 0
                for (pd in props) {
                    if (pd.readMethod != null && fieldNames.contains(pd.name)) {
                        val propValue = pd.readMethod.invoke(value)
                        if (propValue != null) nonNullCount++
                    }
                }
                nonNullCount == 1
            } catch (_: Exception) {
                false
            }

        }
    }
}
