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

package org.radarbase.appserver.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.beans.BeanWrapperImpl
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

@Target(CLASS)
@Retention(RUNTIME)
@Constraint(validatedBy = [CheckExactlyOneNotNull.CheckExactlyOneNotNullValidator::class])
@MustBeDocumented
annotation class CheckExactlyOneNotNull(
    val fieldNames: Array<String>,
) {
    class CheckExactlyOneNotNullValidator : ConstraintValidator<CheckExactlyOneNotNull, Any> {

        private lateinit var fieldNames: Array<String>

        override fun initialize(constraintAnnotation: CheckExactlyOneNotNull) {
            this.fieldNames = constraintAnnotation.fieldNames
        }

        override fun isValid(obj: Any?, constraintContext: ConstraintValidatorContext): Boolean {
            if (obj == null) return false

            val wrapper = BeanWrapperImpl(obj)
            var notNullCtr = 0

            return try {
                for (fieldName in fieldNames) {
                    val property = wrapper.getPropertyValue(fieldName)
                    if (property != null) notNullCtr++
                }
                notNullCtr == 1
            } catch (e: Exception) {
                false
            }
        }
    }
}
