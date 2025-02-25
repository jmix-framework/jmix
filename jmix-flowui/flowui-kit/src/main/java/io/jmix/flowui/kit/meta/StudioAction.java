/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.kit.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * Meta description that describes action.
 */
@Target(ElementType.METHOD)
@Inherited
public @interface StudioAction {

    /**
     * Action type. Should be related to {@code io.jmix.flowui.action.ActionType}.
     */
    String type() default "";

    /**
     * @see StudioComponent#classFqn
     */
    String classFqn() default "";

    /**
     * @see StudioComponent#classFqn
     */
    String icon() default "io/jmix/flowui/kit/meta/icon/action/action.svg";

    /**
     * Action description.
     * Studio will use the description in wizards.
     */
    String description() default "";

    /**
     * @see StudioActionsGroup#target
     */
    String[] target() default {};

    /**
     * @see StudioActionsGroup#unsupportedTarget
     */
    String[] unsupportedTarget() default {
            "io.jmix.flowui.app.main.StandardMainView",
            "io.jmix.tabbedmode.app.main.StandardTabbedModeMainView"
    };

    /**
     * Specifies whether the action should be displayed in the view creation wizard.
     */
    boolean availableInViewWizard() default false;

    /**
     * @see StudioComponent#properties
     */
    StudioProperty[] properties() default {};

    /**
     * Properties that should be wrapped into <code>property</code> tag
     * and added to the <code>properties</code> tag.
     */
    StudioPropertiesItem[] items() default {};

    /**
     * @see StudioComponent#documentationLink
     */
    String documentationLink() default "";
}
