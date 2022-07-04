package io.jmix.flowui.view;

import java.lang.annotation.Target;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface ComponentId {

    /**
     * The id of the element to map to. When empty, the name of the field is
     * used instead.
     *
     * @return the id of the element to map to
     */
    String value() default "";
}
