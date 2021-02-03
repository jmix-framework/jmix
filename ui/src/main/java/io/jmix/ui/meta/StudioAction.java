/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.meta;

import io.jmix.ui.action.Action;

import java.lang.annotation.*;

/**
 * Indicates that the annotated interface should be available in Studio Screen Designer as an action.
 * Provides metadata for Component Palette and Component Inspector of Screen Designer. The annotated interface must be a
 * direct or indirect subclass of {@link Action}.
 */
@Documented
@Target(ElementType.TYPE)
public @interface StudioAction {
    /**
     * Caption of the action in Screen Designer Palette.
     */
    String caption() default "";

    /**
     * Description of the action in Screen Designer Palette.
     */
    String description() default "";

    /**
     * Fully-qualified class names of the components to which the action can be applied.
     * Some base interface is expected in most cases.
     * E.g. "io.jmix.ui.component.ListComponent"
     */
    String[] target() default {};

    /**
     * Component Palette icon, SVG or PNG.
     */
    String icon() default "";

    /**
     * Indicates that action should be available in screen wizard
     */
    boolean availableInScreenWizard() default false;
}
