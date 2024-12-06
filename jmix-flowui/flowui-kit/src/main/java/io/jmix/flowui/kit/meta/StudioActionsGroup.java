/*
 * Copyright 2022 Haulmont.
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
 * Meta description that describes group of actions.
 *  * <p></p>
 *  * You can override an already existing {@link StudioActionsGroup} for your component
 *  * by specifying the same {@link StudioActionsGroup#xmlElement}
 *  * and defining your {@link StudioActionsGroup#xmlns}
 *  * or defining a more specific {@link StudioActionsGroup#target} (usually the FQN of your component).
 */
@Target(ElementType.METHOD)
@Inherited
public @interface StudioActionsGroup {

    /**
     * @see StudioComponent#name
     */
    String name() default "";

    /**
     * FQN of action class that should be added to the group.
     */
    String actionClassFqn() default "";

    /**
     * Specifies that group actions should use the same xml namespace as the group.
     */
    boolean useGroupXmlns() default true;

    /**
     * @see StudioComponent#icon
     */
    String icon() default "io/jmix/flowui/kit/meta/icon/actionsgroup/actions.svg";

    /**
     * @see StudioComponent#xmlElement
     */
    String xmlElement() default "actions";

    /**
     * @see StudioComponent#xmlns
     */
    String xmlns() default "";

    /**
     * @see StudioComponent#xmlnsAlias
     */
    String xmlnsAlias() default "";

    /**
     * FQN of target component class or component tag (e.g.: <code>tag:button</code>)
     */
    String[] target() default {};

    /**
     * FQNs of unsupported target component class or component tag (e.g.: <code>tag:button</code>)
     */
    String[] unsupportedTarget() default {};

    /**
     * @see StudioComponent#properties
     */
    StudioProperty[] properties() default {};

    /**
     * @see StudioComponent#documentationLink
     */
    String documentationLink() default "";
}
