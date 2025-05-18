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
