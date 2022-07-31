package io.jmix.flowui.kit.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Inherited
public @interface StudioAction {

    String type() default "";

    String classFqn() default "";

    String icon() default "";

    String description() default "";

    String defaultProperty() default "";

    String[] target() default {};

    String[] unsupportedTarget() default {};

    boolean availableInViewWizard() default false;

    StudioProperty[] properties() default {};

    StudioPropertiesItem[] items() default {};
}
