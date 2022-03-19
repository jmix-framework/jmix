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

import java.lang.annotation.Documented;

/**
 * Indicates that the annotated method should be shown in Studio Screen Designer
 * as a nested group of elements of UI component, e.g. columns, actions, fields.
 */
@Documented
public @interface StudioElementsGroup {
    /**
     * @return caption of the elements group in Studio Screen Designer
     */
    String caption() default "";

    /**
     * @return description of the elements group in Studio Screen Designer
     */
    String description() default "";

    /**
     * @return XML tag of the elements group
     */
    String xmlElement() default "";

    /**
     * Specifies path to the component icon, SVG or PNG. Relative to the component module root.
     * The icon used in the Component Hierarchy.
     *
     * @return relative path to the SVG or PNG icon file.
     */
    String icon() default "";

    /**
     * Specifies URL pointing to the documentation page for the annotated elements group.
     * Used in <i>Jmix Documentation</i> action in the Studio Screen Designer.<br>
     * If the documentation is version dependent, use %VERSION% as a placeholder.
     * It will be replaced with the minor version (e.g. 1.2) of the artifact containing UI component.
     *
     * @return URL of the documentation page
     */
    String documentationURL() default "";

    /**
     * @return the maximum number of times a group of elements can occur
     */
    MaxOccurNumber maxOccurs() default MaxOccurNumber.ONE;

    /**
     * Specifies FQN of base element class. If not specified, then it will be automatically defined as a type of
     * the method parameter.
     *
     * @return FQN of base element class
     */
    String elementClass() default "";

    /**
     * @return XML tag of the element in the group if it differs from the xmlElement specified
     * in the element definition class.
     */
    String elementXmlName() default "";
}