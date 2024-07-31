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
 * Meta description that describes group of elements.
 */
@Target(ElementType.METHOD)
@Inherited
public @interface StudioElementsGroup {

    /**
     * @see StudioComponent#name
     */
    String name() default "";

    /**
     * FQN of element class that should be added into group.
     */
    String elementClassFqn() default "";

    /**
     * Specifies that elements should use the same xml namespace as the group.
     */
    boolean useGroupXmlns() default true;

    /**
     * @see StudioComponent#icon
     */
    String icon() default "io/jmix/flowui/kit/meta/icon/unknownComponent.svg";

    /**
     * @see StudioComponent#xmlElement
     */
    String xmlElement() default "";

    /**
     * @see StudioComponent#xmlns
     */
    String xmlns() default "";

    /**
     * @see StudioComponent#xmlnsAlias
     */
    String xmlnsAlias() default "";

    /**
     * FQNs of target component class or component tag (e.g.: <code>tag:button</code>)
     */
    String[] target() default {};

    /**
     * FQNs of unsupported target component class or component tag (e.g.: <code>tag:button</code>)
     */
    String[] unsupportedTarget() default {};

    /**
     * Specifies that group should be visible in Studio preview.
     */
    boolean visible() default false;

    /**
     * Defines whether an elements group can be added to component an unlimited number of times.
     * By default, an elements group can only be added once.
     *
     * @return whether an elements group can be added to component an unlimited number of times
     */
    boolean unlimitedCount() default false;

    /**
     * @see StudioComponent#properties
     */
    StudioProperty[] properties() default {};

    /**
     * @see StudioComponent#documentationLink
     */
    String documentationLink() default "";
}
