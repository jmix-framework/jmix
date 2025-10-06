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
 * <p></p>
 * You can override an already existing {@link StudioElementsGroup} for your component
 * by specifying the same {@link StudioElementsGroup#xmlElement}
 * and defining your {@link StudioElementsGroup#xmlns}
 * or defining a more specific {@link StudioElementsGroup#target} (usually the FQN of your component).
 */
@Target(ElementType.METHOD)
@Inherited
public @interface StudioElementsGroup {

    String TARGET_TAG_PREFIX = "tag:";
    String TARGET_GROUP_IDENTIFIER_PREFIX = "groupIdentifier:";

    /**
     * Optional.
     * <p></p>
     *
     * Unique elements group identifier.
     * <p></p>
     *
     * Identifier is used to identify elements group in
     * {@link StudioElementsGroup#target} and {@link StudioElementsGroup#unsupportedTarget}
     * when need to define a nested elements group in {@link StudioElementsGroup} meta.
     */
    String identifier() default "";

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
     * Target that group should be added to.
     * <p></p>
     * Studio accepts the following formats:
     * <p></p>
     * <ul>
     *     <li>FQN of component class
     *     (e.g.: <code>io.jmix.flowui.component.combobox.EntityComboBox</code>)</li>
     *
     *     <li>Component tag.
     *     Use {@link StudioElementsGroup#TARGET_TAG_PREFIX}
     *     (e.g: <code>tag:button</code>)</li>
     *
     *     <li>Identifier of an elements group.
     *     Use {@link StudioElementsGroup#TARGET_GROUP_IDENTIFIER_PREFIX}
     *     (e.g.: <code>groupIdentifier:my_group_id</code>)</li>
     * </ul>
     */
    String[] target() default {};

    /**
     * Target that group should <b>NOT</b> be added to.
     * <p></p>
     * See {@link #target()} for details and available formats.
     */
    String[] unsupportedTarget() default {};

    /**
     * Specifies the information about elements that are excluded from a given elements group.
     */
    StudioExcludedElementsInfo excludedElementsInfo() default @StudioExcludedElementsInfo;

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
