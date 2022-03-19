package test_support.entity.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PurchasedAfterManufacturedValidator.class)
@Documented
public @interface PurchasedAfterManufactured {
    String message() default "{msg://com.company.scr.entity.constraints/Car.PurchasedAfterManufactured.constraintViolationMessage}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
