package io.jmix.flowui.action;

import io.jmix.flowui.Actions;
import io.jmix.flowui.SameAsUi;
import io.jmix.flowui.kit.action.Action;

import java.lang.annotation.*;

/**
 * Indicates that {@link Action} can be created with {@link Actions} factory and can be used in screen XML descriptor.
 */
@SameAsUi
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ActionType {

    String VALUE_ATTRIBUTE = "value";

    /**
     * @return id of action type
     */
    String value();
}