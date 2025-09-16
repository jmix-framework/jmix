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
 * Meta description that describes specific element for component.
 */
@Target(ElementType.METHOD)
@Inherited
public @interface StudioElement {

    /**
     * @see StudioComponent#name
     */
    String name() default "";

    /**
     * @see StudioComponent#classFqn
     */
    String classFqn() default "";

    /**
     * @see StudioComponent#injectionIdentifier
     */
    String injectionIdentifier() default "id";

    /**
     * @see StudioComponent#isInjectable
     */
    boolean isInjectable() default true;

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
     * @see StudioElementsGroup#target
     */
    String[] target() default {};

    /**
     * @see StudioElementsGroup#unsupportedTarget
     */
    String[] unsupportedTarget() default {};

    /**
     * @see StudioElementsGroup#visible
     */
    boolean visible() default false;

    /**
     * @see StudioElementsGroup#unlimitedCount
     */
    boolean unlimitedCount() default true;

    /**
     * @see StudioComponent#properties
     */
    StudioProperty[] properties() default {};

    /**
     * @see StudioComponent#propertiesBindings
     */
    StudioPropertiesBinding[] propertiesBindings() default {};

    /**
     * @see StudioComponent#supplyHandlers
     */
    StudioSupplyHandler[] supplyHandlers() default {};

    /**
     * @see StudioComponent#customSubscriptions
     */
    StudioCustomSubscription[] customSubscriptions() default {};

    /**
     * @see StudioComponent#customInstalls
     */
    StudioCustomInstall[] customInstalls() default {};

    /**
     * @see StudioComponent#availableChildren
     */
    StudioAvailableChildrenInfo availableChildren() default @StudioAvailableChildrenInfo();

    /**
     * @see StudioComponent#documentationLink
     */
    String documentationLink() default "";
}
