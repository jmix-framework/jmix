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

import java.lang.annotation.Annotation;

/**
 * Describes how generics should be resolved.
 */
public @interface GenericResolvingInfo {

    /**
     * Generic type parameter name.
     * For example, we have component with type {@code DataGrid<T>},
     * then {@code typeParameter} should be {@code T}.
     */
    String typeParameter();

    /**
     * Describes generic resolving strategy.
     *
     * @see ResolvingStrategy
     */
    ResolvingStrategy resolvingStrategy();

    /**
     * Describes how generic value can be resolved.
     * For example, we can resolve generic type from XML (see {@link XmlStrategy})
     * or define a class FQN (see {@link ClassFqnStrategy})
     *
     * @see XmlStrategy
     * @see ClassFqnStrategy
     */
    @interface ResolvingStrategy {

        /**
         * Describes resolving from XML.
         *
         * @see XmlStrategy
         */
        XmlStrategy xmlStrategy();

        /**
         * Describes resolving from concrete class FQN.
         *
         * @see ClassFqnStrategy
         */
        ClassFqnStrategy classFqnStrategy();

        /**
         * Describes type from concrete class FQN with type parameters.
         */
        @interface ClassFqnStrategy {

            /**
             * Class FQN of type.
             */
            String classFqn();

            /**
             * Class FQNs of type parameters (if needed).
             */
            String[] typeParametersFqn() default {};
        }

        /**
         * Describes type from component that should be extracted from XML.
         */
        @interface XmlStrategy {

            String PARENT_COMPONENT_TAG_PLACEHOLDER = "${PARENT_TAG}";

            /**
             * Parent component tag local name.
             * Supports {@link XmlStrategy#PARENT_COMPONENT_TAG_PLACEHOLDER} placeholder.
             * <p>
             * Can be combined with <code>:</code> char.
             * For example <code>${PARENT_TAG}:${PARENT_TAG}</code>.
             */
            String parentComponentTag();

            /**
             * Class FQNs of type parameters.
             */
            String[] typeParametersFqn() default {};

            /**
             * Index of type parameter from resolved type.
             * <p>
             * If value is negative or zero, then a resolved type will be used.
             * <p>
             * If value is positive, then a type parameter (with index from value)
             * from the resolved type will be used.
             */
            int takeFromTypeParameter() default -1;
        }
    }
}
