/*
 * Copyright 2024 Haulmont.
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

/**
 * Meta description that describes available component conversions.
 */
public @interface StudioConvertStrategy {

    /**
     * List of tags available for conversion.
     */
    TagInfo[] tagsToConvertInto() default {};

    /**
     * List of components classes available for conversion.
     */
    ClassInfo[] classesToConvertInto() default {};

    @interface ClassInfo {
        /**
         * Component class FQN.
         */
        String qualifiedName();

        /**
         * @see AttributeConvertStrategy
         */
        AttributeConvertStrategy[] attributeConvertStrategy() default {};
    }

    @interface TagInfo {

        /**
         * Xml tag FQN.
         */
        String qualifiedName();

        /**
         * @see AttributeConvertStrategy
         */
        AttributeConvertStrategy[] attributeConvertStrategy() default {};
    }

    /**
     * Meta description for attribute conversion.
     */
    @interface AttributeConvertStrategy {
        /**
         * Xml attribute FQN.
         */
        String qualifiedName();

        /**
         * Xml attribute type.
         */
        StudioPropertyType type();

        /**
         * Xml attribute new value.
         */
        String value();

        /**
         * Related xml attribute from which to take a new value.
         */
        String valueFrom() default "";
    }
}
