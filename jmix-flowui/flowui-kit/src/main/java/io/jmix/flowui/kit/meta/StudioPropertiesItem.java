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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Describes properties that included into {@code <properties></properties>} tag.
 * This is usually relevant for actions.
 */
@Documented
@Target(ElementType.TYPE_PARAMETER)
public @interface StudioPropertiesItem {

    /**
     * Xml attribute name.
     */
    String xmlAttribute();

    /**
     * Xml attribute type.
     */
    StudioPropertyType type();

    /**
     * Fully-qualified name of property class
     */
    String classFqn() default "";

    /**
     * Property category.
     * @see StudioProperty.Category
     */
    String category() default StudioProperty.Category.ADDITIONAL;

    boolean required() default false;

    String defaultValue() default "";

    String initialValue() default "";

    String[] options() default {};

    String setMethod() default "";

    String setParameterFqn() default "";

    String addMethod() default "";

    String addParameterFqn() default "";

    String removeMethod() default "";

    String removeParameterFqn() default "";
}
