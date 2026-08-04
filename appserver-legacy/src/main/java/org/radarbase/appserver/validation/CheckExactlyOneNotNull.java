package org.radarbase.appserver.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapperImpl;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = CheckExactlyOneNotNull.CheckExactlyOneNotNullValidator.class)
@Documented
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public @interface CheckExactlyOneNotNull {

    String[] fieldNames();

    public static class CheckExactlyOneNotNullValidator implements ConstraintValidator<CheckExactlyOneNotNull, Object> {

        private transient String[] fieldNames;

        public void initialize(CheckExactlyOneNotNull constraintAnnotation) {
            this.fieldNames = constraintAnnotation.fieldNames();
        }

        public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
            BeanWrapperImpl wrapper = new BeanWrapperImpl(object);
            int notNullCtr = 0;
            try {
                for (String fieldName : fieldNames) {
                    Object property = wrapper.getPropertyValue(fieldName);
                    if (property != null) notNullCtr++;
                }
                return notNullCtr == 1;
            } catch (Exception e) {
                return false;
            }
        }

    }

}
