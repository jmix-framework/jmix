package test_support.entity.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReliabilityPolicyCompliantValidator.class)
@Documented
public @interface ReliabilityPolicyCompliant {
    String message() default "{msg://com.company.scr.entity.constraints/Car.ReliabilityPolicyCompliant.constraintViolationMessage}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
